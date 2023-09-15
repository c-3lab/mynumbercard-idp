//
//  SignatureViewControllerTests
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/12.
//
@testable import MyNumberCardAuth
import SwiftUI
import XCTest

final class SignatureViewControllerTests: XCTestCase {
    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testIsEnableButton() throws {
        let controller = SignatureViewController()

        controller.inputPIN = ""
        controller.nonce = ""
        controller.actionURL = ""
        XCTAssertFalse(controller.isEnableButton())

        controller.inputPIN = "A12345"
        controller.nonce = ""
        controller.actionURL = ""
        XCTAssertFalse(controller.isEnableButton())

        controller.inputPIN = "A12345"
        controller.nonce = "123456"
        controller.actionURL = ""
        XCTAssertFalse(controller.isEnableButton())

        controller.inputPIN = "A1234"
        controller.nonce = "123456"
        controller.actionURL = "example"
        XCTAssertFalse(controller.isEnableButton())

        controller.inputPIN = "A12345"
        controller.nonce = "123456"
        controller.actionURL = "example"
        XCTAssertTrue(controller.isEnableButton())
    }

    func testGetButtonColor() throws {
        let controller = SignatureViewController()

        controller.inputPIN = ""
        controller.nonce = ""
        controller.actionURL = ""
        XCTAssertEqual(controller.getButtonColor(), Color(UIColor.lightGray))

        controller.inputPIN = "A12345"
        controller.nonce = "123456"
        controller.actionURL = "example"
        XCTAssertEqual(controller.getButtonColor(), Color(UIColor.blue))
    }
}
