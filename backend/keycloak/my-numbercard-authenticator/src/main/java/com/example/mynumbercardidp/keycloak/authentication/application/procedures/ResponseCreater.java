package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.Response;

/**
 * ユーザーへのHTTPレスポンスを作成する処理の定義です。
 *
 * このクラスはユーティリティです。
 */
public final class ResponseCreater {
    private ResponseCreater() {
    }

    /**
     * 認証フローに認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context    認証フローのコンテキスト
     * @param actionName 初期表示とする動作の種類
     * @return HTTP レスポンス
     */
    public static final Response createChallengePage(final AuthenticationFlowContext context, final String actionName) {
        return ResponseCreater.createChallengePage(context, actionName, Response.Status.OK);
    }

    /**
     * 認証フローに認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status  HTTP ステータスコード
     * @return HTTP レスポンス
     */
    public static final Response createChallengePage(final AuthenticationFlowContext context,
            final Response.Status status) {
        return ResponseCreater.createChallengePage(context, null, null, status);
    }

    public static final Response createChallengePage(final AuthenticationFlowContext context, String actionName,
            final Response.Status status) {
        LoginFormsProvider form = context.form();

        Optional<String> actionNameValue = Optional.ofNullable(actionName);
        context.getAuthenticationSession().setAuthNote("initialView", actionNameValue.orElse(""));
        form.setAttribute("initialView", actionNameValue.orElse(""));
        return ResponseCreater.createChallengePage(context, null, null, status);
    }

    public static final Response createChallengePage(final AuthenticationFlowContext context, final String error,
            final String field, final Response.Status status) {
        URI actionURLValue = context.getActionUrl(context.generateAccessCode());
        LoginFormsProvider form = context.form()
                .setActionUri(actionURLValue)
                .setExecution(context.getExecution().getId())
                .setResponseHeader("X-Action-URL", actionURLValue.toString());

        form.setAttribute("refreshUrl", context.getRefreshUrl(true).toString());
        if (StringUtil.isNonEmpty(error)) {
            if (StringUtil.isNonEmpty(field)) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        Response templateResponse = form.createLoginUsernamePassword();
        return Response.fromResponse(templateResponse)
                .status(status)
                .build();
    }

    /**
     * 指定された処理の画面を初期表示とした画面のレスポンスを返します。
     *
     * @param context    認証フローのコンテキスト
     * @param actionName 遷移先処理の種類
     * @param status     プラットフォームのHTTPステータスコード
     */
    public static final void sendChallengeResponse(final AuthenticationFlowContext context, final String actionName,
            final Response.Status status) {
        ResponseCreater.setLoginFormAttributes(context);
        Response response = ResponseCreater.createChallengePage(context, actionName, status);
        context.challenge(response);
    }

    /**
     * 無効な要求であったことを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status  プラットフォームのHTTPステータスコード
     */
    public static final void sendInvalidRequestResponse(final AuthenticationFlowContext context,
            final Response.Status status) {
        context.form().setError(Messages.INVALID_REQUEST, "");
        context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, context.form().createErrorPage(status));
    }

    /**
     * 共通で使うテンプレート変数をユーザーに表示する画面のテンプレートへ設定します。
     *
     * @param context 認証フローのコンテキスト
     */
    public static final void setLoginFormAttributes(final AuthenticationFlowContext context) {
        boolean reuseNonceFlag = Boolean.valueOf(context.getAuthenticationSession().getAuthNote("reuseNonceFlag"));
        String nonce = ResponseCreater.createNonceOrReuse(reuseNonceFlag, context.getAuthenticationSession());
        LoginFormsProvider form = context.form();
        form.setAttribute("nonce", nonce);
        SpiConfigProperty.getFreeMarkerJavaTemplateVariables().forEach((k, v) -> form.setAttribute(k, v));
    }

    /**
     * Nonceを返します。
     *
     * 認証フローのAuth noteにあるNonceの再利用フラグがtrueの場合、直前に発行したNonceの文字列を返します。
     * そうでない場合はUUID文字列を返します。
     *
     * @param reuseNonceFlag Nonceの再利用フラグ
     * @param session        認証フローのセッション情報
     * @return UUID文字列
     */
    private static String createNonceOrReuse(boolean reuseNonceFlag, final AuthenticationSessionModel session) {
        if (reuseNonceFlag) {
            return session.getAuthNote("nonce");
        } else {
            String nonce = UUID.randomUUID().toString();
            session.setAuthNote("nonce", nonce);
            return nonce;
        }
    }
}
