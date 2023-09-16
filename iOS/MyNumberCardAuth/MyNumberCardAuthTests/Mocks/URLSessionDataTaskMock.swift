//
//  URLSessionDataTaskMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/16.
//

import Foundation
@testable import MyNumberCardAuth

class URLSessionDataTaskMock: URLSessionDataTaskProtocol {
    init() { }

    private(set) var resumeCallCount = 0
    var resumeHandler: (() -> ())?
    func resume()  {
        resumeCallCount += 1
        if let resumeHandler = resumeHandler {
            resumeHandler()
        }        
    }
}
