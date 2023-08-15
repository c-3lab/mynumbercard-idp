package com.example.mynumbercardidp.keycloak.network.platform;

import org.keycloak.authentication.AuthenticationFlowContext;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Keycloakを利用してログインするユーザーが送信したHTTPリクエスト内容を元にデータ構造体を作成するクラスのインタフェースです。
 */
public interface RequestBuilderImpl {

    /**
     * 認証フローのコンテキストからHTMLフォームデータを抽出します。
     *
     * @param context 認証フローのコンテキスト
     * @return ユーザーが送ったHTMLフォームデータのマップ
     */
    static MultivaluedMap<String, String> extractFormData(AuthenticationFlowContext context) {
         return context.getHttpRequest().getDecodedFormParameters();
    }

    /**
     * ユーザーリクエスト構造のインスタンスを返します。
     *
     * @return ユーザーリクエスト構造のインスタンス
     */
    CommonRequestModelImpl getUserRequest();

    /**
     * プラットフォームリクエスト構造のインスタンスを返します。
     *
     * @return プラットフォーム構造のインスタンス
     */
    CommonRequestModelImpl getPlatformRequest();

    /**
     * ユーザーリクエスト構造のインスタンスを設定します。
     *
     * @param request ユーザーリクエスト構造のインスタンス
     */
    void setUserRequest(CommonRequestModelImpl request);

    /**
     * プラットフォームリクエスト構造のインスタンスを設定します。
     *
     * @param request プラットフォーム構造のインスタンス
     */
    void setPlatformRequest(CommonRequestModelImpl request);

    /**
     * ユーザーのリクエスト構造体からプラットフォームへのリクエスト構造体に変換します。
     * 
     * @param platformRequestModel プラットフォームへ送るリクエスト構造体のクラス
     * @param sender プラットフォームが識別するIDプロバイダーの送信者符号
     * @return ユーザーリクエスト構造のインスタンス
     */
    CommonRequestModelImpl toPlatformRequest(Class<? extends CommonRequestModelImpl> platformRequestModel, String sender);
}
