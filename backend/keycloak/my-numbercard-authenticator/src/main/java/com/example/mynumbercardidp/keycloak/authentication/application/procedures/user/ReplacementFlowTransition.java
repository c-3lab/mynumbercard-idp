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
            case GONE:
                // 登録情報の変更で証明書が失効しているのにもう一度登録情報の変更を呼び出す...？
                // String replacementActionName = ActionType.REPLACEMENT.getName();
                // ResponseCreater.actionReChallenge(context, replacementActionName, status.getStatusCode());
                // return false;
                break;
        }
        super.actionUndefinedFlow(ReplacementFlowTransition.ACTION_NAME, status.getStatusCode());
        return false;
    }
}
