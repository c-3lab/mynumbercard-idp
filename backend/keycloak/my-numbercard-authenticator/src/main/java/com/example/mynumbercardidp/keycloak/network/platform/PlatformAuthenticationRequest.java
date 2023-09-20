package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * プラットフォームAPIへ送信するリクエスト内容の構造体を表すクラスです。
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class PlatformAuthenticationRequest {
    @JsonIgnore
    private CertificateType certificateType;
    @JsonIgnore
    private String certificate;
    private String applicantData;
    private String sign;
    @JsonProperty("requestInfo")
    private RequestInfo requestInfo;

    protected PlatformAuthenticationRequest(final String sender) {
        this.requestInfo = new RequestInfo(sender);
    }

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

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    protected PlatformAuthenticationRequest setCertificateType(final CertificateType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    protected PlatformAuthenticationRequest setCertificate(final String certificate) {
        this.certificate = certificate;
        return this;
    }

    protected PlatformAuthenticationRequest setApplicantData(final String applicantData) {
        this.applicantData = applicantData;
        return this;
    }

    protected PlatformAuthenticationRequest setSign(final String sign) {
        this.sign = sign;
        return this;
    }

    protected PlatformAuthenticationRequest setRequestInfo(final RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class RequestInfo {
        private String transactionId = UUID.randomUUID().toString();
        private String recipient = "JPKI";
        private String sender;
        @JsonProperty("ts")
        private String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS").format(new Date());

        protected RequestInfo(final String sender) {
            this.sender = sender;
        }

        public String getTransactionId() {
            return this.transactionId;
        }

        public String getRecipient() {
            return this.recipient;
        }

        public String getSender() {
            return this.sender;
        }

        public String getTimeStamp() {
            return this.timeStamp;
        }

        protected RequestInfo setSender(final String sender) {
            this.sender = sender;
            return this;
        }
    }
}
