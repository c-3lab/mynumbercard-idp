package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;

import javax.ws.rs.core.Response;

/**
 * プラットフォームが応答したステータスコードによる認証処理の遷移を定義するクラスです。
 */
public class LoginFlowTransition extends CommonFlowTransition {
    private static final String ACTION_NAME = ActionType.LOGIN.getName();

    @Override
    protected boolean canAction(final AuthenticationFlowContext context, final Response.Status status) {
        if (super.canAction(context, status)) {
            return true;
        }
        switch (status) {
            case NOT_FOUND:
                ResponseCreater.actionRegistrationChallenge(context);
                return false;
            case UNAUTHORIZED:
                ResponseCreater.actionUnauthorized(context);
                return false;
            case GONE:
                String replacementActionName = ActionType.REPLACEMENT.getName();
                ResponseCreater.actionReChallenge(context, replacementActionName, status.getStatusCode());
                return false;
        }
        super.actionUndefinedFlow(LoginFlowTransition.ACTION_NAME, status.getStatusCode());
        return false;
    }
}
