package com.example.mynumbercardidp.keycloak.network.platform;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * プラットフォームAPIへ送信するリクエスト内容の構造体を表すクラスです。
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class PlatformRequestModel extends CommonRequestModel {

    @JsonProperty("requestInfo")
    private RequestInfo requestInfo;

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    protected PlatformRequestModel(final String sender) {
        this.requestInfo = new RequestInfo(sender);
    }

    protected PlatformRequestModel setRequestInfo(final RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class RequestInfo {
        private String transactionId = UUID.randomUUID().toString();
        private String recipient = "JPKI";
        private String sender;
        @JsonProperty("ts")
        private String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS").toString();

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
