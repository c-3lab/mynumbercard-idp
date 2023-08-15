package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.network.CommonResponseModel;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformResponseModel extends CommonResponseModel {

    class ResponseInfo {
        public String transactionId;
        public String recipient;
        public String sender;

        @JsonProperty("ts")
        public String timeStamp;
    }

    class Status {
        public int statusCode;
        public String errorInfoReason;
        public String message;
    }

    class IdentityInfo {
        public String uniqueId;
        public String name;
        public String dateOfBirth;
        public String gender;
        public String address;
    }

    ResponseInfo responseInfo;
    Status status;
    IdentityInfo identityInfo;
    String applicantId;

    {
        uniqueId = identityInfo.uniqueId;
    }

}
