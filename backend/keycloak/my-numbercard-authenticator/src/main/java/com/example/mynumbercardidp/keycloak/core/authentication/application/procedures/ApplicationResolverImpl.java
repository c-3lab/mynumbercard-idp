package com.example.mynumbercardidp.keycloak.core.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import org.keycloak.authentication.AuthenticationFlowContext;

/**
 * 認証フローの中で認証、登録、登録情報の変更など、ユーザーの要求に応じて処理を呼び出すインタフェースです。
 */
public interface ApplicationResolverImpl {
    /**
     * ユーザーの要求に応じて処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     */
    void action(AuthenticationFlowContext context);
}
