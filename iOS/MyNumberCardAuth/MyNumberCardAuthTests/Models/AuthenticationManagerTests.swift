//
//  AuthenticationManagerTests.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/12.
//

import Foundation
@testable import MyNumberCardAuth
@testable import TRETJapanNFCReader_MIFARE_IndividualNumber
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
        var manager: AuthenticationManager!
        manager = AuthenticationManager(makeReader: { _ in
                                            readerMock = IndividualNumberReaderMock()
                                            readerMock!.computeDigitalSignatureHandler = {
                                                XCTAssertEqual($0, .userAuthentication)
                                                XCTAssertEqual($1, "1234")
                                                XCTAssertEqual($2, [UInt8]("0123456789".utf8))
                                                var cardData = TRETJapanNFCReader_MIFARE_IndividualNumber.IndividualNumberCardData()
                                                cardData.computeDigitalSignatureForUserAuthentication = Array("5678".utf8)
                                                cardData.userAuthenticationCertificate = Array("9012".utf8)
                                                manager.individualNumberReaderSession(didRead: cardData)
                                            }
                                            return readerMock!
                                        },
                                        makeHTTPSession: { _ in
                                            HTTPSessionMock()
                                        })
        let controller = AuthenticationControllerMock()
        manager.authenticationController = controller

        manager.authenticateForUserVerification(pin: "1234",
                                                nonce: "0123456789",
                                                actionURL: "https://example.com/realms/1")

        XCTAssertNotNil(readerMock)
        XCTAssertEqual(readerMock?.computeDigitalSignatureCallCount, 1)
    }

    func testAuthenticateForSignature() throws {
        var readerMock: IndividualNumberReaderMock?
        var manager: AuthenticationManager!
        manager = AuthenticationManager(makeReader: { _ in
                                            readerMock = IndividualNumberReaderMock()
                                            readerMock!.computeDigitalSignatureHandler = {
                                                XCTAssertEqual($0, .digitalSignature)
                                                XCTAssertEqual($1, "5678")
                                                XCTAssertEqual($2, [UInt8]("67890012345".utf8))
                                                var cardData = TRETJapanNFCReader_MIFARE_IndividualNumber.IndividualNumberCardData()
                                                cardData.computeDigitalSignatureForDigitalSignature = Array("5678".utf8)
                                                cardData.digitalSignatureCertificate = Array("9012".utf8)
                                                manager.individualNumberReaderSession(didRead: cardData)
                                            }
                                            return readerMock!
                                        },
                                        makeHTTPSession: { _ in
                                            HTTPSessionMock()
                                        })
        let controller = AuthenticationControllerMock()
        manager.authenticationController = controller

        manager.authenticateForSignature(pin: "5678",
                                         nonce: "67890012345",
                                         actionURL: "https://example.com/realms/2")

        XCTAssertNotNil(readerMock)
        XCTAssertEqual(readerMock?.computeDigitalSignatureCallCount, 1)
    }
}
