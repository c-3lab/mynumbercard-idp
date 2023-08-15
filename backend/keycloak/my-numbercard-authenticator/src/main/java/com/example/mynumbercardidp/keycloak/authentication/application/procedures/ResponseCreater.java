package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.CommonClientSessionModel.ExecutionStatus;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import java.util.Objects;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * ユーザーへのHTTPレスポンスを作成する処理の定義です。
 *
 * このクラスはユーティリティです。
 */
public class ResponseCreater {
    private ResponseCreater() {}

    /**
     * 認証フローに認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param actionMode 初期表示とする動作の種類
     * @return HTTP レスポンス
     */
    public static Response createChallengePage(AuthenticationFlowContext context, String actionMode) {
       return createChallengePage(context, actionMode, Response.Status.OK.getStatusCode());
    }

    /**
     * 認証フローに認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status HTTP ステータスコード
     * @return HTTP レスポンス
     */
    public static Response createChallengePage(AuthenticationFlowContext context, int status) {
       return createChallengePage(context, null, null, status);
    }

    public static Response createChallengePage(AuthenticationFlowContext context, String actionMode, int status) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        LoginFormsProvider form = context.form();

        // initialView（初期表示）をテンプレート変数に入れる
        context.getAuthenticationSession().setAuthNote("initialView", actionMode);
        form.setAttribute("initialView", actionMode);
        return createChallengePage(context, null, null, status);
    }

    public static Response createChallengePage(AuthenticationFlowContext context, String error, String field, int status) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        form.setAttribute("refreshUrl", context.getRefreshUrl(true).toString());

        if (isStringNonEmpty(error)) {
            if (isStringNonEmpty(field)) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        Response templateResponse = createLoginForm(form);
        return Response.fromResponse(templateResponse)
            .status(status)
            .build();
    }

    private static Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsernamePassword();
    }

    /**
     * 認証フローの状態に認証が成功したことを設定し、認証フローを終了します。
     *
     * @param context 認証フローのコンテキスト
     */
    public static void setFlowStepSuccess(AuthenticationFlowContext context) {
       context.success();
    }

    /**
     * 認証フローの状態に認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status HTTP ステータスコード
     */
    public static void setFlowStepChallenge(AuthenticationFlowContext context, Response response) {
       context.challenge(response);
    }

    /**
     * 認証フローの状態に失敗したことを設定し、ユーザーへエラー画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param error 認証フローエラーの種類
     * @param status HTTP ステータスコード
     */
    public static void setFlowStepFailure(AuthenticationFlowContext context, AuthenticationFlowError error, Response response) {
       context.failure(error, response);
    }

    /**
     * String型がNullまたは文字列の長さがゼロでは無いことを判定します。
     *
     * @param str Nullまたは長さがゼロでは無いことを判定したい文字列
     * @return Nullまたは長さがゼロでは無い場合はtrue、そうでない場合はfalse
     */
    protected static boolean isStringNonEmpty(String str) {
        return java.util.Objects.nonNull(str) && str.length() > 0;
    }
}
