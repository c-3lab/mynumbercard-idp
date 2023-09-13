//
//  HTTPSessionTests.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/12.
//
@testable import MyNumberCardAuth
import SwiftUI
import XCTest

final class HTTPSessionTests: XCTestCase {
    let session: HTTPSession = .init(authenticationController: AuthenticationController())

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }
}
