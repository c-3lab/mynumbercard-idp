//
//  AuthenticationManagerProtocol.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/15.
//

import Foundation

/// @mockable(override: name = AuthenticationManagerMock)
protocol AuthenticationManagerProtocol {
    var authenticationController: AuthenticationControllerProtocol? { get set }

    func authenticateForSignature(pin: String, nonce: String, actionURL: String)
    func authenticateForUserVerification(pin: String, nonce: String, actionURL: String)
}
