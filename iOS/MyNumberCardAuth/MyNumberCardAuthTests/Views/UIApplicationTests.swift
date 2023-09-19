//
//  UIApplicationTests.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/19.
//

@testable import MyNumberCardAuth
import XCTest

private var openedUrl: URL?

final class UIApplicationTests: XCTestCase {
    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testOpenURL() throws {
        let application = UIApplication.shared
        let aClass: AnyClass! = object_getClass(application)
        guard let originalMethod = class_getInstanceMethod(aClass,
                                                           #selector(UIApplication.open(_:options:completionHandler:))),
            let swizzledMethod = class_getInstanceMethod(aClass,
                                                         #selector(UIApplication.swizzleingOpen(_:options:completionHandler:)))
        else {
            XCTFail("Method not found")
            return
        }
        method_exchangeImplementations(originalMethod, swizzledMethod)
        defer {
            method_exchangeImplementations(swizzledMethod, originalMethod)
        }
        openedUrl = nil

        let url = URL(string: "https://example.com")!
        (application as URLOpenerProtocol).openURL(url)

        XCTAssertEqual(openedUrl?.absoluteString, "https://example.com")
    }
}

extension UIApplication {
    @objc func swizzleingOpen(_ url: URL, options _: [UIApplication.OpenExternalURLOptionsKey: Any] = [:], completionHandler _: ((Bool) -> Void)? = nil) {
        openedUrl = url
    }
}
