//
//  AuthenticationManager.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/04/24.
//

import Foundation
import TRETJapanNFCReader_MIFARE_IndividualNumber
import CryptoKit

public class AuthenticationManager:IndividualNumberReaderSessionDelegate{
    private var authenticationController:AuthenticationController
    private var individualNumberCardExecuteType: IndividualNumberCardExecuteType?
    private var actionURL: String?
    private var reader: IndividualNumberReader!

    init(authenticationController: AuthenticationController) {
        self.authenticationController = authenticationController
    }
    
    public func authenticate(pin : String,nonce : String,actionURL : String){
        self.actionURL = actionURL
        self.computeDigitalSignatureForUserAuthentication(userAuthenticationPIN: pin, dataToSign: nonce)
    }
    
    public func authenticateForSignature(pin : String,nonce : String,actionURL : String){
        self.actionURL = actionURL
        self.computeDigitalSignatureForSignature(SignaturePIN: pin, dataToSign: nonce)
    }
    
    public func getDigitalCertificate(){
        self.getDigitalCertificateForUserVerification()
    }

    public func individualNumberReaderSession(didRead individualNumberCardData: TRETJapanNFCReader_MIFARE_IndividualNumber.IndividualNumberCardData) {
        switch self.individualNumberCardExecuteType {
        case .computeDigitalSignature:
            if let digitalSignature = individualNumberCardData.digitalSignatureForUserVerification,
               let digitalCertificate = individualNumberCardData.digitalCertificateForUserVerification,
               let actionURL = self.actionURL
            {
                print(digitalSignature)
                print(digitalCertificate)
                self.verifySignature(digitalSignature: digitalSignature, digitalCertificate: digitalCertificate, actionURL: actionURL)
            }
            break
        case .computeDigitalSignatureForSignature:
            if let digitalSignature = individualNumberCardData.digitalSignatureForUserVerification,
               let digitalCertificate = individualNumberCardData.digitalCertificateForUserVerification,
               let actionURL = self.actionURL
            {
                print(digitalSignature)
                print(digitalCertificate)
                self.verifySignature(digitalSignature: digitalSignature, digitalCertificate: digitalCertificate, actionURL: actionURL)
            }
            break
        case .getCardInfoInput:
            break
        case .getDigitalCertificateForUserVerification:
            if let digitalCertificate = individualNumberCardData.digitalCertificateForUserVerification {
                print(digitalCertificate)
            }
            break
        case .lookupRemainingPIN:
            break
        case .none:
            break
        }
    }
    
    public func japanNFCReaderSession(didInvalidateWithError error: Error) {
        
    }
    
    private func computeDigitalSignatureForUserAuthentication(userAuthenticationPIN:String, dataToSign:String) {
        self.individualNumberCardExecuteType = .computeDigitalSignature

        let data = dataToSign.data(using: .utf8)
        let nonceStr = (SHA256.hash(data: data!).description)
        print(nonceStr)
        
        self.authenticationController.nonceHash = String(nonceStr.dropFirst(15))
        print(self.authenticationController.nonceHash)

        let dataToSignByteArray = [UInt8](dataToSign.utf8)
        self.reader = IndividualNumberReader(delegate: self)
        // 以下処理はNFC読み取りが非同期で行われ、完了するとindividualNumberReaderSessionが呼び出される
        self.reader.computeDigitalSignatureForUserAuthentication(userAuthenticationPIN: userAuthenticationPIN,dataToSign: dataToSignByteArray)
    }
    
    private func computeDigitalSignatureForSignature(SignaturePIN:String, dataToSign:String) {
        self.individualNumberCardExecuteType = .computeDigitalSignatureForSignature
        
        let data = dataToSign.data(using: .utf8)
        let nonceStr = (SHA256.hash(data: data!).description)
        print(nonceStr)
        
        self.authenticationController.nonceHash = String(nonceStr.dropFirst(15))
        print(self.authenticationController.nonceHash)
        
        let dataToSignByteArray = [UInt8](dataToSign.utf8)
        self.reader = IndividualNumberReader(delegate: self)
        // 以下処理はNFC読み取りが非同期で行われ、完了するとindividualNumberReaderSessionが呼び出される
        self.reader.computeDigitalSignatureForSignature(SignaturePIN: SignaturePIN,dataToSign: dataToSignByteArray)
    }
    
    private func getDigitalCertificateForUserVerification(){
        self.individualNumberCardExecuteType = .getDigitalCertificateForUserVerification
        self.reader = IndividualNumberReader(delegate: self)
        self.reader.getDigitalCertificateForUserVerification()
    }
    
    private func verifySignature(digitalSignature:[UInt8],digitalCertificate:[UInt8],actionURL : String){
        
        guard let digitalSignatureBase64URLEncoded = encodingBase64URL(from: digitalSignature) else {
            return
        }
        guard let digitalCertificateBase64URLEncoded = encodingBase64URL(from: digitalCertificate) else {
            return
        }
        
        print(digitalSignatureBase64URLEncoded)
        print(digitalCertificateBase64URLEncoded)
        
        sendVerifySignatureRequest(signature: digitalSignatureBase64URLEncoded,
                                   x509File: digitalCertificateBase64URLEncoded, actionURL: actionURL)
    }
    
    private func sendVerifySignatureRequest(signature:String,x509File:String,actionURL:String){
        guard let url = URL(string: actionURL) else{
            return
        }
        var request = URLRequest(url: url)
        
        var mode:String = ""
        switch( self.authenticationController.runMode){
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
    
    
    private func encodingBase64URL(from : [UInt8]) -> String?{
        let fromData = Data(from)
        let fromBase64Encoded = fromData.base64EncodedString(options: [])
        let allowedCharacterSet = CharacterSet(charactersIn: "!*'();:@&=+$,/?%#[]").inverted
        let fromBase64URLEncoded = fromBase64Encoded.addingPercentEncoding(withAllowedCharacters: allowedCharacterSet)
        return fromBase64URLEncoded
    }
    
}
