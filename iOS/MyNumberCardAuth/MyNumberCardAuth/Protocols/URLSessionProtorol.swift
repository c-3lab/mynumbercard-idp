//
//  URLSessionProtorol.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/16.
//

import Foundation

/// @mockable(override: name = URLSessionMock)
protocol URLSessionProtorol {
    func dataTask(with request: URLRequest,
                  completionHandler: @escaping @Sendable (Data?, URLResponse?, Error?) -> Void) -> URLSessionDataTaskProtocol
}

extension URLSession: URLSessionProtorol {
    func dataTask(with request: URLRequest, completionHandler: @escaping @Sendable (Data?, URLResponse?, Error?) -> Void) -> URLSessionDataTaskProtocol {
        let ret: URLSessionDataTask = dataTask(with: request,
                                               completionHandler: completionHandler)
        return ret
    }
}
