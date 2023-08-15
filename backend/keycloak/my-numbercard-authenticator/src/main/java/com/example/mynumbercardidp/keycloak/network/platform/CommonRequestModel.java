package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容とプラットフォームへ送信するHTTPリクエスト内容で共通する構造体を表すクラスです。
 */
@JsonAutoDetect(fieldVisibility = Visibility.PROTECTED_AND_PUBLIC)
public class CommonRequestModel {

    @JsonIgnore
    protected CertificateType certificateType;
    @JsonIgnore
    protected String certificate;
    protected String applicantData;
    protected String sign;

    public CertificateType getCertificateType() {
        return certificateType;
    }

    protected CommonRequestModel setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    public String getCertificate() {
        return certificate;
    }

    protected CommonRequestModel setCertificate(String certificate) {
        this.certificate = certificate;
        return this;
    }

    public String getApplicantData() {
        return applicantData;
    }

    protected CommonRequestModel setApplicantData(String applicantData) {
        this.applicantData = applicantData;
        return this;
    }

    public String getSign() {
        return sign;
    }

    protected CommonRequestModel setSign(String sign) {
        this.sign = sign;
        return this;
    }
}
