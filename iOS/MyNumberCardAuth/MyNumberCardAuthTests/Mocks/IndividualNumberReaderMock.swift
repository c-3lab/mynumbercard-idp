//
//  IndividualNumberReaderMock.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/12.
//

import CoreNFC
import TRETJapanNFCReader_MIFARE_IndividualNumber
#if canImport(TRETJapanNFCReader_Core)
    import TRETJapanNFCReader_Core
#endif
#if canImport(TRETJapanNFCReader_MIFARE)
    import TRETJapanNFCReader_MIFARE
#endif
#if os(iOS)
    #if canImport(TRETJapanNFCReader_Core)
        import TRETJapanNFCReader_Core
    #endif
    #if canImport(TRETJapanNFCReader_MIFARE)
        import TRETJapanNFCReader_MIFARE
    #endif
    import CoreNFC
#endif
@testable import MyNumberCardAuth

class IndividualNumberReaderMock: IndividualNumberReaderProtocol {
    init() {}

    private(set) var getCallCount = 0
    var getHandler: (([IndividualNumberCardItem], String) -> Void)?
    func get(items: [IndividualNumberCardItem], cardInfoInputSupportAppPIN: String) {
        getCallCount += 1
        if let getHandler = getHandler {
            getHandler(items, cardInfoInputSupportAppPIN)
        }
    }

    private(set) var lookupRemainingPINCallCount = 0
    var lookupRemainingPINHandler: ((IndividualNumberCardPINType, @escaping (Int?) -> Void) -> Void)?
    func lookupRemainingPIN(pinType: IndividualNumberCardPINType, completion: @escaping (Int?) -> Void) {
        lookupRemainingPINCallCount += 1
        if let lookupRemainingPINHandler = lookupRemainingPINHandler {
            lookupRemainingPINHandler(pinType, completion)
        }
    }

    private(set) var checkReadingAvailableCallCount = 0
    var checkReadingAvailableHandler: (() -> (Bool))?
    func checkReadingAvailable() -> Bool {
        checkReadingAvailableCallCount += 1
        if let checkReadingAvailableHandler = checkReadingAvailableHandler {
            return checkReadingAvailableHandler()
        }
        return false
    }

    private(set) var computeDigitalSignatureCallCount = 0
    var computeDigitalSignatureHandler: ((IndividualNumberCardSignatureType, String, [UInt8]) -> Void)?
    func computeDigitalSignature(signatureType: IndividualNumberCardSignatureType, pin: String, dataToSign: [UInt8]) {
        computeDigitalSignatureCallCount += 1
        if let computeDigitalSignatureHandler = computeDigitalSignatureHandler {
            computeDigitalSignatureHandler(signatureType, pin, dataToSign)
        }
    }
}
