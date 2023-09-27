package com.example.mynumbercardidp.keycloak.core.network;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.util.StringUtil;

import java.util.Objects;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容の構造体を表すクラスです。
 */
public class AuthenticationRequest {

    private CertificateType certificateType;
    private String certificate;
    private String applicantData;
    private String nonceData;
    private String sign;
    private String nonceSign;
    private String actionMode;

    public CertificateType getCertificateType() {
        return this.certificateType;
    }

    public String getCertificate() {
        return this.certificate;
    }

    public String getApplicantData() {
        return this.applicantData;
    }

    public String getNonceData() {
        return this.nonceData;
    }

    public String getSign() {
        return this.sign;
    }

    public String getNonceSign() {
        return this.nonceSign;
    }

    public AuthenticationRequest setCertificateType(final CertificateType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    public AuthenticationRequest setCertificate(final String certificate) {
        this.certificate = certificate;
        return this;
    }

    public AuthenticationRequest setApplicantData(final String applicantData) {
        this.applicantData = applicantData;
        return this;
    }

    public AuthenticationRequest setNonceData(final String nonceData) {
        this.nonceData = nonceData;
        return this;
    }

    public AuthenticationRequest setSign(final String sign) {
        this.sign = sign;
        return this;
    }

    public AuthenticationRequest setNonceSign(final String nonceSign) {
        this.nonceSign = nonceSign;
        return this;
    }

    public final String getActionMode() {
        return this.actionMode;
    }

    public final void setActionMode(final String mode) {
        this.actionMode = mode;
    }

    /**
     * すべてのフィールドに値が存在することを検証します。
     *
     * 1つ以上のフィールドでNullまたは許容されていない空値があった場合は例外を送出します。
     *
     * @exception IllegalStateException 1つ以上のフィールドでNullまたは空値があった場合
     */
    public void validateHasValues() {
        if (Objects.isNull(this.certificateType) ||
                StringUtil.isEmpty(this.actionMode) ||
                StringUtil.isEmpty(this.certificate) ||
                (StringUtil.isEmpty(this.applicantData) &&
                StringUtil.isEmpty(this.nonceData)) ||
                (StringUtil.isEmpty(this.sign) &&
                StringUtil.isEmpty(this.nonceSign))) {
            throw new IllegalStateException("One or more values were not set.");
        }
    }

    public static enum Filed {
        ACTION_MODE("mode"),
        ENCRYPTED_USER_AUTHENTICATION_CERTIFICATE(CertificateType.ENCRYPTED_USER_AUTHENTICATION.getName()),
        ENCRYPTED_DIGITAL_SIGNATURE_CERTIFICATE(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE.getName()),
        APPLICANT_DATA("applicantData"),
        SIGN("sign");

        private String name;

        private Filed(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
