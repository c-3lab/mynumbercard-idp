package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.network.CommonRequestModel;
import com.example.mynumbercardidp.keycloak.network.CertificateType;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容の構造体を表すクラスです。
 */
public class UserRequestModel extends CommonRequestModel implements UserRequestModelImpl {
    public static enum Filed {
        ACTION_MODE("mode"),
        USER_AUTHENTICATION_CERTIFICATE(CertificateType.USER_AUTHENTICATION.name()),
        ENCRYPTED_DIGITAL_SIGNATURE_CERTIFICATE(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE.name()),
        APPLICANT_DATA("applicantData"),
        SIGN("sigin");

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

    public UserRequestModelImpl setActionMode(String mode) {
        actionMode = mode;
        return this;
    }
}

