package com.example.mynumbercardidp.keycloak.network.platform;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容とプラットフォームへ送信するHTTPリクエスト内容で共通する構造体を表すクラスです。
 */
public class CommonRequestModel implements CommonRequestModelImpl {

    protected CertificateType certificateType;
    protected String certificate;
    protected String applicantData;
    protected String sign;

    public CommonRequestModel() {}

    @Override
    public CertificateType getCertificateType() {
        return certificateType;
    }

    @Override
    public CommonRequestModelImpl setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    @Override
    public String getCertificate() {
        return certificate;
    }

    @Override
    public CommonRequestModelImpl setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }

    @Override
    public String getApplicantData() {
        return applicantData;
    }

    @Override
    public CommonRequestModelImpl setApplicantData(String applicantData) {
        this.applicantData = applicantData;
        return this;
    }

    @Override
    public String getSign() {
        return sign;
    }

    @Override
    public CommonRequestModelImpl setSign(String sign) {
        this.sign = sign;
        return this;
    }
}
