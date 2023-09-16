//
//  AuthenticationManager.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/04/24.
//

import Foundation
import TRETJapanNFCReader_MIFARE_IndividualNumber
import CryptoKit

public class AuthenticationManager:IndividualNumberReaderSessionDelegate{
    private var authenticationController:AuthenticationController
    private var individualNumberCardSignatureType: IndividualNumberCardSignatureType?
    private var actionURL: String?
    private var reader: IndividualNumberReader!

    init(authenticationController: AuthenticationController) {
        self.authenticationController = authenticationController
    }
    
    public func authenticateForUserVerification(pin: String, nonce: String, actionURL: String){
        self.actionURL = actionURL
        self.conputeDigitalSignatureForUserVerification(userAuthenticationPIN: pin, dataToSign: nonce)
    }
    
    public func authenticateForSignature(pin: String, nonce: String, actionURL: String){
        self.actionURL = actionURL
        self.computeDigitalCertificateForSignature(signaturePIN: pin, dataToSign: nonce)
    }
    
    public func individualNumberReaderSession(didRead individualNumberCardData: TRETJapanNFCReader_MIFARE_IndividualNumber.IndividualNumberCardData) {
        switch self.individualNumberCardSignatureType {
        case .userAuthentication:
            if let digitalSignature = individualNumberCardData.computeDigitalSignatureForUserAuthentication,
               let digitalCertificate = individualNumberCardData.userAuthenticationCertificate,
               let actionURL = self.actionURL
            {
                self.verifySignature(digitalSignature: digitalSignature, digitalCertificate: digitalCertificate, actionURL: actionURL)
            }
            break
        case .digitalSignature:
            if let digitalSignature = individualNumberCardData.computeDigitalSignatureForDigitalSignature,
               let digitalCertificate = individualNumberCardData.digitalSignatureCertificate,
               let actionURL = self.actionURL
            {
                self.verifySignature(digitalSignature: digitalSignature, digitalCertificate: digitalCertificate, actionURL: actionURL)
            }
            break
        case .none:
            break
        }
    }
    
    public func japanNFCReaderSession(didInvalidateWithError error: Error) {
        
    }
    
    private func conputeDigitalSignatureForUserVerification(userAuthenticationPIN: String, dataToSign: String) {
        self.individualNumberCardSignatureType = .userAuthentication

        let data = dataToSign.data(using: .utf8)
        let nonceStr = (SHA256.hash(data: data!).description)
        
        self.authenticationController.nonceHash = String(nonceStr.dropFirst(15))

        let dataToSignByteArray = [UInt8](dataToSign.utf8)
        self.reader = IndividualNumberReader(delegate: self)
        // 以下処理はNFC読み取りが非同期で行われ、完了するとindividualNumberReaderSessionが呼び出される
        self.reader.computeDigitalSignature(signatureType: self.individualNumberCardSignatureType!, pin: userAuthenticationPIN, dataToSign: dataToSignByteArray)
    }
    
    private func computeDigitalCertificateForSignature(signaturePIN: String, dataToSign: String) {
        self.individualNumberCardSignatureType = .digitalSignature
        
        let data = dataToSign.data(using: .utf8)
        let nonceStr = (SHA256.hash(data: data!).description)
        
        self.authenticationController.nonceHash = String(nonceStr.dropFirst(15))

        let dataToSignByteArray = [UInt8](dataToSign.utf8)
        self.reader = IndividualNumberReader(delegate: self)
        // 以下処理はNFC読み取りが非同期で行われ、完了するとindividualNumberReaderSessionが呼び出される
        self.reader.computeDigitalSignature(signatureType: self.individualNumberCardSignatureType!, pin: signaturePIN, dataToSign: dataToSignByteArray)
    }
        
    private func verifySignature(digitalSignature: [UInt8], digitalCertificate: [UInt8], actionURL: String){
        
        guard let digitalSignatureBase64URLEncoded = encodingBase64URL(from: digitalSignature) else {
            return
        }
        guard let digitalCertificateBase64URLEncoded = encodingBase64URL(from: digitalCertificate) else {
            return
        }
        
        sendVerifySignatureRequest(signature: digitalSignatureBase64URLEncoded,
                                   x509File: digitalCertificateBase64URLEncoded, actionURL: actionURL)
    }
    
    private func sendVerifySignatureRequest(signature: String,x509File: String,actionURL: String){
        guard let url = URL(string: actionURL) else{
            return
        }
        var request = URLRequest(url: url)
        
        var mode:String = ""
        switch(self.authenticationController.runMode){
        case .Login:
            mode = "login"
        case .Registration:
            mode = "registration"
        case .Replacement:
            mode = "replacement"
        }
        
        var certificate:String = ""
        switch(self.authenticationController.viewState){
        case .UserVerificationView:
            certificate = "userAuthenticationCertificate"
        case .SignatureView:
            certificate = "encryptedDigitalSignatureCertificate"
        case .ExplanationView:
            break
        }
        
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        var requestBodyComponents = URLComponents()
        requestBodyComponents.queryItems = [URLQueryItem(name: "mode", value: mode),
                                            URLQueryItem(name: certificate, value: x509File),
                                            URLQueryItem(name: "applicantData", value: self.authenticationController.nonceHash),
                                            URLQueryItem(name: "sign", value: signature)]
        
        request.httpBody = requestBodyComponents.query?.data(using: .utf8)
        
        let session = HTTPSession(authenticationController: self.authenticationController)
        session.openRedirectURLOnSafari(request: request)
    }
    
    
    private func encodingBase64URL(from: [UInt8]) -> String?{
        let fromData = Data(from)
        let fromBase64Encoded = fromData.base64EncodedString(options: [])
        let allowedCharacterSet = CharacterSet(charactersIn: "!*'();:@&=+$,/?%#[]").inverted
        let fromBase64URLEncoded = fromBase64Encoded.addingPercentEncoding(withAllowedCharacters: allowedCharacterSet)
        return fromBase64URLEncoded
    }
    
}
