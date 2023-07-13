//
//  IndividualNumberReaderReadFunctions.swift
//  TRETJapanNFCReader
//
//  Created by treastrain on 2020/05/10.
//  Copyright © 2020 treastrain / Tanaka Ryoga. All rights reserved.
//

#if os(iOS)
import CoreNFC
#if canImport(TRETJapanNFCReader_MIFARE)
import TRETJapanNFCReader_MIFARE
#endif
import CryptoKit

@available(iOS 13.0, *)
extension IndividualNumberReader {
    
    internal func readJPKIToken(_ session: NFCTagReaderSession, _ individualNumberCard: IndividualNumberCard) -> IndividualNumberCard {
        let semaphore = DispatchSemaphore(value: 0)
        var individualNumberCard = individualNumberCard
        let tag = individualNumberCard.tag
        
        self.selectJPKIAP(tag: tag) { (responseData, sw1, sw2, error) in
            self.printData(responseData, sw1, sw2)
            
            if let error = error {
                print(error.localizedDescription)
                session.invalidate(errorMessage: "SELECT JPKIAP\n\(error.localizedDescription)")
                self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                return
            }
            
            if sw1 != 0x90 {
                session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                return
            }
            
            self.selectEF(tag: tag, data: [0x00, 0x06]) { (responseData, sw1, sw2, error) in
                self.printData(responseData, sw1, sw2)
                
                if let error = error {
                    print(error.localizedDescription)
                    session.invalidate(errorMessage: "SELECT EF\n\(error.localizedDescription)")
                    self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                    return
                }
                
                if sw1 != 0x90 {
                    session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                    return
                }
                
                self.readBinary(tag: tag, p1Parameter: 0x00, p2Parameter: 0x00, expectedResponseLength: 20) { (responseData, sw1, sw2, error) in
                    self.printData(responseData, sw1, sw2)
                    
                    if let error = error {
                        print(error.localizedDescription)
                        session.invalidate(errorMessage: "READ BINARY\n\(error.localizedDescription)")
                        self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                        return
                    }
                    
                    if sw1 != 0x90 {
                        session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                        return
                    }
                    
                    let responseString = String(data: responseData, encoding: .utf8) ?? ""
                    individualNumberCard.data.token = responseString.filter { $0 != " " }
                    semaphore.signal()
                }
            }
        }
        
        semaphore.wait()
        return individualNumberCard
    }
    
