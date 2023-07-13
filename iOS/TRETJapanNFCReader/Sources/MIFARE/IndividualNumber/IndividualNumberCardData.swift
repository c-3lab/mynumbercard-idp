//
//  IndividualNumberCardData.swift
//  TRETJapanNFCReader
//
//  Created by treastrain on 2020/05/11.
//  Copyright © 2020 treastrain / Tanaka Ryoga. All rights reserved.
//

import Foundation

/// マイナンバーカードのデータ
public struct IndividualNumberCardData {
    /// トークン情報
    public var token: String?
    /// マイナンバー
    public var individualNumber: String?
    /// 利用者証明用電子証明書
    public var digitalCertificateForUserVerification: [UInt8]?
    /// 利用者証明用電子署名
    public var digitalSignatureForUserVerification: [UInt8]?
    /// 暗証番号残り試行回数
    public var remaining: Int?
}
