//
//  IndividualNumberReaderProtocol.swift
//  MyNumberCardAuthTests
//
//  Created by c3lab on 2023/09/14.
//

import TRETJapanNFCReader_MIFARE_IndividualNumber

import CoreNFC
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


/// @mockable(override: name = IndividualNumberReaderMock)
protocol IndividualNumberReaderProtocol {
    func get(items: [IndividualNumberCardItem], cardInfoInputSupportAppPIN: String)
    func lookupRemainingPIN(pinType: IndividualNumberCardPINType, completion: @escaping (Int?) -> Void)
    func checkReadingAvailable() -> Bool
    func computeDigitalSignatureForUserAuthentication(userAuthenticationPIN: String, dataToSign: [UInt8])
    func computeDigitalSignatureForSignature(signaturePIN: String, dataToSign: [UInt8])
}

extension IndividualNumberReader : IndividualNumberReaderProtocol {
    
}

