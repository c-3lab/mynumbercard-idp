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
    func open(_ url: URL)
}

extension UIApplication: URLOpenerProtocol {
    func open(_ url: URL) {
        open(url,
             options: [:],
             completionHandler: nil)
    }
}