    internal func readIndividualNumber(_ session: NFCTagReaderSession, _ individualNumberCard: IndividualNumberCard, cardInfoInputSupportAppPIN: [UInt8]) -> IndividualNumberCard {
        
        if cardInfoInputSupportAppPIN.isEmpty {
            session.invalidate(errorMessage: IndividualNumberReaderError.needPIN.errorDescription!)
            self.delegate?.japanNFCReaderSession(didInvalidateWithError: IndividualNumberReaderError.needPIN)
            return individualNumberCard
        }
        
        let semaphore = DispatchSemaphore(value: 0)
        var individualNumberCard = individualNumberCard
        let tag = individualNumberCard.tag
        
        self.selectCardInfoInputSupportAP(tag: tag) { (responseData, sw1, sw2, error) in
            self.printData(responseData, sw1, sw2)
            
            if let error = error {
                print(error.localizedDescription)
                session.invalidate(errorMessage: "SELECT TextAP\n\(error.localizedDescription)")
                self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                return
            }
            
            if sw1 != 0x90 {
                session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                return
            }
            
            self.selectEF(tag: tag, data: [0x00, 0x11]) { (responseData, sw1, sw2, error) in
                self.printData(responseData, sw1, sw2)
                
                if let error = error {
                    print(error.localizedDescription)
                    session.invalidate(errorMessage: "SELECT EF\n\(error.localizedDescription)")
                    self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                    return
                }
                
                if sw1 != 0x90 {
                    session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                    return
                }
                
                self.verify(tag: tag, pin: cardInfoInputSupportAppPIN) { (responseData, sw1, sw2, error) in
                    self.printData(responseData, sw1, sw2)
                    
                    if let error = error {
                        print(error.localizedDescription)
                        session.invalidate(errorMessage: "VERIFY\n\(error.localizedDescription)")
                        self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                        return
                    }
                    
                    if sw1 != 0x90 {
                        if sw1 == 0x63 {
                            var error = IndividualNumberReaderError.incorrectPIN(0)
                            switch sw2 {
                            case 0xC1:
                                error = .incorrectPIN(1)
                            case 0xC2:
                                error = .incorrectPIN(2)
                            case 0xC3:
                                error = .incorrectPIN(3)
                            case 0xC4:
                                error = .incorrectPIN(4)
                            case 0xC5:
                                error = .incorrectPIN(5)
                            default:
                                break
                            }
                            print("PIN エラー", error)
                            self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                        }
                        session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                        return
                    }
                    
                    self.selectEF(tag: tag, data: [0x00, 0x01]) { (responseData, sw1, sw2, error) in
                        self.printData(responseData, sw1, sw2)
                        
                        if let error = error {
                            print(error.localizedDescription)
                            session.invalidate(errorMessage: "SELECT EF\n\(error.localizedDescription)")
                            self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                            return
                        }
                        
                        if sw1 != 0x90 {
                            session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                            return
                        }
                        
                        self.readBinary(tag: tag, p1Parameter: 0x00, p2Parameter: 0x00, expectedResponseLength: 17) { (responseData, sw1, sw2, error) in
                            self.printData(responseData, sw1, sw2)
                            
                            if let error = error {
                                print(error.localizedDescription)
                                session.invalidate(errorMessage: "READ BINARY\n\(error.localizedDescription)")
                                self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                                return
                            }
                            
                            if sw1 != 0x90 {
                                session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                                return
                            }
                            
                            var data = [UInt8](responseData)
                            data.removeFirst()
                            let fields = TLVField.sequenceOfFields(from: data)
                            
                            if let individualNumberData = fields.first?.value, let individualNumber = String(data: Data(individualNumberData), encoding: .utf8) {
                                individualNumberCard.data.individualNumber = individualNumber
                            }
                            
                            semaphore.signal()
                        }
                    }
                }
            }
        }
        
        semaphore.wait()
        return individualNumberCard
    }
    
    
    internal func lookupRemainingPIN(_ session: NFCTagReaderSession, _ individualNumberCard: IndividualNumberCard, _ pinType: IndividualNumberCardPINType) -> IndividualNumberCard {
        
        var individualNumberCard = individualNumberCard
        let tag = individualNumberCard.tag
        let semaphore = DispatchSemaphore(value: 0)
        
        let dfData: Data
        let efData: [UInt8]
        switch pinType {
        case .digitalSignature:
            dfData = IndividualNumberCardAID.jpkiAP
            efData = [0x00, 0x1B]
        case .userAuthentication:
            dfData = IndividualNumberCardAID.jpkiAP
            efData = [0x00, 0x18]
        case .cardInfoInputSupport:
            dfData = IndividualNumberCardAID.cardInfoInputSupportAP
            efData = [0x00, 0x11]
        case .individualNumber:
            dfData = IndividualNumberCardAID.individualNumberAP
            efData = [0x00, 0x1C]
        }
        
        self.selectDF(tag: tag, data: dfData) { (responseData, sw1, sw2, error) in
            // self.printData(responseData, isPrintData: true, sw1, sw2)
            
            if let error = error {
                print(error.localizedDescription)
                session.invalidate(errorMessage: "SELECT DF\n\(error.localizedDescription)")
                self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                semaphore.signal()
                return
            }
            
            if sw1 != 0x90 {
                session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                semaphore.signal()
                return
            }
            
            self.selectEF(tag: tag, data: efData) { (responseData, sw1, sw2, error) in
                // self.printData(responseData, isPrintData: true, sw1, sw2)
                
                if let error = error {
                    print(error.localizedDescription)
                    session.invalidate(errorMessage: "SELECT EF\n\(error.localizedDescription)")
                    self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                    semaphore.signal()
                    return
                }
                
                if sw1 != 0x90 {
                    session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                    semaphore.signal()
                    return
                }
                
                self.verify(tag: tag, pin: []) { (responseData, sw1, sw2, error) in
                    // self.printData(responseData, isPrintData: true, sw1, sw2)
                    
                    if let error = error {
                        print(error.localizedDescription)
                        session.invalidate(errorMessage: "VERIFY\n\(error.localizedDescription)")
                        self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
                        semaphore.signal()
                        return
                    }
                    
                    if sw1 == 0x63 {
                        individualNumberCard.data.remaining = Int(sw2 & 0x0F)
                    } else {
                        session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
                    }
                    
                    semaphore.signal()
                }
            }
        }
        
        semaphore.wait()
        return individualNumberCard
    }
    
