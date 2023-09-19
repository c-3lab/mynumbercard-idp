//
//  AuthenticationManagerTests.swift
//  MyNumberCardAuth
//
//  Created by c3lab on 2023/09/12.
//

import Foundation
@testable import MyNumberCardAuth
@testable import TRETJapanNFCReader_Core
@testable import TRETJapanNFCReader_MIFARE_IndividualNumber
import XCTest

final class AuthenticationManagerTests: XCTestCase {
    private static var certificate: Data! =
        AuthenticationManagerTests.loadStringFromBundle(forResource: "AuthenticationManagerCertificate",
                                                        withExtension: "txt")
        .flatMap { Data(base64URLEncoded: $0) }

    private static let certs: Data! =
        AuthenticationManagerTests.loadDataFromBundle(forResource: "AuthenticationManagerCerts",
                                                      withExtension: "json")

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testAuthenticateForUserVerification() throws {
        let doTest = { [weak self] (runMode: Mode,
                                    viewState: ShowView,
                                    validCertificate: Bool,
                                    validCerts: Bool) in
                guard let self = self else {
                    XCTFail()
                    return
                }

                var manager: AuthenticationManager!
                let readerMock = IndividualNumberReaderMock()
                let readerComputeDigitalSignatureForUserAuthenticationExpectation = expectation(description: "reader.computeDigitalSignatureForUserAuthentication")
                readerMock.computeDigitalSignatureHandler = {
                    XCTAssertEqual($0, .userAuthentication)
                    XCTAssertEqual($1, "1234")
                    XCTAssertEqual($2, [UInt8]("0123456789".utf8))
                    var cardData = TRETJapanNFCReader_MIFARE_IndividualNumber.IndividualNumberCardData()
                    cardData.computeDigitalSignatureForUserAuthentication = Array("5678".utf8)
                    cardData.userAuthenticationCertificate = validCertificate ? Array(Self.certificate) : [UInt8]("5678901234".utf8)
                    manager.individualNumberReaderSession(didRead: cardData)
                    readerComputeDigitalSignatureForUserAuthenticationExpectation.fulfill()
                }
                let httpSessionMock = HTTPSessionMock()
                let httpSessionOpenRedirectURLOnSafariExpectation = expectation(description: "httpSession.openRedirectURLOnSafari")
                httpSessionMock.openRedirectURLOnSafariHandler = { request in
                    XCTAssertEqual(request.url?.absoluteString, "https://example.com/realms/1")
                    httpSessionOpenRedirectURLOnSafariExpectation.fulfill()
                }
                let urlSessionMock = URLSessionMock()
                let urlSessionDataExpectation = expectation(description: "urlSession.data")
                let dataHandler: ((URLRequest, URLSessionTaskDelegate?) async throws -> (Data, URLResponse))? = {
                    request, _ in
                    XCTAssertEqual(request.url?.absoluteString, "https://example.com/realms/1/protocol/openid-connect/certs")
                    urlSessionDataExpectation.fulfill()
                    return (validCerts ? Self.certs : Data(),
                            URLResponse())
                }
                urlSessionMock.dataHandler = dataHandler
                manager = AuthenticationManager(makeReader: { _ in
                                                    readerMock
                                                },
                                                makeHTTPSession: { _ in
                                                    httpSessionMock
                                                },
                                                makeURLSession: {
                                                    urlSessionMock
                                                })
                let controller = AuthenticationControllerMock()
                controller.runMode = runMode
                controller.viewState = viewState
                manager.authenticationController = controller

                manager.authenticateForUserVerification(pin: "1234",
                                                        nonce: "0123456789",
                                                        actionURL: "https://example.com/realms/1")

                waitForExpectations(timeout: 0.3)
                XCTAssertEqual(readerMock.computeDigitalSignatureCallCount, 1)
                XCTAssertEqual(httpSessionMock.openRedirectURLOnSafariCallCount, 1)
                XCTAssertEqual(urlSessionMock.dataCallCount, 1)
        }

        for mode in Mode.allCases {
            for viewState in ShowView.allCases {
                for validCertificate in [true, false] {
                    for validCerts in [true, false] {
                        doTest(mode, viewState, validCertificate, validCerts)
                    }
                }
            }
        }
    }

