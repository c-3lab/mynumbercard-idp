package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformResponseModelImpl;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.models.UserModel;

/**
 * プラットフォームのレスポンス構造体です。
 *
 * Jacksonによるオブジェクト、JSON間の相互変換することができるデータ定義です。
 */
// [NOTE] 参照されないフィールドのゲッターとセッターは書かない。必要になった場合に書く。
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class PlatformResponseModel implements PlatformResponseModelImpl {

    @JsonIgnore
    private int httpStatusCode;
    private PlatformResponseModel.ResponseInfo responseInfo = new ResponseInfo();
    private PlatformResponseModel.Status status = new Status();
    private PlatformResponseModel.IdentityInfo identityInfo = new IdentityInfo();
    private String applicantId;

    @Override
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    @Override
    @JsonIgnore
    public void ensureHasUniqueId() {
        String uniqueId = this.identityInfo.getUniqueId();
        if (StringUtil.isEmpty(uniqueId)) {
            throw new IllegalStateException("The unique id in the platform response was empty.");
        }
    }

    @Override
    @JsonIgnore
    public UserModel toUserModelAttributes(final UserModel user) {
        user.setSingleAttribute("uniqueId", this.identityInfo.getUniqueId());
        user.setSingleAttribute("name", this.identityInfo.getName());
        user.setSingleAttribute("gender_code", this.identityInfo.getGender());
        user.setSingleAttribute("user_address", this.identityInfo.getAddress());
        user.setSingleAttribute("birth_date", this.identityInfo.getDateOfBirth());
        return user;
    }

    protected void setHttpStatusCode(final int status) {
        this.httpStatusCode = status;
    }

    @Override
    @JsonIgnore
    public String getUniqueId() {
        return this.identityInfo.uniqueId;
    }

    public PlatformResponseModel.IdentityInfo getIdentityInfo() {
        return this.identityInfo;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class ResponseInfo {
        private String transactionId;
        private String recipient;
        private String sender;
        @JsonProperty("ts")
        private String timeStamp;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String status;
        private String errorInfoReason;
        private String message;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static class IdentityInfo {
        private String uniqueId;
        private String name;
        private String dateOfBirth;
        private String gender;
        private String address;

        public String getUniqueId() {
            return this.uniqueId;
        }

        public String getName() {
            return this.name;
        }

        public String getDateOfBirth() {
            return this.dateOfBirth;
        }

        public String getGender() {
            return this.gender;
        }

        public String getAddress() {
            return this.address;
        }
    }
}
