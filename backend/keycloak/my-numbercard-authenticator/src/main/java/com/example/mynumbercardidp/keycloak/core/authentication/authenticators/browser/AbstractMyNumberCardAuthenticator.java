package com.example.mynumbercardidp.keycloak.core.authentication.authenticators.browser;

import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Objects;

/**
 * 個人番号カードの公的個人認証部分を利用する認証処理で共通して実行する定義を表すクラスです。
 */
public abstract class AbstractMyNumberCardAuthenticator extends AbstractFormAuthenticator {
    /**
     * 認証試行ユーザーのセッション情報に認証フローの状態が存在することを検証します。
     *
     * 存在しない場合はIllegalStateExceptionを送出します。
     *
     * @param context 認証フローのコンテキスト
     * @exception IllegalStateException Auth noteに認証フローの結果が存在しない場合
     */
    protected static void validateHasAuthFlowStatus(final AuthenticationFlowContext context) {
        if (Objects.isNull(context.getStatus())) {
            throw new IllegalStateException("The flow status in the authentication flow context was not set.");
        }
    }

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
}
