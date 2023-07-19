//
//  AuthenticationController.swift
//  MyNumberCardAuth
//
//  Created by abelstaff on 2023/07/05.
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
    @Published var nonceHash:String = ""
    @Published var queryDict : [String: String]?
    @Published var openURL:String = ""
    
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
    
    public func readStart(pin : String,nonce : String,actionURL : String){
        let authenticationManager = AuthenticationManager(authenticationController: self)
        switch(self.viewState){
        case .SignatureView:
            authenticationManager.authenticateForSignature(pin: pin, nonce: nonce ,actionURL: actionURL)
            break;
        case .UserVerificationView:
            authenticationManager.authenticate(pin: pin, nonce: nonce ,actionURL: actionURL)
            break
        case.ExplanationView:
            break
        }
        
    }
    
    public func toggleColor(url:String) -> Color{
        if (url.isEmpty == false) {
            return Color(UIColor.blue)
        }else{
            return Color(UIColor.lightGray)
        }
    }
    
    public func setOpenURL(queryDict : [String: String]){
        if(((queryDict["errorURL"]) != nil)){
            if let url = queryDict["errorURL"]{
                self.openURL = url
            }
        }
    }
}