    internal func computeDigitalSignatureForUserAuthentication(_ session: NFCTagReaderSession, _ individualNumberCard: IndividualNumberCard, userAuthenticationPIN: [UInt8], dataToSign: [UInt8]) -> IndividualNumberCard {
        
        if userAuthenticationPIN.isEmpty {
            session.invalidate(errorMessage: IndividualNumberReaderError.needPIN.errorDescription!)
            self.delegate?.japanNFCReaderSession(didInvalidateWithError: IndividualNumberReaderError.needPIN)
            return individualNumberCard
        }
        
        let digestInfo = generateDigestInfo(data: dataToSign)
        
        var individualNumberCard = individualNumberCard
        let tag = individualNumberCard.tag
        let semaphore = DispatchSemaphore(value: 0)
        
        self.selectJPKIAP(tag: tag){ (responseData, sw1, sw2, error) in
            // 公的個人認証APを選択
            self.printData(responseData, sw1, sw2)
            
            if self.tryHandleAPDUError(ADPUPattern: "SELECT TextAP", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                return
            }
            
            self.selectEF(tag: tag, data: [0x00, 0x18]) { (responseData, sw1, sw2, error) in
                // 認証用PINを選択する
                self.printData(responseData, sw1, sw2)
                
                if self.tryHandleAPDUError(ADPUPattern: "SELECT EF", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                    return
                }
                
                self.verify(tag: tag, pin: userAuthenticationPIN) { (responseData, sw1, sw2, error) in
                    // 認証用パスワードを入力してセキュリティステータスを更新する
                    self.printData(responseData, sw1, sw2)
                    
                    if self.tryHandleAPDUError(ADPUPattern: "VERIFY", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                        return
                    }
                    
                    self.selectEF(tag: tag, data: [0x00, 0x17]) { (responseData, sw1, sw2, error) in
                        // 認証用秘密鍵を選択する
                        self.printData(responseData, sw1, sw2)
                        
                        if self.tryHandleAPDUError(ADPUPattern: "SELECT EF", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                            return
                        }
                        
                        self.computeDigitalSignature(tag: tag, expectedResponseLength: 256, dataToSign: digestInfo) { (responseData, sw1, sw2, error) in
                            // 対象データを送って署名する
                            self.printData(responseData, sw1, sw2)
                            
                            if self.tryHandleAPDUError(ADPUPattern: "COMPUTE DIGITAL SIGNATURE", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                                return
                            }
                            
                            let signedData = [UInt8](responseData)
                            individualNumberCard.data.digitalSignatureForUserVerification = signedData
                            semaphore.signal()
                        }
                    }
                }
            }
        }
        
        semaphore.wait()
        return individualNumberCard
    }
    
