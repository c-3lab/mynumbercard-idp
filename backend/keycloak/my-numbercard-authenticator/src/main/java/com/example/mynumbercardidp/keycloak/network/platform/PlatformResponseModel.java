package com.example.mynumbercardidp.keycloak.network.platform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * プラットフォームのレスポンス構造体です。
 *
 * Jacksonによるオブジェクト、JSON間の相互変換することができるデータ定義です。
 */
public class PlatformResponseModel {

    private int httpStatusCode;

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    @JsonIgnore
    public void setHttpStatusCode(int status) {
        httpStatusCode = status;
    }

    static class ResponseInfo {
        public String transactionId;
        public String recipient;
        public String sender;
        @JsonProperty("ts")
        public String timeStamp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Status {
        public String status;
        public String errorInfoReason;
        public String message;
    }

    static class IdentityInfo {
        public String uniqueId;
        public String name;
        public String dateOfBirth;
        public String gender;
        public String address;
    }

    public ResponseInfo responseInfo = new ResponseInfo();
    public Status status = new Status();
    public IdentityInfo identityInfo = new IdentityInfo();
    public String applicantId = "";

    @JsonIgnore
    public String getUniqueId() {
        return identityInfo.uniqueId;
    }
}
