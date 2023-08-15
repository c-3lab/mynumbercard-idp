package com.example.mynumbercardidp.keycloak.network.platform;

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
}

