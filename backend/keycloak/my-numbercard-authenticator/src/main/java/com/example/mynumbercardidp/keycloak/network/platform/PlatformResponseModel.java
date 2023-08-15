package com.example.mynumbercardidp.keycloak.network.platform;

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
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class PlatformResponseModel {

    @JsonIgnore
    private int httpStatusCode;
    private PlatformResponseModel.ResponseInfo responseInfo = new ResponseInfo();
    private PlatformResponseModel.Status status = new Status();
    private PlatformResponseModel.IdentityInfo identityInfo = new IdentityInfo();
    private String applicantId;

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * プラットフォームレスポンスにユーザーの一意なIDが存在することを保証します。
     *
     * 存在しない場合は IllegalStateException を送出します。
     *
     * @exception IllegalStateException プラットフォームレスポンスにユーザーの一意のIDが存在しない場合
     */
    @JsonIgnore
    public void ensureHasUniqueId() {
        String uniqueId = identityInfo.getUniqueId();
        if (StringUtil.isEmpty(uniqueId)) {
            throw new IllegalStateException("The unique id is empty in platform response.");
        }
    }

    /**
     * プラットフォームのレスポンスからKeycloakのユーザー属性を追加、更新します。
     *
     * @param user Keycloak ユーザーのデータ構造体インスタンス
     */
    @JsonIgnore
    public UserModel toUserModelAttributes(final UserModel user) {
        user.setSingleAttribute("uniqueId", identityInfo.getUniqueId());
        user.setSingleAttribute("name", identityInfo.getName());
        user.setSingleAttribute("gender_code", identityInfo.getGender());
        user.setSingleAttribute("user_address", identityInfo.getAddress());
        user.setSingleAttribute("birth_date", identityInfo.getDateOfBirth());
        return user;
    }

    protected void setHttpStatusCode(final int status) {
        httpStatusCode = status;
    }

    @JsonIgnore
    public String getUniqueId() {
        return identityInfo.uniqueId;
    }

    public PlatformResponseModel.IdentityInfo getIdentityInfo() {
        return identityInfo;
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
            return uniqueId;
        }

        public String getName() {
            return name;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public String getGender() {
            return gender;
        }

        public String getAddress() {
            return address;
        }
    }
}
