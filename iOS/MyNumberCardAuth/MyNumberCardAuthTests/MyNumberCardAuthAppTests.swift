//
//  MyNumberCardAuthAppTests.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/10.
//

@testable import MyNumberCardAuth
import XCTest

final class MyNumberCardAuthAppTests: XCTestCase {
    let myNumberApp = MyNumberCardAuthApp()

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testGenerateQueryDictionary() throws {
        guard let testURL = URL(string: "https://example?nonce=12345&empty") else { return }
        guard let urlComponents = URLComponents(url: testURL, resolvingAgainstBaseURL: true) else { return }

        XCTAssertEqual(MyNumberCardAuth.generateQueryDictionary(from: urlComponents), ["nonce": "12345"])
    }

    func testGenerateQueryDictionaryEmpty() throws {
        guard let testURL = URL(string: "https://example") else { return }
        guard let urlComponents = URLComponents(url: testURL, resolvingAgainstBaseURL: true) else { return }

        XCTAssertEqual(MyNumberCardAuth.generateQueryDictionary(from: urlComponents), [:])
    }

    func testIsMode() throws {
        let mode = MyNumberCardAuth.Mode.Login

        let loginObject = ["mode": "login"]
        XCTAssertEqual(mode.isMode(queryDict: loginObject), MyNumberCardAuth.Mode.Login)

        let registObject = ["mode": "registration"]
        XCTAssertEqual(mode.isMode(queryDict: registObject), MyNumberCardAuth.Mode.Login)

        let replaceObject = ["mode": "replacement"]
        XCTAssertEqual(mode.isMode(queryDict: replaceObject), MyNumberCardAuth.Mode.Login)
    }

    func testIsViewMode() throws {
        let view = MyNumberCardAuth.ShowView.ExplanationView

        let loginObject = ["mode": "login"]
        XCTAssertEqual(view.isViewMode(queryDict: loginObject), MyNumberCardAuth.ShowView.UserVerificationView)

        let registObject = ["mode": "registration"]
        XCTAssertEqual(view.isViewMode(queryDict: registObject), MyNumberCardAuth.ShowView.SignatureView)

        let replaceObject = ["mode": "replacement"]
        XCTAssertEqual(view.isViewMode(queryDict: replaceObject), MyNumberCardAuth.ShowView.SignatureView)
    }
}
