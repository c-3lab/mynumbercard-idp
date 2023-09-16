//
//  URLSessionMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/16.
//

import Foundation
@testable import MyNumberCardAuth

class URLSessionMock: URLSessionProtorol {
    init() { }

    private(set) var dataTaskCallCount = 0
    var dataTaskHandler: ((URLRequest, @escaping @Sendable (Data?, URLResponse?, Error?) -> Void) -> (URLSessionDataTaskProtocol))?
    func dataTask(with request: URLRequest, completionHandler: @escaping @Sendable (Data?, URLResponse?, Error?) -> Void) -> URLSessionDataTaskProtocol {
        dataTaskCallCount += 1
        if let dataTaskHandler = dataTaskHandler {
            return dataTaskHandler(request, completionHandler)
        }
        return URLSessionDataTaskMock()
    }
}
