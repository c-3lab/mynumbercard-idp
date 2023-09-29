package com.example.mynumbercardidp.keycloak.core.network.platform;

/**
 * 個人番号カード内にある公的証明書の種類を表します。
 *
 * ENCRYPTED_USER_AUTHENTICATION               利用者証明用証明書
 * ENCRYPTED_USER_AUTHENTICATION_FOR_PLATFORM  利用者証明用証明書（プラットフォーム通信用の名称）
 * ENCRYPTED_DIGITAL_SIGNATURE                 署名用証明書
 * ENCRYPTED_DIGITAL_SIGNATURE_FOR_PLATFORM    署名用証明書（プラットフォーム通信用の名称）
 */
public enum CertificateType {
    ENCRYPTED_USER_AUTHENTICATION("encryptedUserAuthenticationCertificate"),
    ENCRYPTED_USER_AUTHENTICATION_FOR_PLATFORM("encryptedCertificateForUser"),
    ENCRYPTED_DIGITAL_SIGNATURE("encryptedDigitalSignatureCertificate"),
    ENCRYPTED_DIGITAL_SIGNATURE_FOR_PLATFORM("encryptedCertificateForSign");

    /** HTMLフォームデータのパラメータ名 */
    private String name;

    private CertificateType(final String formName) {
        this.name = formName;
    }

    public String getName() {
        return this.name;
    }
}
