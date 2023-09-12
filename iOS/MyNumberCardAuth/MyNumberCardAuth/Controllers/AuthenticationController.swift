//
//  AuthenticationController.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/07/05.
//

import Foundation
import SwiftUI

class AuthenticationController: ObservableObject {
    @Published var viewState:ShowView = .UserVerificationView
    @Published var runMode:Mode = .Login
    @Published var isAlert:Bool = false
    @Published var isLinkAlert:Bool = false
    @Published var messageTitle:String = ""
    @Published var messageString:String = ""
    @Published var isErrorOpenURL:Bool = false
    @Published var nonce:String = ""
    @Published var queryDict:[String: String]?
    @Published var openURL:String = ""
    @Published var controllerForUserVerification:UserVerificationViewController = UserVerificationViewController()
    @Published var controllerForSignature:SignatureViewController = SignatureViewController()

    // 利用規約/プライバシーポリシー/個人情報保護方針URL
    @Published var termsOfUseURL:String = Bundle.main.object(forInfoDictionaryKey: "TermsOfServiceURL") as! String
    @Published var privacyPolicyURL:String = Bundle.main.object(forInfoDictionaryKey: "PrivacyPolicyURL") as! String
    @Published var protectionPolicyURL:String = Bundle.main.object(forInfoDictionaryKey: "ProtectionPolicyURL") as! String
    // 問い合わせURL
    @Published var inquiryURL:String = Bundle.main.object(forInfoDictionaryKey: "InquiryURL") as! String
    
    public func clear(){
        self.isLinkAlert = false
        self.isAlert = false
        self.messageTitle = ""
        self.messageString = ""
        self.isErrorOpenURL = false
    }
    
    func openURLButton(url: String){
        if (url.isEmpty == false) {
            if let openURL = URL(string: url){
                UIApplication.shared.open(openURL)
            }
        }
    }
    
    public func startReading(pin: String, nonce: String, actionURL: String){
        let authenticationManager = AuthenticationManager(authenticationController: self)
        switch(self.viewState){
        case .SignatureView:
            authenticationManager.authenticateForSignature(pin: pin, nonce: nonce, actionURL: actionURL)
            break;
        case .UserVerificationView:
            authenticationManager.authenticateForUserVerification(pin: pin, nonce: nonce, actionURL: actionURL)
            break
        case.ExplanationView:
            break
        }
        
    }
    
    public func getButtonColor(checkStr: String) -> Color{
        if (checkStr.isEmpty == false) {
            return Color(UIColor.blue)
        }else{
            return Color(UIColor.lightGray)
        }
    }
    
    public func
    setErrorPageURL(queryDict: [String : String]){
        if(((queryDict["error_url"]) != nil)){
            if let url = queryDict["error_url"]{
                let replaceUrl = url.replacingOccurrences(of: "&amp;", with: "&")
                self.openURL = replaceUrl
            }
        }
    }
    
    public func onOpenURL(url: URL) {
        let urlComponents = URLComponents(url: url, resolvingAgainstBaseURL: true)
        if let urlComponents = urlComponents {
            self.queryDict = generateQueryDictionary(from: urlComponents)
            if let query = queryDict {
                self.viewState = self.viewState.isViewMode(queryDict: query)
                self.runMode = self.runMode.isMode(queryDict: query)
                self.queryDict = query;
                self.clear()
                self.setErrorPageURL(queryDict: query)
                
                if let actionURL = query["action_url"], let nonse = query["nonce"]
                {
                    self.controllerForUserVerification.inputPIN = ""
                    self.controllerForSignature.inputPIN = ""
                    self.controllerForUserVerification.actionURL = actionURL
                    self.controllerForSignature.actionURL = actionURL
                    self.controllerForUserVerification.nonce = nonse
                    self.controllerForSignature.nonce = nonse
                }
            }
        }
    }
}
