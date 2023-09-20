package com.example.mynumbercardidp.keycloak.core.network.platform;

/**
 * 個人番号カード内にある公的証明書の種類を表します。
 *
 * ENCRYPTED_USER_AUTHENTICATION 利用者証明用証明書
 * ENCRYPTED_DIGITAL_SIGNATURE   署名用証明書
 */
public enum CertificateType {
   ENCRYPTED_USER_AUTHENTICATION("encryptedUserAuthenticationCertificate"),
   ENCRYPTED_DIGITAL_SIGNATURE("encryptedDigitalSignatureCertificate");

   /** HTMLフォームデータのパラメータ名 */
   private String name;

   private CertificateType(final String formName) {
       this.name = formName;
   }

   public String getName() {
       return this.name;
   }
}
