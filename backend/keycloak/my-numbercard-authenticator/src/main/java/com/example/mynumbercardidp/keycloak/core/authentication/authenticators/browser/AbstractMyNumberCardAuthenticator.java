package com.example.mynumbercardidp.keycloak.core.authentication.authenticators.browser;

import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Objects;
import javax.ws.rs.core.MultivaluedMap;

/**
 * 個人番号カードの公的個人認証部分を利用する認証処理で共通して実行する定義を表すクラスです。
 */
public abstract class AbstractMyNumberCardAuthenticator extends AbstractFormAuthenticator {

    @Override
    public void authenticate(final AuthenticationFlowContext context) {

    }

    @Override
    public void action(final AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(final KeycloakSession session, final RealmModel realm, final UserModel user) {
        // 一度も呼ばれない
        return true;
    }

    @Override
    public void setRequiredActions(final KeycloakSession session, final RealmModel realm, final UserModel user) {
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
    protected final MultivaluedMap<String, String> getFormData(final AuthenticationFlowContext context) {
        return context.getHttpRequest().getDecodedFormParameters();
    }

    /**
     * 認証試行ユーザーのセッション情報に認証フローの状態が存在することを保証します。
     *
     * 存在しない場合はIllegalStateExceptionを送出します。ActionHandlerが呼び出したActionクラスに不備があります。
     * @param context 認証フローのコンテキスト
     * @exception IllegalStateException Auth noteに認証フローの結果が存在しない場合
     */
    protected final void ensureHasAuthFlowStatus(final AuthenticationFlowContext context) {
        if (Objects.isNull(context.getStatus())) {
            throw new IllegalStateException("The Flow status in authentication flow context is not set.");
        }
    }

}
