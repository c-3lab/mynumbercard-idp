package com.example.mynumbercardidp.data

enum class Rfc3447HashPrefix() {
    // RSA-MD2ハッシュのプレフィックス
    MD2 { override fun toString(): String { return "3020300c06082a864886f70d020205000410" } },
    // RSA-MD5ハッシュのプレフィックス
    MD5 { override fun toString(): String { return "3020300c06082a864886f70d020505000410" } },
    // RSA-SHA1ハッシュのプレフィックス
    SHA1 { override fun toString(): String { return "3021300906052b0e03021a05000414" } },
    // RSA-SHA256ハッシュのプレフィックス
    SHA256 { override fun toString(): String { return "3031300d060960864801650304020105000420" } },
    // RSA-SHA384ハッシュのプレフィックス
    SHA384 { override fun toString(): String { return "3041300d060960864801650304020205000430" } },
    // RSA-SHA512ハッシュのプレフィックス
    SHA512 { override fun toString(): String { return "3051300d060960864801650304020305000440" } },
}

enum class URLTypes() {
    Inquiry {override fun toString(): String { return "https://www.kojinbango-card.go.jp/contact/" }},
    PrivacyPolicy {override fun toString(): String { return "https://example.com/open-id/privacy-policy.html" }},
    ProtectionPolicy {override fun toString(): String { return "https://example.com/open-id/personal-data-protection-policy.html" }},
    TermsOfUse {override fun toString(): String { return "https://example.com/open-id/terms-of-use.html" }},
}

enum class ValidInputText(val length: Int) {
    CertForUserVerification(4),
    CertForSign(6),
}

enum class MaxInputText(val length: Int) {
    CertForUserVerification(4),
    CertForSign(16),
}

enum class HttPStatusCode(val value: Int) {
    Found(302),
    BadRequest(400),
    Unauthorized(401),
    NotFound(404),
    Conflict(409),
    Gone(410),
    InternalServerError(500),
    ServiceUnavailable(503),
}