    // 署名用電子証明書で署名
    internal func computeDigitalSignatureForSignature(_ session: NFCTagReaderSession, _ individualNumberCard: IndividualNumberCard, userAuthenticationPIN: [UInt8], dataToSign: [UInt8]) -> IndividualNumberCard {
        
        if userAuthenticationPIN.isEmpty {
            session.invalidate(errorMessage: IndividualNumberReaderError.needPIN.errorDescription!)
            self.delegate?.japanNFCReaderSession(didInvalidateWithError: IndividualNumberReaderError.needPIN)
            return individualNumberCard
        }
        
        let digestInfo = generateDigestInfo(data: dataToSign)
        
        var individualNumberCard = individualNumberCard
        let tag = individualNumberCard.tag
        let semaphore = DispatchSemaphore(value: 0)

        print("読込処理")
        self.selectJPKIAP(tag: tag){ (responseData, sw1, sw2, error) in
            // 公的個人認証APを選択
            self.printData(responseData, sw1, sw2)
            
            if self.tryHandleAPDUError(ADPUPattern: "SELECT TextAP", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                return
            }
            
            self.selectEF(tag: tag, data: [0x00, 0x1B]) { (responseData, sw1, sw2, error) in
                // 認証用PINを選択する
                self.printData(responseData, sw1, sw2)
                
                if self.tryHandleAPDUError(ADPUPattern: "SELECT EF", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                    return
                }
                
                self.verify(tag: tag, pin: userAuthenticationPIN) { (responseData, sw1, sw2, error) in
                    // 認証用パスワードを入力してセキュリティステータスを更新する
                    self.printData(responseData, sw1, sw2)
                    
                    if self.tryHandleAPDUError(ADPUPattern: "VERIFY", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                        return
                    }
                    
                    self.selectEF(tag: tag, data: [0x00, 0x1A]) { (responseData, sw1, sw2, error) in
                        // 認証用秘密鍵を選択する
                        self.printData(responseData, sw1, sw2)
                        
                        if self.tryHandleAPDUError(ADPUPattern: "SELECT EF", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                            return
                        }
                        
                        self.computeDigitalSignature(tag: tag, expectedResponseLength: 256, dataToSign: digestInfo) { (responseData, sw1, sw2, error) in
                            // 対象データを送って署名する
                            self.printData(responseData, sw1, sw2)
                            
                            if self.tryHandleAPDUError(ADPUPattern: "COMPUTE DIGITAL SIGNATURE(forSignature)", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                                return
                            }
                            
                            let signedData = [UInt8](responseData)
                            individualNumberCard.data.digitalSignatureForUserVerification = signedData
                            semaphore.signal()
                        }
                    }
                }
            }
        }
        
        semaphore.wait()
        return individualNumberCard
    }
    
    func generateDigestInfo(data : [UInt8]) -> [UInt8]{
        let hashedData = sha256(data: data)
        
        /// 署名を行うには、ASN.1記法で定義されたDigestInfoというデータに変換する。
        /// 以下はASN.1記法で定義されたDigestInfoであり、これをバイト列にしたものに署名する
        /// DigestInfo ::= SEQUENCE {
        ///      digestAlgorithm SEQUENCE  {
        ///           algorithm   OBJECT IDENTIFIER,
        ///           parameters  ANY DEFINED BY algorithm OPTIONAL  },
        ///      digest OCTET STRING }
        let sequenceTag: [UInt8] = [0x30]
        let outerSequencelength: [UInt8] = [0x31]
        let digestAlgorithmSequencelength: [UInt8] = [0x0D]
        let objectIdentifierTag: [UInt8] = [0x06]
        let algorithmObjectIdentifierLength: [UInt8] = [0x09]
        let sha256ObjectIdentifierValue: [UInt8] = [0x60, 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01]
        let parameterTag: [UInt8] = [0x05]
        let nullParameter: [UInt8] = [0x00]
        let digestOctetStringTag: [UInt8] = [0x04]
        let digestOctetStringLength: [UInt8] = [0x20]
        
        var digestInfoInitial: [UInt8] = []
        digestInfoInitial += sequenceTag
        digestInfoInitial += outerSequencelength
        digestInfoInitial += sequenceTag
        digestInfoInitial += digestAlgorithmSequencelength
        digestInfoInitial += objectIdentifierTag
        digestInfoInitial += algorithmObjectIdentifierLength
        digestInfoInitial += sha256ObjectIdentifierValue
        digestInfoInitial += parameterTag
        digestInfoInitial += nullParameter
        digestInfoInitial += digestOctetStringTag
        digestInfoInitial += digestOctetStringLength
        
        let digestInfo : [UInt8] = digestInfoInitial + hashedData
        return digestInfo
    }
    
