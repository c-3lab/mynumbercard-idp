package com.example.mynumbercardidp.keycloak.network.platform;

import org.keycloak.models.UserModel;

import java.net.http.HttpTimeoutException;
import java.net.URISyntaxException;
import javax.ws.rs.core.MultivaluedMap;

/**
 * このインターフェイスは個人番号カードの公的個人認証部分を利用する認証をしたいユーザー向けです。
 */
public interface PlatformApiClientImpl {

    /**
     * プラットフォームのAPI基準URLとプラットフォームへ送信するパラメータとIdp送信者識別符号で初期化します。
     * @param apiRootUri プラットフォームAPI URLでユーザーが希望する処理に関わらず共通の部分
     *                   主にプロトコルとホスト名、ポート番号までを指す。
     * @param formData Keycloakが受け取ったHTTPリクエスト内に含まれているFormパラメータをデコードした連想配列
     * @param idpSender プラットフォームへ送るIdp送信者の識別符号
     */
    void init(String apiRootUri, MultivaluedMap<String, String> formData, String idpSender);

    /**
     * プラットフォームと通信し、メソッド呼び出し元へ結果を返します。
     *
     * このメソッドを実行する前に{@link #initPost(MultivaluedMap<String, String>)}を実行しておく必要があります。
     * @return プラットフォームからの応答内容
     */
    void action();

    /**
     * ユーザーが希望する処理の種類を返します。
     *
     * @return ユーザーが希望する処理の種類
     */
    String getUserActionMode();

    /**
     * ユーザーリクエストの構造体を返します。
     *
     * @return ユーザーリクエストのデータ構造体インスタンス
     */
    UserRequestModel getUserRequest();

    /**
     * プラットフォームリクエストの構造体を返します。
     *
     * @return プラットフォームリクエストのデータ構造体インスタンス
     */
    PlatformRequestModel getPlatformRequest();

    /**
     * プラットフォームレスポンスの構造体を返します。
     *
     * @return プラットフォームレスポンスのデータ構造体インスタンス
     */
    PlatformResponseModel getPlatformResponse();

    /**
     * プラットフォームレスポンスにユーザーの一意のIDが存在することを保証します。
     *
     * 存在しない場合は IllegalStateException を送出します。
     *
     * @exception IllegalStateException プラットフォームレスポンスにユーザーの一意のIDが存在しない場合
     */
    void ensureHasUniqueId();

    /**
     * プラットフォームのレスポンスからKeycloakのユーザー属性を追加、更新します。
     *
     * @param user Keycloak ユーザーのデータ構造体インスタンス
     */
    UserModel addUserModelAttributes(UserModel user);
}
