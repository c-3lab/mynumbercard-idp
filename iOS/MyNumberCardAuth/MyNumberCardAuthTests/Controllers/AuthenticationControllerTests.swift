//
//  AuthenticationControllerTests.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/10.
//
@testable import MyNumberCardAuth
import SwiftUI
import XCTest

final class AuthenticationControllerTests: XCTestCase {
    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testClear() throws {
        let controller = AuthenticationController()

        controller.isAlert = true
        controller.isLinkAlert = true
        controller.isErrorOpenURL = true
        controller.messageTitle = "test"
        controller.messageString = "test"
        controller.clear()

        XCTAssertFalse(controller.isAlert)
        XCTAssertFalse(controller.isLinkAlert)
        XCTAssertFalse(controller.isErrorOpenURL)
        XCTAssertEqual(controller.messageTitle, "")
        XCTAssertEqual(controller.messageString, "")
    }

    func testOpenURLButton() throws {
        let controller = AuthenticationController()

        // mock必要
        controller.openURLButton(url: "https://example.com")
    }

    func testOpenURLButtonURLEmpty() throws {
        let controller = AuthenticationController()

        controller.openURLButton(url: "")
    }

    func testOpenURLButtonNoURL() throws {
        let controller = AuthenticationController()

        controller.openURLButton(url: "てすと")
    }

    func testStartReadingViewStateIsSignatureView() throws {
        let mock = AuthenticationManagerMock()
        let controller = AuthenticationController(authenticationManager: mock)
        var handlerCalled = false
        mock.authenticateForSignatureHandler = {
            XCTAssertEqual($0, "1234")
            XCTAssertEqual($1, "0123456789")
            XCTAssertEqual($2, "https:example.1")
            XCTAssertTrue($3 === controller)
            handlerCalled = true
        }

        controller.viewState = .SignatureView
        controller.startReading(pin: "1234", nonce: "0123456789", actionURL: "https:example.1")

        XCTAssertEqual(mock.authenticateForSignatureCallCount, 1)
        XCTAssertEqual(mock.authenticateForUserVerificationCallCount, 0)
        XCTAssertTrue(handlerCalled)
    }

    func testStartReadingViewStateIsUserVerificationView() throws {
        let mock = AuthenticationManagerMock()
        let controller = AuthenticationController(authenticationManager: mock)
        var handlerCalled = false
        mock.authenticateForUserVerificationHandler = {
            XCTAssertEqual($0, "5678")
            XCTAssertEqual($1, "9876543210")
            XCTAssertEqual($2, "https:example.2")
            XCTAssertTrue($3 === controller)
            handlerCalled = true
        }

        controller.viewState = .UserVerificationView
        controller.startReading(pin: "5678", nonce: "9876543210", actionURL: "https:example.2")

        XCTAssertEqual(mock.authenticateForSignatureCallCount, 0)
        XCTAssertEqual(mock.authenticateForUserVerificationCallCount, 1)
        XCTAssertTrue(handlerCalled)
    }

    func testStartReadingViewStateIsExplanationView() throws {
        let mock = AuthenticationManagerMock()
        let controller = AuthenticationController(authenticationManager: mock)
        var handlerCalled = false
        mock.authenticateForSignatureHandler = { _, _, _, _ in
            handlerCalled = true
        }

        controller.viewState = .ExplanationView
        controller.startReading(pin: "9012", nonce: "9876501234", actionURL: "https:example.3")

        XCTAssertEqual(mock.authenticateForSignatureCallCount, 0)
        XCTAssertEqual(mock.authenticateForUserVerificationCallCount, 0)
        XCTAssertFalse(handlerCalled)
    }

    func testGetButtonColor() throws {
        let controller = AuthenticationController()
        XCTAssertEqual(controller.getButtonColor(checkStr: "example"), Color(UIColor.blue))
    }

    func testGetButtonColorEmpty() throws {
        let controller = AuthenticationController()
        XCTAssertEqual(controller.getButtonColor(checkStr: ""), Color(UIColor.lightGray))
    }

    func testSetErrorPageURLQueryDicNil() throws {
        let controller = AuthenticationController()
        controller.setErrorPageURL(queryDict: ["": ""])
        XCTAssertEqual(controller.openURL, "")
    }

    func testSetErrorPageURLNoErrorURL() throws {
        let controller = AuthenticationController()
        controller.setErrorPageURL(queryDict: ["error_url": ""])
        XCTAssertEqual(controller.openURL, "")
    }

    func testSetErrorPageURL() throws {
        let controller = AuthenticationController()
        controller.setErrorPageURL(queryDict: ["error_url": "https://example/?test=1&amp;test2=1"])
        XCTAssertEqual(controller.openURL, "https://example/?test=1&test2=1")
    }

    func testOnOpenURLEmptyNonceEmpty() throws {
        let controller = AuthenticationController()
        controller.onOpenURL(url: URL(string: "http://example?action_url=example")!)
        XCTAssertEqual(controller.controllerForUserVerification.actionURL, "")
        XCTAssertEqual(controller.controllerForUserVerification.nonce, "")
        XCTAssertEqual(controller.controllerForSignature.actionURL, "")
        XCTAssertEqual(controller.controllerForSignature.nonce, "")
    }

    func testOnOpenURLEmptyActionURLEmpty() throws {
        let controller = AuthenticationController()
        controller.onOpenURL(url: URL(string: "http://example?nonce=example")!)
        XCTAssertEqual(controller.controllerForUserVerification.actionURL, "")
        XCTAssertEqual(controller.controllerForUserVerification.nonce, "")
        XCTAssertEqual(controller.controllerForSignature.actionURL, "")
        XCTAssertEqual(controller.controllerForSignature.nonce, "")
    }

    func testOnOpenURL() throws {
        let controller = AuthenticationController()
        controller.onOpenURL(url: URL(string: "https://example?action_url=example&nonce=1234")!)
        XCTAssertEqual(controller.controllerForUserVerification.actionURL, "example")
        XCTAssertEqual(controller.controllerForUserVerification.nonce, "1234")
        XCTAssertEqual(controller.controllerForSignature.actionURL, "example")
        XCTAssertEqual(controller.controllerForSignature.nonce, "1234")
    }
}