    func sha256(data: [UInt8]) -> [UInt8] {
        let inputData = Data(data)
        let hashedData = SHA256.hash(data: inputData)
        print("sha256 call")
        print(hashedData)
        return Array(hashedData)
    }
    
    internal func getDigitalCertificateForUserVerification(_ session: NFCTagReaderSession,_ individualNumberCard: IndividualNumberCard)-> IndividualNumberCard{
        var individualNumberCard = individualNumberCard
        let tag = individualNumberCard.tag
        let semaphore = DispatchSemaphore(value: 0)
        
        self.selectJPKIAP(tag: tag){ (responseData, sw1, sw2, error) in
            // 公的個人認証APを選択
            self.printData(responseData, sw1, sw2)
            
            if self.tryHandleAPDUError(ADPUPattern: "SELECT TextAP", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                return
            }
            
            self.selectEF(tag: tag, data: [0x00, 0x0A]) { (responseData, sw1, sw2, error) in
                // 公的個人認証用証明書を選択する
                self.printData(responseData, sw1, sw2)
                
                if self.tryHandleAPDUError(ADPUPattern: "SELECT EF", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                    return
                }
                self.readBinary(tag: tag, p1Parameter: 0x00, p2Parameter: 0x00, expectedResponseLength: 4) { (responseData, sw1, sw2, error) in
                    // 証明書のサイズを読み取り（初めの4バイトが長さ）
                    self.printData(responseData, sw1, sw2)
                    
                    if self.tryHandleAPDUError(ADPUPattern: "READ BINARY", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                        return
                    }
                    
                    let expectedResponseLength :Int = self.getSizeIntByResposeData(responseData: responseData) + 4
                    
                    self.readBinary(tag: tag, p1Parameter: 0x00, p2Parameter: 0x00, expectedResponseLength: expectedResponseLength) { (responseData, sw1, sw2, error) in
                        // サイズ分のREAD BINARYを行う
                        self.printData(responseData, sw1, sw2)
                        
                        if self.tryHandleAPDUError(ADPUPattern: "READ BINARY", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                            return
                        }
                        
                        let digitalCertificate = [UInt8](responseData)
                        individualNumberCard.data.digitalCertificateForUserVerification = digitalCertificate
                        semaphore.signal()
                    }
                    
                }
            }
        }
        
        semaphore.wait()
        return individualNumberCard
        
    }
    
