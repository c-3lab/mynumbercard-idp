//
//  AuthenticationManagerProtocol.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/15.
//

import Foundation

/// @mockable(override: name = AuthenticationManagerMock)
protocol AuthenticationManagerProtocol {
    func authenticateForSignature(pin: String, nonce: String, actionURL: String, authenticationController: AuthenticationController)
    func authenticateForUserVerification(pin: String, nonce: String, actionURL: String, authenticationController: AuthenticationController)
}
