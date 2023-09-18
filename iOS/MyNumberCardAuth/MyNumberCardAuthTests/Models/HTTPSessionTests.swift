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

    func testOpenRedirectURLOnSafariStatusCode400_500_503() throws {
        let doTest = { [weak self] (statusCode: Int) in
            guard let self = self else {
                XCTFail()
                return
            }
            let controller = AuthenticationControllerMock()
            guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                       urlString: "https://example.com",
                                                                       responseStatusCode: statusCode,
                                                                       responseHeaderFields: nil)
            else {
                XCTFail()
                return
            }
            ///  実行されないはずのexpectation達が満たすべき条件を反転する
            let fullfillingExpectationDescriptions = [
                "isAlertDidSet",
                "messageTitleDidSet",
                "messageStringDidSet",
            ]
            helper.expectations
                .filter {
                    !fullfillingExpectationDescriptions.contains($0.expectationDescription)
                }
                .forEach {
                    $0.isInverted = true
                }

            // ヘルパーが返してきたテストを実行する
            helper.doTest()

            // expectation達が条件を満たすのを待つ
            waitForExpectations(timeout: 0.3)
            // controllerのプロパティに値のAssertion
            XCTAssertTrue(controller.isAlert)
            XCTAssertFalse(controller.messageTitle.isEmpty)
            XCTAssertFalse(controller.messageString.isEmpty)
        }

        doTest(400)
        doTest(500)
        doTest(503)
    }

    /// 引数のcontollerを使ってHTTPSessionを生成し、
    /// 引数のurlStringへのリクエストを行うopenRedirectURLOnSafari(request:) を呼び出した場合、
    /// HTTPサーバーからのレスポンスが responseStatusCode, responseHeaderFields だとして、
    /// 実行すべきテストとcontrollerの主要なプロパティが変更されたことを示す
    /// XCTestExpectation達を返すヘルパーメソッド
    /// - 引数urlStringが不正な場合はnilを返す
    func helperForTestingOpenRedirectURLOnSafari(using controller: AuthenticationControllerMock,
                                                 urlString: String,
                                                 responseStatusCode statusCode: Int,
                                                 responseHeaderFields headerFields: [String: String]?)
        -> (doTest: () -> Void,
            expectations: [XCTestExpectation])?
    {
        guard let url = URL(string: urlString) else {
            XCTFail("urlString is invalid")
            return nil
        }

        let viewStateDidSet = expectation(description: "viewStateDidSet")
        let runModeDidSet = expectation(description: "runModeDidSet")
        let isAlertDidSet = expectation(description: "isAlertDidSet")
        let isLinkAlertDidSet = expectation(description: "isLinkAlertDidSet")
        let messageTitleDidSet = expectation(description: "messageTitleDidSet")
        let messageStringDidSet = expectation(description: "messageStringDidSet")
        let isErrorOpenURLDidSet = expectation(description: "isErrorOpenURLDidSet")
        let openURLCalled = expectation(description: "openURLCalled")
        let doTest = {
            var urlSessionMock: URLSessionMock?
            var urlSessionDataTaskMock: URLSessionDataTaskMock?
            var dataTaskCompletionHandler: ((Data?, URLResponse?, Error?) -> Void)?
            controller.viewStateDidSetHandler = { _ in
                viewStateDidSet.fulfill()
            }
            controller.runModeDidSetHandler = { _ in
                runModeDidSet.fulfill()
            }
            controller.isAlertDidSetHandler = { _ in
                isAlertDidSet.fulfill()
            }
            controller.isLinkAlertDidSetHandler = { _ in
                isLinkAlertDidSet.fulfill()
            }
            controller.messageTitleDidSetHandler = { _ in
                messageTitleDidSet.fulfill()
            }
            controller.messageStringDidSetHandler = { _ in
                messageStringDidSet.fulfill()
            }
            controller.isErrorOpenURLDidSetHandler = { _ in
                isErrorOpenURLDidSet.fulfill()
            }
            controller.openURLHandler = { _ in
                openURLCalled.fulfill()
            }
            let session = HTTPSession(authenticationController: controller,
                                      makeURLSession: { _, _, _ in
                                          urlSessionMock = URLSessionMock()
                                          urlSessionMock?.dataTaskHandler = { request, completionHandler in
                                              XCTAssertEqual(request.url?.absoluteString, urlString)
                                              XCTAssertEqual(request.httpMethod, "GET")
                                              dataTaskCompletionHandler = completionHandler
                                              urlSessionDataTaskMock = URLSessionDataTaskMock()
                                              urlSessionDataTaskMock?.resumeHandler = {
                                                  dataTaskCompletionHandler?(nil,
                                                                             HTTPURLResponse(url: url,
                                                                                             statusCode: statusCode,
                                                                                             httpVersion: nil,
                                                                                             headerFields: headerFields),
                                                                             nil)
                                              }
                                              return urlSessionDataTaskMock!
                                          }

                                          return urlSessionMock!
                                      })
            let request = URLRequest(url: url)

            session.openRedirectURLOnSafari(request: request)

            XCTAssertNotNil(urlSessionMock)
            XCTAssertEqual(urlSessionMock?.dataTaskCallCount, 1)
            XCTAssertNotNil(urlSessionDataTaskMock)
            XCTAssertEqual(urlSessionDataTaskMock?.resumeCallCount, 1)
        }
        return (doTest: doTest,
                expectations: [viewStateDidSet,
                               runModeDidSet,
                               isAlertDidSet,
                               isLinkAlertDidSet,
                               messageTitleDidSet,
                               messageStringDidSet,
                               isErrorOpenURLDidSet,
                               openURLCalled])
    }
}

extension HTTPSessionTests: URLSessionTaskDelegate {}
