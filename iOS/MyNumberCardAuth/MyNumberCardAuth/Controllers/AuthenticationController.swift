//
//  AuthenticationController.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/07/05.
//

import Foundation
import SwiftUI

public class AuthenticationController: ObservableObject {
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
    @Published var controllerForUserVerification: UserVerificationViewController = .init()
    @Published var controllerForSignature: SignatureViewController = .init()

    // 利用規約/プライバシーポリシー/個人情報保護方針URL
    @Published var termsOfUseURL: String = Bundle.main.object(forInfoDictionaryKey: "TermsOfServiceURL") as! String
    @Published var privacyPolicyURL: String = Bundle.main.object(forInfoDictionaryKey: "PrivacyPolicyURL") as! String
    @Published var protectionPolicyURL: String = Bundle.main.object(forInfoDictionaryKey: "ProtectionPolicyURL") as! String
    // 問い合わせURL
    @Published var inquiryURL: String = Bundle.main.object(forInfoDictionaryKey: "InquiryURL") as! String

    var authenticationManager: AuthenticationManagerProtocol

    /// URLをopenするインスタンス
    /// - URLをopenするインスタンスとして、UIApplication.sharedを使う場合があり、
    /// アプリ起動直後にUIApplication.sharedが生成されていない場合があり、
    /// (https://forums.swift.org/t/uiapplication-shared-is-undefined-during-app-launch-but-it-works/60859 参照)
    /// AuthenticationController は、アプリ起動直後に生成される場合がありえるので、
    /// AuthenticationController 生成後、初回のURLをopenするタイミングで
    /// URLをopenするインスタンスを生成している
    private(set) lazy var urlOpener: URLOpenerProtocol = self.makeURLOpener()
    /// URLをopenするインスタンスを生成するクロージャ
    private let makeURLOpener: () -> (URLOpenerProtocol)

    convenience init() {
        self.init(authenticationManager: AuthenticationManager(),
                  makeURLOpener: { UIApplication.shared })
    }

    init(authenticationManager: AuthenticationManagerProtocol,
         makeURLOpener: @escaping () -> (URLOpenerProtocol))
    {
        self.authenticationManager = authenticationManager
        self.makeURLOpener = makeURLOpener

        self.authenticationManager
            .authenticationController = self
    }

    public func clear() {
        isLinkAlert = false
        isAlert = false
        messageTitle = ""
        messageString = ""
        isErrorOpenURL = false
    }

    /// 文字列として渡したURLを開く
    /// - URLとして不適切な文字列を渡した場合、何もしない
    func open(urlString: String) {
        guard let url = URL(string: urlString) else {
            return
        }

        urlOpener.openURL(url)
    }

    public func startReading(pin: String, nonce: String, actionURL: String) {
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
            let url = queryDict["error_url"]
            let replaceUrl = url!.replacingOccurrences(of: "&amp;", with: "&")
            openURL = replaceUrl
        }
    }

    public func onOpenURL(url: URL) {
        let urlComponents = URLComponents(url: url, resolvingAgainstBaseURL: true)
        queryDict = generateQueryDictionary(from: urlComponents!)
        viewState = viewState.isViewMode(queryDict: queryDict!)
        runMode = runMode.isMode(queryDict: queryDict!)
        queryDict = queryDict
        clear()
        setErrorPageURL(queryDict: queryDict!)

        if let actionURL = queryDict!["action_url"], let nonse = queryDict!["nonce"] {
            controllerForUserVerification.inputPIN = ""
            controllerForSignature.inputPIN = ""
            controllerForUserVerification.actionURL = actionURL
            controllerForSignature.actionURL = actionURL
            controllerForUserVerification.nonce = nonse
            controllerForSignature.nonce = nonse
        }
    }
}

extension AuthenticationController: AuthenticationControllerProtocol {}
