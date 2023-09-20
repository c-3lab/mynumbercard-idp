//
//  MyNumberCardAuthUITests.swift
//  MyNumberCardAuthUITests
//
//  Created by c3lab on 2023/09/10.
//

import XCTest

// ユニバーサルリンクを使用するため、keycloakへのアクセスが可能な状態でテストを実行してください。
// 下記変数を使用するkeycloakのアドレスとレルムに変更してテストを行なってください。
var keycloakURL: String = "https://54f4c17847af.ngrok.app" // ポート80
var relum: String = "OIdp"

final class MyNumberCardAuthUITests: XCTestCase {
    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.

        // In UI tests it is usually best to stop immediately when a failure occurs.
        continueAfterFailure = false

        // In UI tests it’s important to set the initial state - such as interface orientation - required for your tests before they run. The setUp method is a good place to do this.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testAppExplanationView() throws {
        // UI tests must launch the application that they test.
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.staticTexts["このアプリについて"].exists)
        XCTAssertTrue(app.staticTexts["ブラウザを使用してログインするためのアプリです。ブラウザから起動して使用してください。"].exists)

        XCTAssertTrue(app.buttons["利用規約"].exists)
        XCTAssertTrue(app.buttons["プライバシーポリシー"].exists)
        XCTAssertTrue(app.buttons["個人情報保護方針"].exists)
        
        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")

        app.buttons["利用規約"].tap()
        sleep(3)
        XCTAssertTrue(safari.staticTexts["利用規約"].exists)
        app.activate()
        
        sleep(3)
        app.buttons["プライバシーポリシー"].tap()
        sleep(3)
        XCTAssertTrue(safari.staticTexts["プライバシーポリシー"].exists)
        app.activate()
        
        sleep(3)
        app.buttons["個人情報保護方針"].tap()
        sleep(3)
        XCTAssertTrue(safari.staticTexts["個人情報保護方針"].exists)
        app.activate()
    }

    func testNFCReadingForUserVerificationView() throws {
        let app = XCUIApplication()
        XCUIDevice.shared.press(.home)
        XCUIApplication(bundleIdentifier: "com.apple.springboard").swipeDown()
        let spotlight = XCUIApplication(bundleIdentifier: "com.apple.Spotlight")
        spotlight.textFields["SpotlightSearchField"].typeText(keycloakURL + "/realms/" + relum + "/login-actions/authenticate?action_url=https://example&mode=login&nonce=1234")
        spotlight.buttons["Go"].tap()
        
        XCTAssertTrue(app.staticTexts["マイナンバーカード\n認証"].exists)
        XCTAssertTrue(app.staticTexts["マイナンバーカードの利用者証明用電子証明書のパスワード（4桁の数字）を入力し、読み取り開始ボタンを押してください"].exists)
        XCTAssertTrue(app.secureTextFields.firstMatch.exists)
        XCTAssertTrue(app.buttons["読み取り開始"].exists)
        
        app.secureTextFields.firstMatch.tap()
        app.secureTextFields.firstMatch.typeText("12345")
        app.buttons["閉じる"].tap()
    }

    func testNFCReadingForSignatureView() throws {
        let app = XCUIApplication()
        
        XCUIDevice.shared.press(.home)
        XCUIApplication(bundleIdentifier: "com.apple.springboard").swipeDown()
        let spotlight = XCUIApplication(bundleIdentifier: "com.apple.Spotlight")
        spotlight.textFields["SpotlightSearchField"].typeText(keycloakURL + "/realms/" + relum + "/login-actions/authenticate?action_url=https://example&mode=registration&nonce=1234")
        spotlight.buttons["Go"].tap()
        
        XCTAssertTrue(app.staticTexts["マイナンバーカード\n認証"].exists)
        XCTAssertTrue(app.staticTexts["マイナンバーカードの署名用電子証明書のパスワード（6～16桁の英数字）を入力し、読み取り開始ボタンを押してください"].exists)
        XCTAssertTrue(app.secureTextFields.firstMatch.exists)
        XCTAssertTrue(app.buttons["読み取り開始"].exists)
        
        app.secureTextFields.firstMatch.tap()
        app.secureTextFields.firstMatch.typeText("abcdefghij01234567")
        app.buttons["閉じる"].tap()
    }
}
