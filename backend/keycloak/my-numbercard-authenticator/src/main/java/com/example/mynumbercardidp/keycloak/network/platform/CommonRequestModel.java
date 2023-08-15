package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容とプラットフォームへ送信するHTTPリクエスト内容で共通する構造体を表すクラスです。
 */
public class CommonRequestModel {

    protected CertificateType certificateType;
    protected String certificate;
    protected String applicantData;
    protected String sign;

    public CommonRequestModel() {}

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public CommonRequestModel setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    public String getCertificate() {
        return certificate;
    }

    public CommonRequestModel setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }

    public String getApplicantData() {
        return applicantData;
    }

    public CommonRequestModel setApplicantData(String applicantData) {
        this.applicantData = applicantData;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public CommonRequestModel setSign(String sign) {
        this.sign = sign;
        return this;
    }
}
