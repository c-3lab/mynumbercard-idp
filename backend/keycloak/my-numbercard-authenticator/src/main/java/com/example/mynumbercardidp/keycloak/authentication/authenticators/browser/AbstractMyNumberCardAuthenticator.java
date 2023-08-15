package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;

/**
 * 個人番号カードの公的個人認証部分を利用する認証処理で共通して実行する定義を表すクラスです。
 */
public abstract class AbstractMyNumberCardAuthenticator extends AbstractFormAuthenticator {

    public static final String REGISTRATION_FORM_ACTION = "registration_form"; // テンプレートファイルとの互換性維持
    public static final String ATTEMPTED_USERNAME = "ATTEMPTED_USERNAME"; // テンプレートファイルとの互換性維持

    @Override
    public void authenticate(AuthenticationFlowContext context) {

    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // 一度も呼ばれない
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // 一度も呼ばれない
    }

    @Override
    public void close() {

    }

    /**
     * HTTPフォームデータを取得する。
     *
     * @param context 認証フローのコンテキスト
     * @return デコード済みのHTTPフォームデータ配列
     */
    protected MultivaluedMap<String, String> getFormData(AuthenticationFlowContext context) {
        return context.getHttpRequest().getDecodedFormParameters();
    }
}
