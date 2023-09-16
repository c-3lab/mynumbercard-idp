//
//  URLOpenerMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/16.
//

import Foundation
@testable import MyNumberCardAuth

class URLOpenerMock: URLOpenerProtocol {
    init() { }

    private(set) var openURLCallCount = 0
    var openURLHandler: ((URL) -> ())?
    func openURL(_ url: URL)  {
        openURLCallCount += 1
        if let openURLHandler = openURLHandler {
            openURLHandler(url)
        }
    }
}
