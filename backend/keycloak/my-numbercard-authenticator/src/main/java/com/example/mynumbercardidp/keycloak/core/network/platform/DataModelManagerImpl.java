package com.example.mynumbercardidp.keycloak.core.network.platform;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.nio.charset.Charset;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Keycloakへ送信されたユーザーのリクエストデータやプラットフォームへ送信するリクエストデータ、
 * プラットフォームから送信されたレスポンスデータを管理するクラスのインタフェースです。
 */
public interface DataModelManagerImpl {
    /**
     * ユーザーが送信したHTMLフォームパラメータを設定します。
     *
     * @param formData ユーザーが送信したHTMLフォームパラメータの配列
     */
    void setUserFormData(MultivaluedMap<String, String> formData);

    /**
     * プラットフォームへリクエストを送る場合の文字セットを設定します。
     *
     * @param formData ユーザーが送信したHTMLフォームパラメータの配列
     */
    void setRequestCharset(Charset charset);

    /**
     * プラットフォームへ送信するIdP送信者符号を設定します。
     *
     * 空文字列は許容しますが、nullは許容しません。
     * @param requestSender プラットフォームへ送信するIdP送信者符号
     * @exception NullPointerException - nullである場合
     */
    void setPlatformRequestSender(String requestSender);

    /**
     * ユーザーリクエストのデータ構造を返します。
     *
     * @return ユーザーリクエストのデータ
     */
    UserRequestModelImpl getUserRequest();

    /**
     * プラットフォームリクエストのデータ構造を返します。
     *
     * @return プラットフォームリクエストのデータ
     */
    Object getPlatformRequest();

    /**
     * プラットフォームレスポンスのデータ構造を返します。
     *
     * @return プラットフォームレスポンスのデータ
     */
    Object getPlatformResponse();

    /**
     * HTTPレスポンスからプラットフォームレスポンスのデータ構造を返します。
     *
     * 2回目以降は引数なしで呼び出すことができます。
     * @param httpResponse プラットフォームのHTTPレスポンス
     * @return プラットフォームレスポンスのデータ
     * @exception NullPointerException プラットフォームのHTTPレスポンスがnullである場合
     */
    Object getPlatformResponse(CloseableHttpResponse httpResponse);
}
