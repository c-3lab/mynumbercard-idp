//
//  AuthenticationControllerTests.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/10.
//
import XCTest
@testable import MyNumberCardAuth
import SwiftUI

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
    
    func testOpenURLButton_empty() throws {
        controller.openURLButton(url:"")
    }
    
    func testOpenURLButton_noUrl() throws {
        controller.openURLButton(url:"てすと")
    }

    func testStartReading()throws {
        // mock必要
        controller.viewState = .SignatureView
        controller.startReading(pin: "1234", nonce: "0123456789", actionURL: "https:example")
        
        controller.viewState = .UserVerificationView
        controller.startReading(pin: "1234", nonce: "0123456789", actionURL: "https:example")
        
        controller.viewState = .ExplanationView
        controller.startReading(pin: "1234", nonce: "0123456789", actionURL: "https:example")
    }
    
    func testGetButtonColor() throws {
        XCTAssertEqual(controller.getButtonColor(checkStr: "example"), Color(UIColor.blue))
    }
    
    func testGetButtonColorEmpty() throws {
        XCTAssertEqual(controller.getButtonColor(checkStr: ""), Color(UIColor.lightGray))
    }
    
    func testOnOpenURL_empty() throws {
        // Componentsをnilにできない。。。
        controller.onOpenURL(url: URL(string: "https://example")!)
        XCTAssertEqual(controller.queryDict,[:])
    }
    
    func testOnOpenURL() throws {
        controller.onOpenURL(url: URL(string: "https://example?action_url=example&nonce=1234")!)
        XCTAssertEqual(controller.controllerForUserVerification.actionURL,"example")
        XCTAssertEqual(controller.controllerForUserVerification.nonce,"1234")
        XCTAssertEqual(controller.controllerForSignature.actionURL,"example")
        XCTAssertEqual(controller.controllerForSignature.nonce,"1234")
    }
}
