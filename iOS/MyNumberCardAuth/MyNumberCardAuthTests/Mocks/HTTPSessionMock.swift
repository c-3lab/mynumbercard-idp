//
//  HTTPSessionMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/16.
//

import Foundation
@testable import MyNumberCardAuth

class HTTPSessionMock: HTTPSessionProtocol {
    init() { }

    private(set) var openRedirectURLOnSafariCallCount = 0
    var openRedirectURLOnSafariHandler: ((URLRequest) -> ())?
    func openRedirectURLOnSafari(request: URLRequest)  {
        openRedirectURLOnSafariCallCount += 1
        if let openRedirectURLOnSafariHandler = openRedirectURLOnSafariHandler {
            openRedirectURLOnSafariHandler(request)
        }
        
    }
}
