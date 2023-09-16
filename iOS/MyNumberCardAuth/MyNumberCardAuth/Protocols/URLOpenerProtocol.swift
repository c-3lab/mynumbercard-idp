//
//  URLOpenerProtocol.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/16.
//

import Foundation
import UIKit.UIApplication

/// @mockable(override: name = URLOpenerMock)
protocol URLOpenerProtocol {
    func openURL(_ url: URL)
}

extension UIApplication : URLOpenerProtocol {
    func openURL(_ url: URL) {
        self.open(url,
                  options: [:],
                  completionHandler: nil)
    }
}
