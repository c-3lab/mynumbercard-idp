//
//  AuthenticationControllerMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/15.
//

import Foundation
@testable import MyNumberCardAuth
import SwiftUI

class AuthenticationControllerMock: AuthenticationControllerProtocol {
    init() {}

    private(set) var viewStateSetCallCount = 0
    private var _viewState: ShowView! { didSet { viewStateSetCallCount += 1 } }
    var viewStateSetHandler: ((ShowView) -> Void)?
    var viewState: ShowView {
        get { return _viewState }
        set {
            _viewState = newValue
            viewStateSetHandler?(viewState)
        }
    }

    private(set) var runModeSetCallCount = 0
    private var _runMode: Mode! { didSet { runModeSetCallCount += 1 } }
    var runModeSetHandler: ((Mode) -> Void)?
    var runMode: Mode {
        get { return _runMode }
        set {
            _runMode = newValue
            runModeSetHandler?(runMode)
        }
    }

    private(set) var isAlertSetCallCount = 0
    var isAlertSetHandler: ((Bool) -> Void)?
    var isAlert: Bool = false {
        didSet {
            isAlertSetCallCount += 1
            isAlertSetHandler?(isAlert)
        }
    }

    private(set) var isLinkAlertSetCallCount = 0
    var isLinkAlertSetHandler: ((Bool) -> Void)?
    var isLinkAlert: Bool = false {
        didSet {
            isLinkAlertSetCallCount += 1
            isLinkAlertSetHandler?(isLinkAlert)
        }
    }

    private(set) var messageTitleSetCallCount = 0
    var messageTitleSetHandler: ((String) -> Void)?
    var messageTitle: String = "" {
        didSet {
            messageTitleSetCallCount += 1
            messageTitleSetHandler?(messageString)
        }
    }

    private(set) var messageStringSetCallCount = 0
    var messageStringSetHandler: ((String) -> Void)?
    var messageString: String = "" {
        didSet {
            messageStringSetCallCount += 1
            messageStringSetHandler?(messageString)
        }
    }

    private(set) var isErrorOpenURLSetCallCount = 0
    var isErrorOpenURLSetHandler: ((Bool) -> Void)?
    var isErrorOpenURL: Bool = false {
        didSet {
            isErrorOpenURLSetCallCount += 1
            isErrorOpenURLSetHandler?(isErrorOpenURL)
        }
    }

    private(set) var nonceSetCallCount = 0
    var nonce: String = "" { didSet { nonceSetCallCount += 1 } }

    private(set) var queryDictSetCallCount = 0
    var queryDict: [String: String]? = nil { didSet { queryDictSetCallCount += 1 } }

    private(set) var openURLSetCallCount = 0
    var openURL: String = "" { didSet { openURLSetCallCount += 1 } }

    private(set) var controllerForUserVerificationSetCallCount = 0
    private var _controllerForUserVerification: UserVerificationViewController! { didSet { controllerForUserVerificationSetCallCount += 1 } }
    var controllerForUserVerification: UserVerificationViewController {
        get { return _controllerForUserVerification }
        set { _controllerForUserVerification = newValue }
    }

    private(set) var controllerForSignatureSetCallCount = 0
    private var _controllerForSignature: SignatureViewController! { didSet { controllerForSignatureSetCallCount += 1 } }
    var controllerForSignature: SignatureViewController {
        get { return _controllerForSignature }
        set { _controllerForSignature = newValue }
    }

    private(set) var termsOfUseURLSetCallCount = 0
    var termsOfUseURL: String = "" { didSet { termsOfUseURLSetCallCount += 1 } }

    private(set) var privacyPolicyURLSetCallCount = 0
    var privacyPolicyURL: String = "" { didSet { privacyPolicyURLSetCallCount += 1 } }

    private(set) var protectionPolicyURLSetCallCount = 0
    var protectionPolicyURL: String = "" { didSet { protectionPolicyURLSetCallCount += 1 } }

    private(set) var inquiryURLSetCallCount = 0
    var inquiryURL: String = "" { didSet { inquiryURLSetCallCount += 1 } }

    private(set) var clearCallCount = 0
    var clearHandler: (() -> Void)?
    func clear() {
        clearCallCount += 1
        if let clearHandler = clearHandler {
            clearHandler()
        }
    }

    private(set) var openCallCount = 0
    var openHandler: ((String) -> Void)?
    func open(urlString: String) {
        openCallCount += 1
        if let openHandler = openHandler {
            openHandler(urlString)
        }
    }

    private(set) var startReadingCallCount = 0
    var startReadingHandler: ((String, String, String) -> Void)?
    func startReading(pin: String, nonce: String, actionURL: String) {
        startReadingCallCount += 1
        if let startReadingHandler = startReadingHandler {
            startReadingHandler(pin, nonce, actionURL)
        }
    }

    private(set) var getButtonColorCallCount = 0
    var getButtonColorHandler: ((String) -> (Color))?
    func getButtonColor(checkStr: String) -> Color {
        getButtonColorCallCount += 1
        if let getButtonColorHandler = getButtonColorHandler {
            return getButtonColorHandler(checkStr)
        }
        fatalError("getButtonColorHandler returns can't have a default value thus its handler must be set")
    }

    private(set) var setErrorPageURLCallCount = 0
    var setErrorPageURLHandler: (([String: String]) -> Void)?
    func setErrorPageURL(queryDict: [String: String]) {
        setErrorPageURLCallCount += 1
        if let setErrorPageURLHandler = setErrorPageURLHandler {
            setErrorPageURLHandler(queryDict)
        }
    }

    private(set) var onOpenURLCallCount = 0
    var onOpenURLHandler: ((URL) -> Void)?
    func onOpenURL(url: URL) {
        onOpenURLCallCount += 1
        if let onOpenURLHandler = onOpenURLHandler {
            onOpenURLHandler(url)
        }
    }
}
