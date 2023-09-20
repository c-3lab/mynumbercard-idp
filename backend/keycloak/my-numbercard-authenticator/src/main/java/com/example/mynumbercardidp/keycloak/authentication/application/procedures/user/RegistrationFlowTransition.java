package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;

import javax.ws.rs.core.Response;

/**
 * プラットフォームが応答したステータスコードによる登録処理の遷移を定義するクラスです。
 */
public class RegistrationFlowTransition extends CommonFlowTransition {
    protected boolean canExecuteRegistration(final AuthenticationFlowContext context, final Response.Status status) {
        if (super.canExecuteUserAction(context, status)) {
            return true;
        }
        switch (status) {
            case UNAUTHORIZED:
                ResponseCreater.sendInvalidRequestResponse(context, status);
                return false;
            case CONFLICT:
                ResponseCreater.sendChallengeResponse(context, ActionType.LOGIN.getName(), status);
                return false;
        }
        super.throwExceptionByUndefinedActionFlow(ActionType.REGISTRATION.getName(), status.getStatusCode());
        return false;
    }
}
