package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容とプラットフォームへ送信するHTTPリクエスト内容で共通する構造体を表すクラスです。
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class CommonRequestModel {

    @JsonIgnore
    private CertificateType certificateType;
    @JsonIgnore
    private String certificate;
    private String applicantData;
    private String sign;

    public CertificateType getCertificateType() {
        return this.certificateType;
    }

    public String getCertificate() {
        return this.certificate;
    }

    public String getApplicantData() {
        return this.applicantData;
    }

    public String getSign() {
        return this.sign;
    }

    protected CommonRequestModel setCertificateType(final CertificateType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    protected CommonRequestModel setCertificate(final String certificate) {
        this.certificate = certificate;
        return this;
    }

    protected CommonRequestModel setApplicantData(final String applicantData) {
        this.applicantData = applicantData;
        return this;
    }

    protected CommonRequestModel setSign(final String sign) {
        this.sign = sign;
        return this;
    }
}
