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

    private(set) var authenticateForSignatureCallCount = 0
    var authenticateForSignatureHandler: ((String, String, String, AuthenticationController) -> Void)?
    func authenticateForSignature(pin: String, nonce: String, actionURL: String, authenticationController: AuthenticationController) {
        authenticateForSignatureCallCount += 1
        if let authenticateForSignatureHandler = authenticateForSignatureHandler {
            authenticateForSignatureHandler(pin, nonce, actionURL, authenticationController)
        }
    }

    private(set) var authenticateForUserVerificationCallCount = 0
    var authenticateForUserVerificationHandler: ((String, String, String, AuthenticationController) -> Void)?
    func authenticateForUserVerification(pin: String, nonce: String, actionURL: String, authenticationController: AuthenticationController) {
        authenticateForUserVerificationCallCount += 1
        if let authenticateForUserVerificationHandler = authenticateForUserVerificationHandler {
            authenticateForUserVerificationHandler(pin, nonce, actionURL, authenticationController)
        }
    }
}
