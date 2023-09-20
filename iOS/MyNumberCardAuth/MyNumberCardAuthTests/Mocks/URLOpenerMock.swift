//
//  URLOpenerMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/16.
//

import Foundation
@testable import MyNumberCardAuth

class URLOpenerMock: URLOpenerProtocol {
    init() {}

    private(set) var openCallCount = 0
    var openHandler: ((URL) -> Void)?
    func open(_ url: URL) {
        openCallCount += 1
        if let openHandler = openHandler {
            openHandler(url)
        }
    }
}
