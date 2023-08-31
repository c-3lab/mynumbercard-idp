package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.services.messages.Messages;
import org.jboss.logging.Logger;

import javax.ws.rs.core.Response;

/**
 * プラットフォームのステータスコードによる処理の遷移を定義するクラスです。
 */
public abstract class CommonFlowTransition {
    /** コンソールロガー */
    private static Logger consoleLogger = Logger.getLogger(CommonFlowTransition.class);

    /**
     * ユーザーが希望する処理を実行できるかどうか返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status  HTTP ステータスコード
     * @return ユーザーが希望する処理を実行できる場合はtrue、そうでない場合はfalse
     */
    protected boolean canExecuteUserAction(final AuthenticationFlowContext context, final Response.Status status) {
        switch (status) {
            case OK:
                return true;
            case BAD_REQUEST:
                ResponseCreater.setLoginFormAttributes(context);
                Response response = ResponseCreater.createChallengePage(context, Messages.INVALID_REQUEST, null,
                        status);
                context.challenge(response);
                return false;
            case INTERNAL_SERVER_ERROR:
                // フォールスルー
            case SERVICE_UNAVAILABLE:
                CommonFlowTransition.consoleLogger.error("Platform response status: " + status.getStatusCode());
                CommonFlowTransition.consoleLogger.error("Make sure the platform API server status is running.");
                throw new IllegalArgumentException("Platform status is " + status.toString() + ".");
        }
        return false;
    }

    /**
     * ユーザーが希望する処理の中で未定義のフローに到達した旨の例外を送出します。
     *
     * @param actionName ユーザーが希望する処理の種類
     * @param status     HTTP ステータスコード
     * @exception IllegalArgumentException 未定義のフローが呼ばれた場合
     */
    protected void throwExceptionByUndefinedActionFlow(final String actionName, final int status) {
        throw new IllegalArgumentException(
                "Received an invalid status code. Status " + status + " is the undefined " + actionName + " flow.");
    }
}
