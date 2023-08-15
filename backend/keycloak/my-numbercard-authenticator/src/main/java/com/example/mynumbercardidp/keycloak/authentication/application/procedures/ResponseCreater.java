package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.CommonClientSessionModel.ExecutionStatus;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * ユーザーへのHTTPレスポンスを作成する処理の定義です。
 *
 * このクラスはユーティリティです。
 */
public final class ResponseCreater {
    private ResponseCreater() {}

    /**
     * 認証フローに認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param actionMode 初期表示とする動作の種類
     * @return HTTP レスポンス
     */
    public static final Response createChallengePage(final AuthenticationFlowContext context, final String actionMode) {
       return ResponseCreater.createChallengePage(context, actionMode, Response.Status.OK.getStatusCode());
    }

    /**
     * 認証フローに認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status HTTP ステータスコード
     * @return HTTP レスポンス
     */
    public static final Response createChallengePage(final AuthenticationFlowContext context, final int status) {
       return ResponseCreater.createChallengePage(context, null, null, status);
    }

    public static final Response createChallengePage(final AuthenticationFlowContext context, String actionMode, final int status) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        LoginFormsProvider form = context.form();

        String authNoteInitialViewAttributeName = "initialView";
        context.getAuthenticationSession().setAuthNote(authNoteInitialViewAttributeName, actionMode);
        form.setAttribute(authNoteInitialViewAttributeName, actionMode);
        return ResponseCreater.createChallengePage(context, null, null, status);
    }

    public static final Response createChallengePage(final AuthenticationFlowContext context, final String error, final String field, final int status) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        String formRefreshUrlAttributeName = "refreshUrl";
        form.setAttribute(formRefreshUrlAttributeName, context.getRefreshUrl(true).toString());

        if (StringUtil.isNonEmpty(error)) {
            if (StringUtil.isNonEmpty(field)) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        Response templateResponse = ResponseCreater.createLoginForm(form);
        return Response.fromResponse(templateResponse)
            .status(status)
            .build();
    }

    /**
     * 認証フローの状態に認証が成功したことを設定し、認証フローを終了します。
     *
     * @param context 認証フローのコンテキスト
     */
    public static final void setFlowStepSuccess(final AuthenticationFlowContext context) {
       context.success();
    }

    /**
     * 認証フローの状態に認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status HTTP ステータスコード
     */
    public static final void setFlowStepChallenge(final AuthenticationFlowContext context, final Response response) {
       context.challenge(response);
    }

    /**
     * 認証フローの状態に失敗したことを設定し、ユーザーへエラー画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param error 認証フローエラーの種類
     * @param status HTTP ステータスコード
     */
    public static final void setFlowStepFailure(final AuthenticationFlowContext context, final AuthenticationFlowError error, final Response response) {
       context.failure(error, response);
    }

    /**
     *  登録画面を初期表示とした画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     */
    public static final void actionRegistrationChallenge(final AuthenticationFlowContext context) {
        String registrationActionMode = "registration";
        ResponseCreater.actionReChallenge(context, registrationActionMode, Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     *  認証画面を初期表示とした画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     */
    public static final void actionLoginChallenge(final AuthenticationFlowContext context) {
        String loginActionMode = "login";
        ResponseCreater.actionReChallenge(context, loginActionMode, Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     *  指定された処理の画面を初期表示とした画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param actionName 遷移先処理の種類
     * @param status プラットフォームのHTTPステータスコード
     */
    public static final void actionReChallenge(final AuthenticationFlowContext context, final String actionName, final int status) {
        Response response = ResponseCreater.createChallengePage(context, actionName, status);
        ResponseCreater.setFlowStepChallenge(context, response);
    }

    /**
     * 公的電子証明書を検証できなかった画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param actionName 遷移先処理の種類
     * @param status プラットフォームのHTTPステータスコード
     */
    public static final void actionUnauthorized(final AuthenticationFlowContext context) {
        context.form().setError(Messages.INVALID_REQUEST, "");
        context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
                        context.form().createErrorPage(Response.Status.UNAUTHORIZED));
    }

    /**
     * 共通で使うテンプレート変数をユーザーに表示する画面のテンプレートへ設定します。
     *
     * @param context 認証フローのコンテキスト
     */
    public static final void setLoginFormAttributes(final AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        LoginFormsProvider form = context.form();

        String nonce = ResponseCreater.createNonce();
        context.getAuthenticationSession().setAuthNote("nonce", nonce);
        form.setAttribute("nonce", nonce);

        Map<String, String> spiConfig = SpiConfigProperty.getFreeMarkerJavaTemplateVariables();
        spiConfig.forEach((k, v) -> form.setAttribute(k, v));
    }

    /**
     * Nonceを生成します。
     *
     * @return nonce UUIDの文字列
     */
    protected static final String createNonce() {
        return UUID.randomUUID().toString();
    }

    private static final Response createLoginForm(final LoginFormsProvider form) {
        return form.createLoginUsernamePassword();
    }
}
