package com.example.mynumbercardidp.keycloak.network.platform;

import org.keycloak.forms.login.LoginFormsProvider;

/**
 * プラットフォームからの応答のうち、共通で利用される部分を構造化したクラスです。
 */
public class CommonResponseModel {

    private int httpStatusCode;

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int status) {
        httpStatusCode = status;
    }
}
