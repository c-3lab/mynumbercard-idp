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

    func testOpenRedirectURLOnSafariStatusCode400() throws {
        var urlSessionMock: URLSessionMock?
        var urlSessionDataTaskMock: URLSessionDataTaskMock?
        var dataTaskCompletionHandler: ((Data?, URLResponse?, Error?) -> Void)?
        let controllerMock = AuthenticationControllerMock()
        let controllerMessageStringDidSetExpectation = expectation(description: "controllermessageStringDidSet")
        controllerMock.messageStringDidSetHandler = { [weak controllerMock] _ in
            guard let controllerMock = controllerMock else {
                return
            }
            XCTAssertTrue(controllerMock.isAlert)
            XCTAssertFalse(controllerMock.messageTitle.isEmpty)
            XCTAssertFalse(controllerMock.messageString.isEmpty)
            controllerMessageStringDidSetExpectation.fulfill()
        }
        let session = HTTPSession(authenticationController: controllerMock,
                                  makeURLSession: { _, _, _ in
                                      urlSessionMock = URLSessionMock()
                                      urlSessionMock?.dataTaskHandler = { request, completionHandler in
                                          XCTAssertEqual(request.url, URL(string: "https://example.com"))
                                          XCTAssertEqual(request.httpMethod, "GET")
                                          dataTaskCompletionHandler = completionHandler
                                          urlSessionDataTaskMock = URLSessionDataTaskMock()
                                          urlSessionDataTaskMock?.resumeHandler = {
                                              dataTaskCompletionHandler?(nil,
                                                                         HTTPURLResponse(url: URL(string: "https://example.com")!,
                                                                                         statusCode: 400,
                                                                                         httpVersion: nil,
                                                                                         headerFields: nil),
                                                                         nil)
                                          }
                                          return urlSessionDataTaskMock!
                                      }

                                      return urlSessionMock!
                                  })
        let request = URLRequest(url: URL(string: "https://example.com")!)

        session.openRedirectURLOnSafari(request: request)

        XCTAssertNotNil(urlSessionMock)
        XCTAssertEqual(urlSessionMock?.dataTaskCallCount, 1)
        XCTAssertNotNil(urlSessionDataTaskMock)
        XCTAssertEqual(urlSessionDataTaskMock?.resumeCallCount, 1)
        wait(for: [controllerMessageStringDidSetExpectation], timeout: 0.3)
    }
}

extension HTTPSessionTests: URLSessionTaskDelegate {}