    //署名用証明書を読みこむ
    internal func getDigitalCertificateForSignature(_ session: NFCTagReaderSession,_ individualNumberCard: IndividualNumberCard, userAuthenticationPIN: [UInt8])-> IndividualNumberCard{
        var individualNumberCard = individualNumberCard
        let tag = individualNumberCard.tag
        let semaphore = DispatchSemaphore(value: 0)
        
        self.selectJPKIAP(tag: tag){ (responseData, sw1, sw2, error) in
            // 公的個人認証APを選択
            self.printData(responseData, sw1, sw2)
            
            if self.tryHandleAPDUError(ADPUPattern: "SELECT TextAP", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                return
            }
            
            // ！！！！！！PIN解除が必要！！！！！！！！
            self.selectEF(tag: tag, data: [0x00, 0x1B]) { (responseData, sw1, sw2, error) in
                // 認証用PINを選択する
                self.printData(responseData, sw1, sw2)
                
                if self.tryHandleAPDUError(ADPUPattern: "SELECT EF", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                    return
                }
                
                self.verify(tag: tag, pin: userAuthenticationPIN) { (responseData, sw1, sw2, error) in
                    // 認証用パスワードを入力してセキュリティステータスを更新する
                    self.printData(responseData, sw1, sw2)
                    
                    if self.tryHandleAPDUError(ADPUPattern: "VERIFY", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                        return
                    }
                    
                    // 署名用証明書を読みこむ
                    self.selectEF(tag: tag, data: [0x00, 0x01]) { (responseData, sw1, sw2, error) in
                        // 公的個人認証用証明書を選択する
                        self.printData(responseData, sw1, sw2)
                        
                        if self.tryHandleAPDUError(ADPUPattern: "SELECT EF", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                            return
                        }
                        // PIN解除済み
                        self.readBinary(tag: tag, p1Parameter: 0x00, p2Parameter: 0x00, expectedResponseLength: 4) { (responseData, sw1, sw2, error) in
                            // 証明書のサイズを読み取り（初めの4バイトが長さ）
                            self.printData(responseData, sw1, sw2)
                            
                            if self.tryHandleAPDUError(ADPUPattern: "READ BINARY", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                                return
                            }
                            
                            let expectedResponseLength :Int = self.getSizeIntByResposeData(responseData: responseData) + 4
                            
                            self.readBinary(tag: tag, p1Parameter: 0x00, p2Parameter: 0x00, expectedResponseLength: expectedResponseLength) { (responseData, sw1, sw2, error) in
                                // サイズ分のREAD BINARYを行う
                                self.printData(responseData, sw1, sw2)
                                
                                if self.tryHandleAPDUError(ADPUPattern: "READ BINARY", session: session, error: error, sw1: sw1, sw2: sw2) == true {
                                    return
                                }
                                
                                let digitalCertificate = [UInt8](responseData)
                                individualNumberCard.data.digitalCertificateForUserVerification = digitalCertificate
                                semaphore.signal()
                            }
                            
                        }
                    }
                }
            }
        }
        
        semaphore.wait()
        return individualNumberCard
        
    }
    
    func getSizeArrayByResposeData(responseData : Data) -> [UInt8]{
        let responseDataArray : [UInt8] = [UInt8](responseData)
        return [responseDataArray[2],responseDataArray[3]]
    }
    
    func getSizeIntByResposeData(responseData : Data) -> Int{
        let responseDataArray : [UInt8] = getSizeArrayByResposeData(responseData :responseData)
        return (Int(responseDataArray[0]) << 8) | Int(responseDataArray[1])
    }
    
    private func tryHandleAPDUError(ADPUPattern: String,session: NFCTagReaderSession,error: Error?,sw1: UInt8,sw2: UInt8)-> Bool{
        if let error = error {
            print(error.localizedDescription)
            session.invalidate(errorMessage: ADPUPattern + "\n\(error.localizedDescription)")
            self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
            return true
        }
        
        if sw1 != 0x90 {
            if ADPUPattern == "VERIFY" && sw1 == 0x63 {
                var error = IndividualNumberReaderError.incorrectPIN(0)
                switch sw2 {
                case 0xC1:
                    error = .incorrectPIN(1)
                case 0xC2:
                    error = .incorrectPIN(2)
                case 0xC3:
                    error = .incorrectPIN(3)
                case 0xC4:
                    error = .incorrectPIN(4)
                case 0xC5:
                    error = .incorrectPIN(5)
                default:
                    break
                }
                print("PIN エラー", error)
                self.delegate?.japanNFCReaderSession(didInvalidateWithError: error)
            }
            session.invalidate(errorMessage: "エラー: ステータス: \(ISO7816Status.localizedString(forStatusCode: sw1, sw2))")
            return true
        }
        return false
    }
}

#endif
