//
//  AuthenticationManagerMock.swift
//  MyNumberCardAuthTests
//
//  Created by abelstaff on 2023/09/15.
//

import Foundation
@testable import MyNumberCardAuth

public class AuthenticationManagerMock: AuthenticationManagerProtocol {
    var barCallCount = 0
    var pin = ""
    var nonce = ""
    var actionURL = ""

    public func authenticateForSignature(pin: String, nonce: String, actionURL: String, authenticationController _: AuthenticationController) {
        self.pin = pin
        self.nonce = nonce
        self.actionURL = actionURL
        barCallCount += 1
    }

    public func authenticateForUserVerification(pin: String, nonce: String, actionURL: String, authenticationController _: AuthenticationController) {
        self.pin = pin
        self.nonce = nonce
        self.actionURL = actionURL
        barCallCount += 1
    }
}
