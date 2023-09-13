//
//  SignatureViewController.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/04/26.
//

import Foundation
import SwiftUI

public class SignatureViewController: ObservableObject {
    @Published var inputPIN: String = ""
    @Published var nonce: String = ""
    @Published var actionURL: String = ""

    public func isEnableButton() -> Bool {
        return !nonce.isEmpty && !actionURL.isEmpty && isValidPin()
    }

    public func getButtonColor() -> Color {
        if isEnableButton() {
            return Color(UIColor.blue)
        } else {
            return Color(UIColor.lightGray)
        }
    }

    private func isValidPin() -> Bool {
        if inputPIN.count >= 6 {
            return true
        } else {
            return false
        }
    }
}
