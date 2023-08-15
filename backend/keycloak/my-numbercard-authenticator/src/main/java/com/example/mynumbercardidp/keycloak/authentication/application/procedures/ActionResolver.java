package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.core.authentication.application.procedures.AbstractActionResolver;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import org.keycloak.authentication.AuthenticationFlowContext;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出すクラスです。
 */
public class ActionResolver extends AbstractActionResolver {
    private static final String USER_ACTION_PACKAGE_NAME = "com.example.mynumbercardidp.keycloak.authentication.application.procedures.user";

    public ActionResolver() {
        super(USER_ACTION_PACKAGE_NAME);
    }

    @Override
    public void onAction(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        ResponseCreater.setLoginFormAttributes(context);
        super.onAction(context, platform);
    }
}
