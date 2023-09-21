//
//  URLSessionProtocol.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/16.
//

import Foundation

/// @mockable(override: name = URLSessionMock)
protocol URLSessionProtocol {
    func dataTask(with request: URLRequest,
                  completionHandler: @escaping @Sendable (Data?, URLResponse?, Error?) -> Void) -> URLSessionDataTaskProtocol
    func data(for request: URLRequest, delegate: URLSessionTaskDelegate?) async throws -> (Data, URLResponse)
}

extension URLSession: URLSessionProtocol {
    func dataTask(with request: URLRequest, completionHandler: @escaping @Sendable (Data?, URLResponse?, Error?) -> Void) -> URLSessionDataTaskProtocol {
        let ret: URLSessionDataTask = dataTask(with: request,
                                               completionHandler: completionHandler)
        return ret
    }
}
