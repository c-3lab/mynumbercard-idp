package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;

import javax.ws.rs.core.Response;

/**
 * プラットフォームが応答したステータスコードによる登録情報更新処理の遷移を定義するクラスです。
 */
public class ReplacementFlowTransition extends CommonFlowTransition {
    private static final String ACTION_NAME = ActionType.REPLACEMENT.getName();

    @Override
    protected boolean canAction(final AuthenticationFlowContext context, final Response.Status status) {
        if (super.canAction(context, status)) {
            return true;
        }
        switch (status) {
        case UNAUTHORIZED:
            ResponseCreater.actionUnauthorized(context);
            return false;
        case CONFLICT:
            // [TODO] この画面遷移、本当に合っている？
            String loginActionName = ActionType.LOGIN.getName();
            ResponseCreater.actionReChallenge(context, loginActionName, status.getStatusCode());
            return false;
        }
        super.actionUndefinedFlow(ACTION_NAME, status.getStatusCode());
        return false;
    }
}
