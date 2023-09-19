//
//  AuthenticationManagerTests.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/12.
//
@testable import MyNumberCardAuth
import SwiftUI
import XCTest

final class AuthenticationManagerTests: XCTestCase {
    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testAuthenticateForUserVerification() throws {
        var readerMock: IndividualNumberReaderMock?
        let manager = AuthenticationManager { _ in
            readerMock = IndividualNumberReaderMock()
            readerMock!.computeDigitalSignatureHandler = {
                XCTAssertEqual($0, .userAuthentication)
                XCTAssertEqual($1, "1234")
                XCTAssertEqual($2, [UInt8]("0123456789".utf8))
            }
            return readerMock!
        }
        let controller = AuthenticationControllerMock()
        manager.authenticationController = controller

        manager.authenticateForUserVerification(pin: "1234",
                                                nonce: "0123456789",
                                                actionURL: "https://example.com")

        XCTAssertNotNil(readerMock)
        XCTAssertEqual(readerMock?.computeDigitalSignatureCallCount, 1)
    }

    func testAuthenticateForSignature() throws {
        var readerMock: IndividualNumberReaderMock?
        let manager = AuthenticationManager { _ in
            readerMock = IndividualNumberReaderMock()
            readerMock!.computeDigitalSignatureHandler = {
                XCTAssertEqual($0, .digitalSignature)
                XCTAssertEqual($1, "5678")
                XCTAssertEqual($2, [UInt8]("67890012345".utf8))
            }
            return readerMock!
        }
        let controller = AuthenticationControllerMock()
        manager.authenticationController = controller

        manager.authenticateForSignature(pin: "5678",
                                         nonce: "67890012345",
                                         actionURL: "https://example.com/1")

        XCTAssertNotNil(readerMock)
        XCTAssertEqual(readerMock?.computeDigitalSignatureCallCount, 1)
    }
}
