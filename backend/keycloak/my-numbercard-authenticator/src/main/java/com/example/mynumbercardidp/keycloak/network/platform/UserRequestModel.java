package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.util.StringUtil;

import java.util.Objects;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容の構造体を表すクラスです。
 */
public class UserRequestModel extends CommonRequestModel {
    public static enum Filed {
        ACTION_MODE("mode"),
        USER_AUTHENTICATION_CERTIFICATE(CertificateType.USER_AUTHENTICATION.getName()),
        ENCRYPTED_DIGITAL_SIGNATURE_CERTIFICATE(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE.getName()),
        APPLICANT_DATA("applicantData"),
        SIGN("sign");

        private String name;

        private Filed(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public String actionMode;

    public String getActionMode() {
        return actionMode;
    }

    public UserRequestModel setActionMode(String mode) {
        actionMode = mode;
        return this;
    }

    /**
     * フィールドに値が存在することを保証します。
     *
     * 1つ以上のフィールドでNullまたは空値があった場合は例外を送出します。
     * @exception IllegalStateException 1つ以上のフィールドでNullまたは空値があった場合
     */
    public void ensureHasValues() {
        if (Objects.isNull(certificateType) ||
            StringUtil.isStringEmpty(actionMode) ||
            StringUtil.isStringEmpty(certificate) ||
            StringUtil.isStringEmpty(applicantData) ||
            StringUtil.isStringEmpty(sign)) {
            throw new IllegalStateException("One or more values is not set.");
        }
    }
}

