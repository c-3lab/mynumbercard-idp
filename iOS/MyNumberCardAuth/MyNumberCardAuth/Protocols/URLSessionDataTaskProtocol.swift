//
//  URLSessionDataTaskProtocol.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/16.
//

import Foundation

/// @mockable(override: name = URLSessionDataTaskMock)
protocol URLSessionDataTaskProtocol {
    func resume()
}

extension URLSessionDataTask: URLSessionDataTaskProtocol {}
