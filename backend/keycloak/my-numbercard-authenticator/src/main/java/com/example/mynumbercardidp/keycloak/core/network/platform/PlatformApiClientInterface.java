package com.example.mynumbercardidp.keycloak.core.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;

import javax.ws.rs.core.MultivaluedMap;

/**
 * このインターフェイスは個人番号カードの公的個人認証部分を利用する認証をしたいユーザー向けです。
 */
public interface PlatformApiClientInterface {

    /**
     * プラットフォームのAPI基準URLとプラットフォームへ送信するパラメータとIdp送信者識別符号で初期化します。
     *
     * @param apiRootUri プラットフォームAPI URLでユーザーが希望する処理に関わらず共通の部分
     *                   主にプロトコルとホスト名、ポート番号までを指します。
     * @param formData   Keycloakが受け取ったHTTPリクエスト内に含まれているFormパラメータをデコードした連想配列
     * @param idpSender  プラットフォームへ送るIdp送信者の識別符号
     */
    void init(String apiRootUri, MultivaluedMap<String, String> formData, String idpSender);

    /**
     * プラットフォームへリクエストを送信します。
     *
     * このメソッドを実行する前に{@link #init(MultivaluedMap<String, String>)}を実行しておく必要があります。
     */
    void sendRequest();

    /**
     * ユーザーリクエストの構造体を返します。
     *
     * @return ユーザーリクエストのデータ構造体インスタンス
     */
    AuthenticationRequest getUserRequest();

    /**
     * プラットフォームリクエストの構造体を返します。
     *
     * @return プラットフォームリクエストのデータ構造体インスタンス
     */
    Object getPlatformRequest();

    /**
     * プラットフォームレスポンスの構造体を返します。
     *
     * @return プラットフォームレスポンスのデータ構造体インスタンス
     */
    PlatformAuthenticationResponseStructure getPlatformResponse();
}
