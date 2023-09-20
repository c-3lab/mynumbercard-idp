package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;

import javax.ws.rs.core.Response;

/**
 * プラットフォームが応答したステータスコードによる登録情報更新処理の遷移を定義するクラスです。
 */
public class ReplacementFlowTransition extends CommonFlowTransition {
    protected boolean canExecuteReplacement(final AuthenticationFlowContext context, final Response.Status status) {
        if (super.canExecuteUserAction(context, status)) {
            return true;
        }
        switch (status) {
            case UNAUTHORIZED:
                ResponseCreater.sendInvalidRequestResponse(context, status);
                return false;
            case GONE:
                ResponseCreater.sendChallengeResponse(context, ActionType.REPLACEMENT.getName(), status);
                return false;
        }
        super.throwExceptionByUndefinedActionFlow(ActionType.REPLACEMENT.getName(), status.getStatusCode());
        return false;
    }
}
