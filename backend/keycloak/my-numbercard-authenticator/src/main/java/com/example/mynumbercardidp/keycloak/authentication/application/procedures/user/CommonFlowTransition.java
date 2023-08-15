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

    /** プラットフォームのステータスコード */
    protected int platformStatusCode = 0;

    /** サブクラスが継承できるコンソールロガー */
    protected final Logger consoleLogger = Logger.getLogger(new Object(){}.getClass());

    /**
     * ユーザーが希望する処理を実行できるかどうか返します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     @ @return ユーザーが希望する処理を実行できる場合はtrue、そうでない場合はfalse
     */
    protected boolean canAction(AuthenticationFlowContext context, int status) {
        if (status == Response.Status.OK.getStatusCode()) {
            consoleLogger.debug("Platform response status code: " + status);
            return true;
        }
        if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
            context.form().setError(Messages.INVALID_REQUEST, "");
            Response response = ResponseCreater.createChallengePage(context, status);
            ResponseCreater.setFlowStepChallenge(context, response);
            return false;
        }
        if (status == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() ||
            status == Response.Status.SERVICE_UNAVAILABLE.getStatusCode()) {
            String statusLabel =  Response.Status.fromStatusCode(platformStatusCode) + " (" + platformStatusCode + ")";
            consoleLogger.error("Platform response status: " + statusLabel);
            consoleLogger.error("Make sure the platform API server status is running.");
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
    protected void actionUndefinedFlow(String actionName) {
        throw new IllegalArgumentException("Received an invalid status code. Status " + platformStatusCode + " is the undefined " + actionName + " flow.");
    }

    /**
     * 公的電子証明書を検証できなかった画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param actionName 遷移先処理の種類
     * @param status プラットフォームのHTTPステータスコード
     */
    protected void actionUnauthorized(AuthenticationFlowContext context) {
        context.form().setError(Messages.INVALID_REQUEST, "");
        context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
                        context.form().createErrorPage(Response.Status.UNAUTHORIZED));
    }
}
