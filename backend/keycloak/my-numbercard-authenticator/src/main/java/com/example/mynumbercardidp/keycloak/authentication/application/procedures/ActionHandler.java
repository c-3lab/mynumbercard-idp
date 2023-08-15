package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.core.authentication.application.procedures.AbstractActionHandler;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出すクラスです。
 */
public class ActionHandler extends AbstractActionHandler {

    public ActionHandler() {
        userActionPackageName = "com.example.mynumbercardidp.keycloak.authentication.application.procedures.user";
    }
}
