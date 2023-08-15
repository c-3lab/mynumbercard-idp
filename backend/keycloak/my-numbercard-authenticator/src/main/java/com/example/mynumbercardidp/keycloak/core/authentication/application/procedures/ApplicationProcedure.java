package com.example.mynumbercardidp.keycloak.core.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import org.keycloak.authentication.AuthenticationFlowContext;

/**
 * このインターフェイスは認証フローの中で認証、登録、登録情報の変更など、処理を切り替えたいユーザー向けです。
 */
public interface ApplicationProcedure {

    /**
     * クラス名で表した処理を実行します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォームのインスタンス
     */
    void execute(AuthenticationFlowContext context, PlatformApiClientImpl platform); 

    /**
     * クラス名で表した処理の事前処理を実行します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    void preExecute(AuthenticationFlowContext context, PlatformApiClientImpl platform);
}
