package com.example.mynumbercardidp.keycloak.core.authentication.application.procedures;

import org.keycloak.authentication.AuthenticationFlowContext;

/**
 * 認証フローの中で認証、登録、登録情報の変更など、ユーザーの要求に応じて処理を呼び出すインタフェースです。
 */
public interface ApplicationResolverInterface {
    /**
     * ユーザーの要求に応じて処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     */
    void executeUserAction(AuthenticationFlowContext context);
}
