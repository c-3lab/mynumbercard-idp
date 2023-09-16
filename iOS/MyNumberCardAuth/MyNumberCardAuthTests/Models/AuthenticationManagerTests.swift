//
//  File.swift
//  AuthenticationManagerTests
//
//  Created by c3lab on 2023/09/12.
//
import XCTest
@testable import MyNumberCardAuth
import SwiftUI

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
            readerMock!.computeDigitalSignatureForUserAuthenticationHandler = {
                XCTAssertEqual($0, "1234")
                XCTAssertEqual($1, [UInt8]("0123456789".utf8))
            }
            return readerMock!
        }
        
        manager.authenticateForUserVerification(pin: "1234",
                                                nonce: "0123456789",
                                                actionURL: "https://example.com",
                                                authenticationController: AuthenticationController())
        
        XCTAssertNotNil(readerMock)
        XCTAssertEqual(readerMock?.computeDigitalSignatureForUserAuthenticationCallCount, 1)
    }
    
    func testAuthenticateForSignature() throws {
        var readerMock: IndividualNumberReaderMock?
        let manager = AuthenticationManager { _ in
            readerMock = IndividualNumberReaderMock()
            readerMock!.computeDigitalSignatureForSignatureHandler = {
                XCTAssertEqual($0, "5678")
                XCTAssertEqual($1, [UInt8]("67890012345".utf8))
            }
            return readerMock!
        }
        
        manager.authenticateForSignature(pin: "5678",
                                         nonce: "67890012345",
                                         actionURL: "https://example.com/1",
                                         authenticationController: AuthenticationController())

        XCTAssertNotNil(readerMock)
        XCTAssertEqual(readerMock?.computeDigitalSignatureForSignatureCallCount, 1)
    }
}
