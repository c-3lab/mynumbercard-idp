//
//  AuthenticationControllerTests.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/10.
//
import XCTest
@testable import MyNumberCardAuth
import SwiftUI

public class AuthenticationManagerMock: AuthenticationManagerProtocol {
    var barCallCount = 0
    var pin = ""
    var nonce = ""
    var actionURL = ""
    
    public func authenticateForSignature(pin: String, nonce: String, actionURL: String, authenticationController:AuthenticationController) {
        self.pin = pin
        self.nonce = nonce
        self.actionURL = actionURL
        barCallCount += 1
    }
    
    public func authenticateForUserVerification(pin: String, nonce: String, actionURL: String, authenticationController:AuthenticationController) {
        self.pin = pin
        self.nonce = nonce
        self.actionURL = actionURL
        barCallCount += 1
    }
}

final class AuthenticationControllerTests: XCTestCase {

    let controller:AuthenticationController = AuthenticationController()
    
    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testclear() throws {
        controller.isAlert = true
        controller.isLinkAlert = true
        controller.isErrorOpenURL = true
        controller.messageTitle = "test"
        controller.messageString = "test"
        controller.clear()
        
        XCTAssertFalse(controller.isAlert)
        XCTAssertFalse(controller.isLinkAlert)
        XCTAssertFalse(controller.isErrorOpenURL)
        XCTAssertEqual(controller.messageTitle,"")
        XCTAssertEqual(controller.messageString,"")
    }
    
    func testOpenURLButton() throws {
        // mock必要
        controller.openURLButton(url:"https://example.com")
    }
    
    func testOpenURLButtonURLEmpty() throws {
        controller.openURLButton(url:"")
    }
    
    func testOpenURLButtonNoURL() throws {
        controller.openURLButton(url:"てすと")
    }

    func testStartReading()throws {
        let mock = AuthenticationManagerMock()
        let authenticationController = AuthenticationController(authenticationManager: mock)
            
        authenticationController.viewState = .SignatureView
        authenticationController.startReading(pin: "1234", nonce: "0123456789", actionURL: "https:example.1")
        
        XCTAssertEqual(mock.barCallCount,1)
        XCTAssertEqual(mock.pin,"1234")
        XCTAssertEqual(mock.nonce,"0123456789")
        XCTAssertEqual(mock.actionURL,"https:example.1")
        
        authenticationController.viewState = .UserVerificationView
        authenticationController.startReading(pin: "5678", nonce: "9876543210", actionURL: "https:example.2")
        
        XCTAssertEqual(mock.barCallCount,2)
        XCTAssertEqual(mock.pin,"5678")
        XCTAssertEqual(mock.nonce,"9876543210")
        XCTAssertEqual(mock.actionURL,"https:example.2")
        
        authenticationController.viewState = .ExplanationView
        authenticationController.startReading(pin: "test", nonce: "nonce", actionURL: "https:example.3")
        
        XCTAssertEqual(mock.barCallCount,2)
        XCTAssertEqual(mock.pin,"5678")
        XCTAssertEqual(mock.nonce,"9876543210")
        XCTAssertEqual(mock.actionURL,"https:example.2")
    }
    
    func testGetButtonColor() throws {
        XCTAssertEqual(controller.getButtonColor(checkStr: "example"), Color(UIColor.blue))
    }
    
    func testGetButtonColorEmpty() throws {
        XCTAssertEqual(controller.getButtonColor(checkStr: ""), Color(UIColor.lightGray))
    }
    
    func testSetErrorPageURLQueryDicNil() throws {
        controller.setErrorPageURL(queryDict: ["":""])
        XCTAssertEqual(controller.openURL, "")
    }
    
    func testSetErrorPageURLNoErrorURL() throws {
        controller.setErrorPageURL(queryDict: ["error_url":""])
        XCTAssertEqual(controller.openURL, "")
    }
    
    func testSetErrorPageURL() throws {
        controller.setErrorPageURL(queryDict: ["error_url":"https://example/?test=1&amp;test2=1"])
        XCTAssertEqual(controller.openURL, "https://example/?test=1&test2=1")
    }
    
    func testOnOpenURLEmptyNonceEmpty() throws {
        controller.onOpenURL(url: URL(string: "http://example?action_url=example")!)
        XCTAssertEqual(controller.controllerForUserVerification.actionURL,"")
        XCTAssertEqual(controller.controllerForUserVerification.nonce,"")
        XCTAssertEqual(controller.controllerForSignature.actionURL,"")
        XCTAssertEqual(controller.controllerForSignature.nonce,"")
    }
    
    func testOnOpenURLEmptyActionURLEmpty() throws {
        controller.onOpenURL(url: URL(string: "http://example?nonce=example")!)
        XCTAssertEqual(controller.controllerForUserVerification.actionURL,"")
        XCTAssertEqual(controller.controllerForUserVerification.nonce,"")
        XCTAssertEqual(controller.controllerForSignature.actionURL,"")
        XCTAssertEqual(controller.controllerForSignature.nonce,"")
    }
    
    func testOnOpenURL() throws {
        controller.onOpenURL(url: URL(string: "https://example?action_url=example&nonce=1234")!)
        XCTAssertEqual(controller.controllerForUserVerification.actionURL,"example")
        XCTAssertEqual(controller.controllerForUserVerification.nonce,"1234")
        XCTAssertEqual(controller.controllerForSignature.actionURL,"example")
        XCTAssertEqual(controller.controllerForSignature.nonce,"1234")
    }
}
