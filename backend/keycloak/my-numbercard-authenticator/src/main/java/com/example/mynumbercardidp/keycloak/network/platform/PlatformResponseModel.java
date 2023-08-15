package com.example.mynumbercardidp.keycloak.network.platform;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * プラットフォームのレスポンス構造体です。
 *
 * Jacksonによるオブジェクト、JSON間の相互変換することができるデータ定義です。
 */
@JsonAutoDetect(fieldVisibility = Visibility.PROTECTED_AND_PUBLIC)
public class PlatformResponseModel {

    @JsonIgnore
    protected int httpStatusCode;

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    protected void setHttpStatusCode(int status) {
        httpStatusCode = status;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.PROTECTED_AND_PUBLIC)
    public static class ResponseInfo {
        protected String transactionId;
        protected String recipient;
        protected String sender;
        @JsonProperty("ts")
        protected String timeStamp;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.PROTECTED_AND_PUBLIC)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        protected String status;
        protected String errorInfoReason;
        protected String message;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.PROTECTED_AND_PUBLIC)
    public static class IdentityInfo {
        protected String uniqueId;
        protected String name;
        protected String dateOfBirth;
        protected String gender;
        protected String address;

        public String getName() {
            return name;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public String getGender() {
            return dateOfBirth;
        }

        public String getAddress() {
            return address;
        }
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
