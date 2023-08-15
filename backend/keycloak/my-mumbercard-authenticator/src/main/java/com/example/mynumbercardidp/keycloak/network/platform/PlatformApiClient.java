package com.example.mynumbercardidp.keycloak.network.platform;

import java.net.http.HttpTimeoutException;
import java.net.URISyntaxException;
import javax.ws.rs.core.MultivaluedMap;

/**
 * このインターフェイスは個人番号カードの公的個人認証部分を利用する認証をしたいユーザー向けです。
 */
public interface PlatformApiClient {

    /**
     * プラットフォームへ送信するパラメータで初期化します。
     * @param formData Keycloakが受け取ったHTTPリクエスト内に含まれているFormパラメータをデコードした連想配列
     */
    default void init(MultivaluedMap<String, String> formData) {
        init(formData, null);
    }

    /**
     * プラットフォームへ送信するパラメータとIdp送信者識別符号で初期化します。
     * @param formData Keycloakが受け取ったHTTPリクエスト内に含まれているFormパラメータをデコードした連想配列
     * @param idpSender プラットフォームへ送るIdp送信者の識別符号
     */
    void init(MultivaluedMap<String, String> formData, String idpSender);

    /**
     * プラットフォームと通信し、メソッド呼び出し元へ結果を返します。
     *
     * このメソッドを実行する前に{@link #initPost(MultivaluedMap<String, String>)}を実行しておく必要があります。
     * @return プラットフォームからの応答内容
     */
    CommonResponseModel action();

    /**
     * ユーザーリクエストの構造体を返します。
     *
     * @return ユーザーリクエストのデータ構造体インスタンス
     */
    CommonRequestModelImpl getUserRequest();

    /**
     * プラットフォームリクエストの構造体を返します。
     *
     * @return プラットフォームリクエストのデータ構造体インスタンス
     */
    CommonRequestModelImpl getPlatformRequest();

    /**
     * プラットフォームレスポンスの構造体を返します。
     *
     * @return プラットフォームレスポンスのデータ構造体インスタンス
     */
    CommonResponseModel getPlatformResponse();
}
