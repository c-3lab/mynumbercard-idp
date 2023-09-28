//
//  AuthenticationManagerMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/15.
//

import Foundation
@testable import MyNumberCardAuth

class AuthenticationManagerMock: AuthenticationManagerProtocol {
    init() {}
    init(authenticationController: AuthenticationControllerProtocol) {
        self.authenticationController = authenticationController
    }

    private(set) var authenticationControllerSetCallCount = 0
    var authenticationControllerStringSetHandler: ((Int) -> Void)?
    var authenticationController: AuthenticationControllerProtocol? = nil {
        didSet {
            authenticationControllerSetCallCount += 1
            authenticationControllerStringSetHandler?(authenticationControllerSetCallCount)
        }
    }

    private(set) var authenticateForSignatureCallCount = 0
    var authenticateForSignatureHandler: ((String, String, String) -> Void)?
    func authenticateForSignature(pin: String, nonce: String, actionURL: String) {
        authenticateForSignatureCallCount += 1
        if let authenticateForSignatureHandler = authenticateForSignatureHandler {
            authenticateForSignatureHandler(pin, nonce, actionURL)
        }
    }

    private(set) var authenticateForUserVerificationCallCount = 0
    var authenticateForUserVerificationHandler: ((String, String, String) -> Void)?
    func authenticateForUserVerification(pin: String, nonce: String, actionURL: String) {
        authenticateForUserVerificationCallCount += 1
        if let authenticateForUserVerificationHandler = authenticateForUserVerificationHandler {
            authenticateForUserVerificationHandler(pin, nonce, actionURL)
        }
    }
}
