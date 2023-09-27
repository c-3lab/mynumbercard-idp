//
//  HTTPSessionProtocol.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/16.
//

import Foundation

/// @mockable(override: name = HTTPSessionMock)
protocol HTTPSessionProtocol {
    func openRedirectURLOnSafari(request: URLRequest)
}
