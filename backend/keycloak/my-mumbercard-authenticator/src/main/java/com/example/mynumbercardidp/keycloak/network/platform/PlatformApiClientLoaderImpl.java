package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.network.RequestBuilder;
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
     * @exception ClassNotFoundException 指定されたクラスが存在しない場合
     * @exception ClassCastException プラットフォームAPIクラスと継承関係にないクラスにキャストしようとした場合
     * @exception NoSuchMethodException プラットフォームAPIクラスに認証フローのコンテキストを受け入れるコンストラクタが存在しない場合
     * @exception URISyntaxException プラットフォーム API URI 文字列をURI参照として解析できなかった場合
     * @exception InstantiationException リクエストデータ、レスポンスデータ構造のインスタンスを作成できなかった場合
     * @exception InstantiationException 配列以外のインスタンス作成、フィールドの設定または取得、メソッドの呼出しを試みた場合
     * @exception InvocationTargetException 呼び出されるメソッドまたはコンストラクタがスローする例外をラップする、チェック例外
     */
    default PlatformApiClient load(String platformClassFqdn, AuthenticationFlowContext context, String apiRootUri) throws ClassNotFoundException, ClassCastException, NoSuchMethodException, URISyntaxException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return load(platformClassFqdn, context, apiRootUri, null);
    }

    /**
     * クラスのFQDNからプラットフォームと通信するためのクラスインスタンスを返します。
     *
     * プラットフォームAPIクライアントを初期化するときにIdp送信者符号も渡します。
     *
     * @param platformClassFqdn プラットフォームAPIクライアントの完全修飾クラス名
     * @param context 認証フローのコンテキスト
     * @param apiRootUri プラットフォームAPIのルートURI（処理による変更が無い共通URI部分）
     * @param idpSender プラットフォームへ送るIdp送信者の識別符号
     * @return プラットフォームAPIクライアントインスタンス
     * @exception ClassNotFoundException 指定されたクラスが存在しない場合
     * @exception ClassCastException プラットフォームAPIクラスと継承関係にないクラスにキャストしようとした場合
     * @exception NoSuchMethodException プラットフォームAPIクラスに認証フローのコンテキストを受け入れるコンストラクタが存在しない場合
     * @exception URISyntaxException プラットフォーム API URI 文字列をURI参照として解析できなかった場合
     * @exception InstantiationException リクエストデータ、レスポンスデータ構造のインスタンスを作成できなかった場合
     * @exception InstantiationException 配列以外のインスタンス作成、フィールドの設定または取得、メソッドの呼出しを試みた場合
     * @exception InvocationTargetException 呼び出されるメソッドまたはコンストラクタがスローする例外をラップする、チェック例外
     */
    PlatformApiClient load(String platformClassFqdn, AuthenticationFlowContext context, String apiRootUri, String idpSender) throws ClassNotFoundException, ClassCastException, NoSuchMethodException, URISyntaxException, InstantiationException, IllegalAccessException, InvocationTargetException;
}
