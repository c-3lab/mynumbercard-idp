package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
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
     * @param platform プラットフォーム APIクライアントのインスタンス
     @ @return ユーザーが希望する処理を実行できる場合はtrue、そうでない場合はfalse
     */
    protected boolean canAction(final AuthenticationFlowContext context, final Response.Status status) {
        switch (status) {
            case OK:
                CommonFlowTransition.consoleLogger.debug("Platform response status code: " + status);
                return true;
            case BAD_REQUEST:
                context.form().setError(Messages.INVALID_REQUEST, "");
                Response response = ResponseCreater.createChallengePage(context, status.getStatusCode());
                ResponseCreater.setFlowStepChallenge(context, response);
                return false;
            case INTERNAL_SERVER_ERROR:
                // フォールスルー
            case SERVICE_UNAVAILABLE:
                String statusLabel = status.toString() + " (" + status + ")";
                CommonFlowTransition.consoleLogger.error("Platform response status: " + statusLabel);
                CommonFlowTransition.consoleLogger.error("Make sure the platform API server status is running.");
                throw new IllegalArgumentException("Platform status is " + statusLabel + ".");
        }
        return false;
    }

    /**
     * ユーザーが希望する処理の中で未定義のフローに到達した旨の例外を送出します。
     *
     * @param actionmName ユーザーが希望する処理の種類
     * @exception IllegalArgumentException 未定義のフローが呼ばれた場合
     */
    protected void actionUndefinedFlow(final String actionName, final int status) {
        String message = "Received an invalid status code. Status " + status + " is the undefined " + actionName + " flow.";
        throw new IllegalArgumentException(message);
    }
}
