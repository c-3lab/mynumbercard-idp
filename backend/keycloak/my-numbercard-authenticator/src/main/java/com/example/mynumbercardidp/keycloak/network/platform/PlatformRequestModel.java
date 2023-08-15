package com.example.mynumbercardidp.keycloak.network.platform;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * プラットフォームAPIへ送信するリクエスト内容の構造体を表すクラスです。
 */
@JsonAutoDetect(fieldVisibility = Visibility.PROTECTED_AND_PUBLIC)
public class PlatformRequestModel extends CommonRequestModel {

    @JsonProperty("requestInfo")
    protected RequestInfo requestInfo;

    protected PlatformRequestModel(String sender) {
        requestInfo = new RequestInfo(sender);
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    protected PlatformRequestModel setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    public static class RequestInfo {
        protected String transactionId;
        protected String recipient;
        protected String sender;
        @JsonProperty("ts")
        protected String timeStamp;

        {
            transactionId = UUID.randomUUID().toString();
            recipient = "JPKI";
            timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS").toString();
        }

        protected RequestInfo(String sender) {
            this.sender = sender;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getSender() {
            return sender;
        }

        protected RequestInfo setSender(String sender) {
            this.sender = sender;
            return this;
        }

        public String getTimeStamp() {
            return timeStamp;
        }
    }
}
