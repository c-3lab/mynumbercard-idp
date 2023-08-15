package com.example.mynumbercardidp.keycloak.core.network.platform;

import org.keycloak.authentication.AuthenticationFlowContext;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * 個人番号カードの公的個人認証部分を受け付けるプラットフォームと通信するクラスローダーの定義です。
 */
interface PlatformApiClientLoaderImpl {
    /**
     * クラスのFQDNからプラットフォームと通信するためのクラスインスタンスを返します。
     *
     * @param platformClassFqdn プラットフォームAPIクライアントの完全修飾クラス名
     * @param context 認証フローのコンテキスト
     * @param apiRootUri プラットフォームAPIのルートURI（処理による変更が無い共通URI部分）
     * @return プラットフォームAPIクライアントインスタンス
     */
    default PlatformApiClientImpl load(String platformClassFqdn, AuthenticationFlowContext context, String apiRootUri) {
        return load(platformClassFqdn, context, apiRootUri, null);
    }

    /**
     * クラスのFQDNからプラットフォームと通信するためのクラスインスタンスを返します。
     *
     * プラットフォームAPIクライアントを初期化するときにIdP送信者符号も渡します。
     *
     * @param platformClassFqdn プラットフォームAPIクライアントの完全修飾クラス名
     * @param context 認証フローのコンテキスト
     * @param apiRootUri プラットフォームAPIのルートURI（処理による変更が無い共通URI部分）
     * @param idpSender プラットフォームへ送るIdP送信者の識別符号
     * @return プラットフォームAPIクライアントインスタンス
     */
    PlatformApiClientImpl load(String platformClassFqdn, AuthenticationFlowContext context, String apiRootUri, String idpSender);
}
