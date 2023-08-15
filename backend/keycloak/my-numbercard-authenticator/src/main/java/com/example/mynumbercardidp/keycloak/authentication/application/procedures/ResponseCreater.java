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
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "サーバーでエラーが発生しました。";

    /** 認証SPIから認証フローの結果を取得できるようにするためのセッション備考名の接頭文字列 */
    private static final String AUTH_NOTE_KEY_PREFIX = "ResponseCreater";

    /** 認証SPIから認証フローの結果を取得できるようにするためのセッション備考名 */
    private static final String AUTH_NOTE_NAME_AUTH_FLOW_RESULT = "AuthFlowResult";

    /** 認証SPIから認証フローの結果を取得できるようにするためのセッション備考名のキー */
    public static final String AUTH_FLOW_RESULT = AUTH_NOTE_KEY_PREFIX + "." + AUTH_NOTE_NAME_AUTH_FLOW_RESULT;

    private ResponseCreater() {}

    /**
     * 内部エラーのレスポンスを返します。認証フローは継続できず、失敗したものとします。
     *
     * @param context 認証フローのコンテキスト
     */
    public static void createInternalServerErrorPage(AuthenticationFlowContext context) {
        createInternalServerErrorPage(context, null, null);
    }

    /**
     * 内部エラーのレスポンスを返します。認証フローは継続できず、失敗したものとします。
     *
     * @param context 認証フローのコンテキスト
     * @param error 表示するエラーメッセージ
     * @param filed フィールド名
     */
    public static void createInternalServerErrorPage(AuthenticationFlowContext context, String error, String filed) {
        context.form().setError(INTERNAL_SERVER_ERROR_MESSAGE, "");
        context.failure(AuthenticationFlowError.INTERNAL_ERROR,
                        context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
    }

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
     * 認証フローに認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返す。
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
        context.getAuthenticationSession().setAuthNote(AUTH_FLOW_RESULT, ExecutionStatus.CHALLENGED.toString());
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
        return createLoginForm(form);
    }

    private static Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsernamePassword();
    }

    /**
     * 認証フローの状態に認証が成功したことを設定し、認証フローを終了します。
     *
     * @param context 認証フローのコンテキスト
     * @param status HTTP ステータスコード
     */
    public static void setFlowStepSuccess(AuthenticationFlowContext context, Response response) {
       context.getAuthenticationSession().setAuthNote(AUTH_FLOW_RESULT, ExecutionStatus.SUCCESS.toString());
       context.challenge(response);
    }

    /**
     * 認証フローの状態に認証が必要であることを設定し、ユーザーへ認証画面のレスポンスを返します。
     *
     * @param context 認証フローのコンテキスト
     * @param status HTTP ステータスコード
     */
    public static void setFlowStepChallenge(AuthenticationFlowContext context, Response response) {
       context.getAuthenticationSession().setAuthNote(AUTH_FLOW_RESULT, ExecutionStatus.CHALLENGED.toString());
       context.challenge(response);
    }

    /**
     * 認証フローの状態に失敗したことを設定し、ユーザーへエラー画面のレスポンスを返します。
     *
     * このレスポンスは後続の認証フローのステップが実行されません。
     * @param context 認証フローのコンテキスト
     * @param status HTTP ステータスコード
     */
    public static void setFlowStepFailure(AuthenticationFlowContext context, Response response) {
       context.getAuthenticationSession().setAuthNote(AUTH_FLOW_RESULT, ExecutionStatus.FAILED.toString());
       context.challenge(response);
    }

    /**
     * 認証フローの状態に認証できなかったことを設定し、認証フローのステップを次へ進めます。
     *
     * このレスポンスはエラー状態でも、成功状態でもありません。
     * このステップの認証SPIは、認証を試行したが認証することができなかったことを表します。
     * 後続の認証フローで認証を行いたい場合はこの処理を呼び出してください。
     *
     * シナリオ例
     *   ユーザー名とパスワードの認証を試行したが、認証することができなかった。（＝Attempted）
     *   後続の認証ステップである、メールで送ったワンタイプパスフレーズで認証を試行する。
     * @param context 認証フローのコンテキスト
     */
    public static void setFlowStepAttempt(AuthenticationFlowContext context) {
        clearAuthNote(context);
        context.attempted();
    }

    /**
     * 認証フローのコンテキストにあるセッション備考から認証フローの状態を取得します。
     *
     * @param context 認証フローのコンテキスト
     * @return 認証フローの状態 設定されていない場合はNullを返します。
     */
    public static String getFlowState(AuthenticationFlowContext context) {
       return context.getAuthenticationSession().getAuthNote(AUTH_FLOW_RESULT);
    }

    /**
     * 認証フローのAuthNoteを初期化します。
     *
     * @param context 認証フローのコンテキスト
     */
    private static void clearAuthNote(AuthenticationFlowContext context) {
        context.getAuthenticationSession().clearAuthNotes();
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
