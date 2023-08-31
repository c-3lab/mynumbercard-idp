package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;

import javax.ws.rs.core.Response;

/**
 * プラットフォームが応答したステータスコードによる認証処理の遷移を定義するクラスです。
 */
public class LoginFlowTransition extends CommonFlowTransition {
    protected boolean canExecuteAuthentication(final AuthenticationFlowContext context, final Response.Status status) {
        if (super.canExecuteUserAction(context, status)) {
            return true;
        }
        switch (status) {
            case NOT_FOUND:
                ResponseCreater.sendChallengeResponse(context, ActionType.REGISTRATION.getName(), status);
                return false;
            case UNAUTHORIZED:
                ResponseCreater.sendInvalidRequestResponse(context, status);
                return false;
            case GONE:
                context.getAuthenticationSession().setAuthNote("reuseNonceFlag", Boolean.TRUE.toString());
                ResponseCreater.sendChallengeResponse(context, ActionType.REPLACEMENT.getName(), status);
                return false;
        }
        super.throwExceptionByUndefinedActionFlow(ActionType.LOGIN.getName(), status.getStatusCode());
        return false;
    }
}