    func testAuthenticateForSignature() throws {
        let doTest = { [weak self] (runMode: Mode,
                                    viewState: ShowView,
                                    validCertificate: Bool,
                                    validCerts: Bool) in
                guard let self = self else {
                    XCTFail()
                    return
                }

                var manager: AuthenticationManager!
                let readerMock = IndividualNumberReaderMock()
                let readerComputeDigitalSignatureForSignatureExpectation = expectation(description: "reader.computeDigitalSignatureForSignature")
                readerMock.computeDigitalSignatureHandler = {
                    XCTAssertEqual($0, .digitalSignature)
                    XCTAssertEqual($1, "7890")
                    XCTAssertEqual($2, [UInt8]("7890123456".utf8))
                    var cardData = TRETJapanNFCReader_MIFARE_IndividualNumber.IndividualNumberCardData()
                    cardData.computeDigitalSignatureForDigitalSignature = Array("0123".utf8)
                    cardData.digitalSignatureCertificate = validCertificate ? Array(Self.certificate) : [UInt8]("0123456789".utf8)
                    manager.individualNumberReaderSession(didRead: cardData)
                    readerComputeDigitalSignatureForSignatureExpectation.fulfill()
                }
                let httpSessionMock = HTTPSessionMock()
                let httpSessionOpenRedirectURLOnSafariExpectation = expectation(description: "httpSession.openRedirectURLOnSafari")
                httpSessionMock.openRedirectURLOnSafariHandler = { request in
                    XCTAssertEqual(request.url?.absoluteString, "https://example.com/realms/2")
                    httpSessionOpenRedirectURLOnSafariExpectation.fulfill()
                }
                let urlSessionMock = URLSessionMock()
                let urlSessionDataExpectation = expectation(description: "urlSession.data")
                let dataHandler: ((URLRequest, URLSessionTaskDelegate?) async throws -> (Data, URLResponse))? = {
                    request, _ in
                    XCTAssertEqual(request.url?.absoluteString, "https://example.com/realms/2/protocol/openid-connect/certs")
                    urlSessionDataExpectation.fulfill()
                    return (validCerts ? Self.certs : Data(),
                            URLResponse())
                }
                urlSessionMock.dataHandler = dataHandler
                manager = AuthenticationManager(makeReader: { _ in
                                                    readerMock
                                                },
                                                makeHTTPSession: { _ in
                                                    httpSessionMock
                                                },
                                                makeURLSession: {
                                                    urlSessionMock
                                                })
                let controller = AuthenticationControllerMock()
                controller.runMode = runMode
                controller.viewState = viewState
                manager.authenticationController = controller

                manager.authenticateForSignature(pin: "7890",
                                                 nonce: "7890123456",
                                                 actionURL: "https://example.com/realms/2")

                waitForExpectations(timeout: 0.3)
                XCTAssertEqual(readerMock.computeDigitalSignatureCallCount, 1)
                XCTAssertEqual(httpSessionMock.openRedirectURLOnSafariCallCount, 1)
                XCTAssertEqual(urlSessionMock.dataCallCount, 1)
        }

        for mode in Mode.allCases {
            for viewState in ShowView.allCases {
                for validCertificate in [true, false] {
                    for validCerts in [true, false] {
                        doTest(mode, viewState, validCertificate, validCerts)
                    }
                }
            }
        }
    }

    func testAuthenticateForUserVerificationAuthenticationControllerNotSet() throws {
        let readerMock = IndividualNumberReaderMock()
        let manager = AuthenticationManager(makeReader: { _ in
                                                readerMock
                                            },
                                            makeHTTPSession: { _ in
                                                HTTPSessionMock()
                                            },
                                            makeURLSession: {
                                                URLSessionMock()
                                            })

        manager.authenticateForUserVerification(pin: "1234",
                                                nonce: "0123456789",
                                                actionURL: "https://example.com/realms/1")

        XCTAssertEqual(readerMock.computeDigitalSignatureCallCount, 0)
    }

