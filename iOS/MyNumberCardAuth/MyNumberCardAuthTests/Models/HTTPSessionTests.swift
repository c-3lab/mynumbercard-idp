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

    func testOpenRedirectURLOnSafariError() throws {
        let controller = AuthenticationControllerMock()
        let error: Error? = NSError(domain: "NSURLErrorDomain", code: NSURLErrorCannotFindHost)
        guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                   urlString: "https://cannotfindhost.co.jp",
                                                                   responseStatusCode: nil,
                                                                   responseHeaderFields: nil,
                                                                   responseError: error)
        else {
            XCTFail()
            return
        }
        ///  実行されないはずのexpectation達が満たすべき条件を反転する
        helper.expectations
            .forEach {
                $0.isInverted = true
            }

        // ヘルパーが返してきたテストを実行する
        helper.doTest()

        // expectation達が条件を満たすのを待つ
        waitForExpectations(timeout: 0.3)
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
                                                                       responseHeaderFields: nil,
                                                                       responseError: nil)
            else {
                XCTFail()
                return
            }
            ///  実行されないはずのexpectation達が満たすべき条件を反転する
            let fulfillingExpectationDescriptions = [
                "isAlertDidSet",
                "messageTitleDidSet",
                "messageStringDidSet",
            ]
            helper.expectations
                .filter {
                    !fulfillingExpectationDescriptions.contains($0.expectationDescription)
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
            XCTAssertEqual(controller.messageTitle, String(localized: "Failure", comment: "失敗"))
            XCTAssertEqual(controller.messageString,
                           String(localized: "An unexpected error has occurred.", comment: "予期せぬエラーが発生しました。"))
        }

        doTest(400)
        doTest(500)
        doTest(503)
    }

    func testOpenRedirectURLOnSafariStatusCode404() throws {
        let controller = AuthenticationControllerMock()
        guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                   urlString: "https://example.com/ARIENAI",
                                                                   responseStatusCode: 404,
                                                                   responseHeaderFields: nil,
                                                                   responseError: nil)
        else {
            XCTFail()
            return
        }
        ///  実行されないはずのexpectation達が満たすべき条件を反転する
        let fulfillingExpectationDescriptions = [
            "isAlertDidSet",
            "isErrorOpenURLDidSet",
            "messageTitleDidSet",
            "messageStringDidSet",
        ]
        helper.expectations
            .filter {
                !fulfillingExpectationDescriptions.contains($0.expectationDescription)
            }
            .forEach {
                $0.isInverted = true
            }

        // ヘルパーが返してきたテストを実行する
        helper.doTest()

        // expectation達が条件を満たすのを待つ
        waitForExpectations(timeout: 0.3)
        // controllerの中身のAssertion
        XCTAssertTrue(controller.isAlert)
        XCTAssertTrue(controller.isErrorOpenURL)
        XCTAssertEqual(controller.messageTitle, String(localized: "Authentication failure", comment: "認証失敗"))
        XCTAssertEqual(controller.messageString,
                       String(localized: "Since you have not registered as a user, please register as a user.",
                              comment: "ユーザー未登録のため、利用者登録を行ってください。"))
    }

    func testOpenRedirectURLOnSafariStatusCode401Login() throws {
        let controller = AuthenticationControllerMock()
        controller.runMode = .Login
        guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                   urlString: "https://example.com",
                                                                   responseStatusCode: 401,
                                                                   responseHeaderFields: nil,
                                                                   responseError: nil)
        else {
            XCTFail()
            return
        }
        ///  実行されないはずのexpectation達が満たすべき条件を反転する
        let fulfillingExpectationDescriptions = [
            "isLinkAlertDidSet",
            "messageTitleDidSet",
            "messageStringDidSet",
        ]
        helper.expectations
            .filter {
                !fulfillingExpectationDescriptions.contains($0.expectationDescription)
            }
            .forEach {
                $0.isInverted = true
            }

        // ヘルパーが返してきたテストを実行する
        helper.doTest()

        // expectation達が条件を満たすのを待つ
        waitForExpectations(timeout: 0.3)
        // controllerの中身のAssertion
        XCTAssertTrue(controller.isLinkAlert)
        XCTAssertEqual(controller.messageString,
                       String(localized: "The electronic certificate for user certification has been revoked.",
                              comment: "利用者証明用電子証明書が失効しています。") +
                           String(localized: "Please contact the window of your municipality, or contact the window of the My Number Card Comprehensive Site from the link below.",
                                  comment: "お住まいの市区町村の窓口へお問い合わせください。"))
    }

    func testOpenRedirectURLOnSafariStatusCode401() throws {
        let doTest = { [weak self] (runMode: Mode) in
            guard let self = self else {
                XCTFail()
                return
            }

            let controller = AuthenticationControllerMock()
            controller.runMode = runMode
            guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                       urlString: "https://example.com",
                                                                       responseStatusCode: 401,
                                                                       responseHeaderFields: nil,
                                                                       responseError: nil)
            else {
                XCTFail()
                return
            }
            ///  実行されないはずのexpectation達が満たすべき条件を反転する
            let fulfillingExpectationDescriptions = [
                "isLinkAlertDidSet",
                "messageTitleDidSet",
                "messageStringDidSet",
            ]
            helper.expectations
                .filter {
                    !fulfillingExpectationDescriptions.contains($0.expectationDescription)
                }
                .forEach {
                    $0.isInverted = true
                }

            // ヘルパーが返してきたテストを実行する
            helper.doTest()

            // expectation達が条件を満たすのを待つ
            waitForExpectations(timeout: 0.3)
            // controllerの中身のAssertion
            XCTAssertTrue(controller.isLinkAlert)
            XCTAssertEqual(controller.messageString,
                           String(localized: "The electronic signature certificate has expired.",
                                  comment: "署名用電子証明書が失効しています。") +
                               String(localized: "Please contact the window of your municipality, or contact the window of the My Number Card Comprehensive Site from the link below.",
                                      comment: "お住まいの市区町村の窓口へお問い合わせください。"))
        }

        doTest(.Registration)
        doTest(.Replacement)
    }

    func testOpenRedirectURLOnSafariStatusCode409() throws {
        let controller = AuthenticationControllerMock()
        guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                   urlString: "https://example.com",
                                                                   responseStatusCode: 409,
                                                                   responseHeaderFields: nil,
                                                                   responseError: nil)
        else {
            XCTFail()
            return
        }
        ///  実行されないはずのexpectation達が満たすべき条件を反転する
        let fulfillingExpectationDescriptions = [
            "isAlertDidSet",
            "isErrorOpenURLDidSet",
            "messageTitleDidSet",
            "messageStringDidSet",
        ]
        helper.expectations
            .filter {
                !fulfillingExpectationDescriptions.contains($0.expectationDescription)
            }
            .forEach {
                $0.isInverted = true
            }

        // ヘルパーが返してきたテストを実行する
        helper.doTest()

        // expectation達が条件を満たすのを待つ
        waitForExpectations(timeout: 0.3)
        // controllerの中身のAssertion
        XCTAssertTrue(controller.isAlert)
        XCTAssertTrue(controller.isErrorOpenURL)
        XCTAssertEqual(controller.messageTitle, String(localized: "Registration failed", comment: "登録失敗"))
        XCTAssertEqual(controller.messageString,
                       String(localized: "Since the user is already registered, please log in.",
                              comment: "既にユーザーが登録されているため、ログインを行ってください。"))
    }

    func testOpenRedirectURLOnSafariStatusCode410() throws {
        let doTest = { [weak self] (xActionUrlString: String?) in
            guard let self = self else {
                XCTFail()
                return
            }

            let controller = AuthenticationControllerMock()
            controller.controllerForSignature = SignatureViewController()
            let header: [String: String]?
            if let xActionUrlString = xActionUrlString {
                header = ["x-action-url": xActionUrlString,
                          "status": "410"]
            } else {
                header = nil
            }
            guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                       urlString: "https://example.com",
                                                                       responseStatusCode: 410,
                                                                       responseHeaderFields: header,
                                                                       responseError: nil)
            else {
                XCTFail()
                return
            }
            ///  実行されないはずのexpectation達が満たすべき条件を反転する
            let fulfillingExpectationDescriptions = [
                "viewStateDidSet",
                "runModeDidSet",
                "isAlertDidSet",
                "messageTitleDidSet",
                "messageStringDidSet",
            ]
            helper.expectations
                .filter {
                    !fulfillingExpectationDescriptions.contains($0.expectationDescription)
                }
                .forEach {
                    $0.isInverted = true
                }

            // ヘルパーが返してきたテストを実行する
            helper.doTest()

            // expectation達が条件を満たすのを待つ
            waitForExpectations(timeout: 0.3)
            // controllerの中身のAssertion
            XCTAssertEqual(controller.viewState, .SignatureView)
            XCTAssertEqual(controller.runMode, .Replacement)
            XCTAssertTrue(controller.isAlert)
            XCTAssertEqual(controller.messageTitle,
                           String(localized: "Reloading My Number Card", comment: "マイナンバーカードの再読み込み"))
            XCTAssertEqual(controller.messageString,
                           String(localized: "I need to change my registration information. Please read the signature digital certificate to make changes.",
                                  comment: "登録情報の変更が必要です。変更を行うため署名用電子証明書の読み取りを行ってください。"))
        }

        doTest(nil)
        doTest("https://example.com/xaction")
    }

    func testOpenRedirectURLOnSafariStatusCode301() throws {
        let controller = AuthenticationControllerMock()
        let header = ["Location": "https://example.com/redirect",
                      "status": "301"]
        guard let helper = helperForTestingOpenRedirectURLOnSafari(using: controller,
                                                                   urlString: "https://example.com",
                                                                   responseStatusCode: 301,
                                                                   responseHeaderFields: header,
                                                                   responseError: nil)
        else {
            XCTFail()
            return
        }
        ///  実行されないはずのexpectation達が満たすべき条件を反転する
        helper.expectations
            .forEach {
                $0.isInverted = true
            }
        // helperが用意するopenURLHandlerを使わず、
        // 本メソッド内で用意する
        // (helperが用意しているopenURLHandlerだと、引数を取れないため)
        var openURLString: String?
        let expectation = self.expectation(description: "openURLHandler")
        controller.openHandler = { urlString in
            openURLString = urlString
            expectation.fulfill()
        }

        // ヘルパーが返してきたテストを実行する
        helper.doTest()

        // expectation達が条件を満たすのを待つ
        var expectations = helper.expectations
        expectations.append(expectation)
        wait(for: expectations, timeout: 0.3)
        // controllerの中身のAssertion
        XCTAssertEqual(openURLString, "https://example.com/redirect")
    }

    /// 引数のcontollerを使ってHTTPSessionを生成し、
    /// 引数のurlStringへのリクエストを行うopenRedirectURLOnSafari(request:) を呼び出した場合、
    /// HTTPサーバーからのレスポンスが responseStatusCode, responseHeaderFields, responseError だとして、
    /// 実行すべきテストとcontrollerの主要なプロパティが変更されたことを示す
    /// XCTestExpectation達を返すヘルパーメソッド
    /// - 引数urlStringが不正な場合はnilを返す
    func helperForTestingOpenRedirectURLOnSafari(using controller: AuthenticationControllerMock,
                                                 urlString: String,
                                                 responseStatusCode statusCode: Int?,
                                                 responseHeaderFields headerFields: [String: String]?,
                                                 responseError: Error?)
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
            let urlSessionMock = URLSessionMock(delegate: nil)
            let urlSessionDataTaskMock = URLSessionDataTaskMock()
            controller.viewStateSetHandler =
                controller.viewStateSetHandler ?? { _ in
                    viewStateDidSet.fulfill()
                }
            controller.runModeSetHandler =
                controller.runModeSetHandler ?? { _ in
                    runModeDidSet.fulfill()
                }
            controller.isAlertSetHandler =
                controller.isAlertSetHandler ?? { _ in
                    isAlertDidSet.fulfill()
                }
            controller.isLinkAlertSetHandler =
                controller.isLinkAlertSetHandler ?? { _ in
                    isLinkAlertDidSet.fulfill()
                }
            controller.messageTitleSetHandler =
                controller.messageTitleSetHandler ?? { _ in
                    messageTitleDidSet.fulfill()
                }
            controller.messageStringSetHandler =
                controller.messageStringSetHandler ?? { _ in
                    messageStringDidSet.fulfill()
                }
            controller.isErrorOpenURLSetHandler =
                controller.isErrorOpenURLSetHandler ?? { _ in
                    isErrorOpenURLDidSet.fulfill()
                }
            controller.openHandler =
                controller.openHandler ?? { _ in
                    openURLCalled.fulfill()
                }
            var session: HTTPSession!
            session = HTTPSession(authenticationController: controller,
                                  makeURLSession: { _, _, _ in
                                      urlSessionMock.dataTaskHandler = { request, completionHandler in
                                          XCTAssertEqual(request.url?.absoluteString, urlString)
                                          XCTAssertEqual(request.httpMethod, "GET")
                                          urlSessionDataTaskMock.resumeHandler = {
                                              if [301,
                                                  302,
                                                  303,
                                                  307,
                                                  308].contains(statusCode),
                                                  let location = headerFields?["Location"],
                                                  let newUrl = URL(string: location)
                                              {
                                                  let newRequest = URLRequest(url: newUrl)
                                                  var completedURLRequest: URLRequest? = newRequest
                                                  session.urlSession(URLSession.shared,
                                                                     task: URLSessionTask(),
                                                                     willPerformHTTPRedirection: HTTPURLResponse(),
                                                                     newRequest: newRequest)
                                                  {
                                                      completedURLRequest = $0
                                                  }
                                                  XCTAssertNil(completedURLRequest)
                                              }

                                              let httpResponse = statusCode.flatMap {
                                                  HTTPURLResponse(url: url,
                                                                  statusCode: $0,
                                                                  httpVersion: nil,
                                                                  headerFields: headerFields ?? ["status": "\($0)"])
                                              }
                                              completionHandler(nil,
                                                                httpResponse,
                                                                responseError)
                                          }
                                          return urlSessionDataTaskMock
                                      }

                                      return urlSessionMock
                                  })
            let request = URLRequest(url: url)

            session.openRedirectURLOnSafari(request: request)

            XCTAssertEqual(urlSessionMock.dataTaskCallCount, 1)
            XCTAssertEqual(urlSessionDataTaskMock.resumeCallCount, 1)
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
