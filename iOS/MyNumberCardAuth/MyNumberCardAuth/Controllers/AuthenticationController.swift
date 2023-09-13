//
//  AuthenticationController.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/07/05.
//

import Foundation
import SwiftUI

class AuthenticationController: ObservableObject {
    @Published var viewState: ShowView = .UserVerificationView
    @Published var runMode: Mode = .Login
    @Published var isAlert: Bool = false
    @Published var isLinkAlert: Bool = false
    @Published var messageTitle: String = ""
    @Published var messageString: String = ""
    @Published var isErrorOpenURL: Bool = false
    @Published var nonce: String = ""
    @Published var queryDict: [String: String]?
    @Published var openURL: String = ""
    @Published var controller: ViewController = .init()
    @Published var controllerForSignature: SignatureViewController = .init()

    // 利用規約/プライバシーポリシー/個人情報保護方針URL
    @Published var termsOfUseURL: String = Bundle.main.object(forInfoDictionaryKey: "TermsOfServiceURL") as! String
    @Published var privacyPolicyURL: String = Bundle.main.object(forInfoDictionaryKey: "PrivacyPolicyURL") as! String
    @Published var protectionPolicyURL: String = Bundle.main.object(forInfoDictionaryKey: "ProtectionPolicyURL") as! String
    // 問い合わせURL
    @Published var inquiryURL: String = Bundle.main.object(forInfoDictionaryKey: "InquiryURL") as! String

    public func clear() {
        isLinkAlert = false
        isAlert = false
        messageTitle = ""
        messageString = ""
        isErrorOpenURL = false
    }

    func openURLButton(url: String) {
        if url.isEmpty == false {
            if let openURL = URL(string: url) {
                UIApplication.shared.open(openURL)
            }
        }
    }

    public func startReading(pin: String, nonce: String, actionURL: String) {
        let authenticationManager = AuthenticationManager(authenticationController: self)
        switch viewState {
        case .SignatureView:
            authenticationManager.authenticateForSignature(pin: pin, nonce: nonce, actionURL: actionURL)
        case .UserVerificationView:
            authenticationManager.authenticateForUserVerification(pin: pin, nonce: nonce, actionURL: actionURL)
        case .ExplanationView:
            break
        }
    }

    public func getButtonColor(checkStr: String) -> Color {
        if checkStr.isEmpty == false {
            return Color(UIColor.blue)
        } else {
            return Color(UIColor.lightGray)
        }
    }

    public func
        setErrorPageURL(queryDict: [String: String])
    {
        if queryDict["error_url"] != nil {
            if let url = queryDict["error_url"] {
                let replaceUrl = url.replacingOccurrences(of: "&amp;", with: "&")
                openURL = replaceUrl
            }
        }
    }
}
