//
//  AuthenticationControllerProtocol.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/15.
//

import Foundation
import SwiftUI

/// @mockable(override: name = AuthenticationControllerMock)
protocol AuthenticationControllerProtocol: AnyObject {
    var viewState: ShowView { get set }
    var runMode: Mode { get set }
    var isAlert: Bool { get set }
    var isLinkAlert: Bool { get set }
    var messageTitle: String { get set }
    var messageString: String { get set }
    var isErrorOpenURL: Bool { get set }
    var nonce: String { get set }
    var queryDict: [String: String]? { get set }
    var openURL: String { get set }
    var controllerForUserVerification: UserVerificationViewController { get set }
    var controllerForSignature: SignatureViewController { get set }
    var termsOfUseURL: String { get set }
    var privacyPolicyURL: String { get set }
    var protectionPolicyURL: String { get set }
    var inquiryURL: String { get set }

    func clear()
    func openURL(string: String)
    func startReading(pin: String, nonce: String, actionURL: String)
    func getButtonColor(checkStr: String) -> Color
    func setErrorPageURL(queryDict: [String: String])
    func onOpenURL(url: URL)
}