    func testAuthenticateForSignatureAuthenticationControllerNotSet() throws {
        let readerMock = IndividualNumberReaderMock()
        let manager = AuthenticationManager(makeReader: { _ in
                                                readerMock
                                            },
                                            makeHTTPSession: { _ in
                                                HTTPSessionMock()
                                            },
                                            makeURLSession: {
                                                URLSessionMock()
                                            })

        manager.authenticateForSignature(pin: "5678",
                                         nonce: "5678901234",
                                         actionURL: "https://example.com/realms/2")

        XCTAssertEqual(readerMock.computeDigitalSignatureCallCount, 0)
    }

    func testAuthenticateForUserVerificationReadingCardFailed() throws {
        let doTest = { [weak self] (runMode: Mode,
                                    viewState: ShowView,
                                    validCertificate: Bool,
                                    validCerts: Bool) in
                guard let self = self else {
                    XCTFail()
                    return
                }

                var manager: AuthenticationManager!
                let readerMock = IndividualNumberReaderMock()
                let readerJapanNFCReaderSessionExpectation = expectation(description: "reader.japanNFCReaderSession")
                readerMock.computeDigitalSignatureHandler = {
                    XCTAssertEqual($0, .userAuthentication)
                    XCTAssertEqual($1, "1234")
                    XCTAssertEqual($2, [UInt8]("0123456789".utf8))
                    var cardData = TRETJapanNFCReader_MIFARE_IndividualNumber.IndividualNumberCardData()
                    cardData.computeDigitalSignatureForUserAuthentication = Array("5678".utf8)
                    cardData.userAuthenticationCertificate = validCertificate ? Array(Self.certificate) : [UInt8]("5678901234".utf8)
                    manager.japanNFCReaderSession(didInvalidateWithError: JapanNFCReaderError.nfcReadingUnavailable)
                    readerJapanNFCReaderSessionExpectation.fulfill()
                }
                let httpSessionMock = HTTPSessionMock()
                let httpSessionOpenRedirectURLOnSafariExpectation = expectation(description: "httpSession.openRedirectURLOnSafari")
                httpSessionOpenRedirectURLOnSafariExpectation.isInverted = true
                httpSessionMock.openRedirectURLOnSafariHandler = { request in
                    XCTAssertEqual(request.url?.absoluteString, "https://example.com/realms/1")
                    httpSessionOpenRedirectURLOnSafariExpectation.fulfill()
                }
                let urlSessionMock = URLSessionMock()
                let urlSessionDataExpectation = expectation(description: "urlSession.data")
                urlSessionDataExpectation.isInverted = true
                let dataHandler: ((URLRequest, URLSessionTaskDelegate?) async throws -> (Data, URLResponse))? = {
                    request, _ in
                    XCTAssertEqual(request.url?.absoluteString, "https://example.com/realms/1/protocol/openid-connect/certs")
                    urlSessionDataExpectation.fulfill()
                    return (validCerts ? Self.certs : Data(),
                            URLResponse())
                }
                urlSessionMock.dataHandler = dataHandler
                manager = AuthenticationManager(makeReader: { _ in
                                                    readerMock
                                                },
                                                makeHTTPSession: { _ in
                                                    httpSessionMock
                                                },
                                                makeURLSession: {
                                                    urlSessionMock
                                                })
                let controller = AuthenticationControllerMock()
                controller.runMode = runMode
                controller.viewState = viewState
                manager.authenticationController = controller

                manager.authenticateForUserVerification(pin: "1234",
                                                        nonce: "0123456789",
                                                        actionURL: "https://example.com/realms/1")

                waitForExpectations(timeout: 0.3)
                XCTAssertEqual(readerMock.computeDigitalSignatureCallCount, 1)
        }

        for mode in Mode.allCases {
            for viewState in ShowView.allCases {
                for validCertificate in [true, false] {
                    for validCerts in [true, false] {
                        doTest(mode, viewState, validCertificate, validCerts)
                    }
                }
            }
        }
    }

    private static func loadDataFromBundle(forResource resource: String,
                                           withExtension extension: String) -> Data?
    {
        return Bundle(for: self)
            .url(forResource: resource,
                 withExtension: `extension`)
            .flatMap { try? Data(contentsOf: $0) }
    }

    private static func loadStringFromBundle(forResource resource: String,
                                             withExtension extension: String) -> String?
    {
        return loadDataFromBundle(forResource: resource,
                                  withExtension: `extension`)
            .flatMap { String(data: $0,
                              encoding: .utf8) }
    }
}
