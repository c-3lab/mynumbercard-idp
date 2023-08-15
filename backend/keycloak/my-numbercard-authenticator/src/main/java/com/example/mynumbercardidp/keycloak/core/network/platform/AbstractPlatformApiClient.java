package com.example.mynumbercardidp.keycloak.core.network.platform;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.keycloak.connections.httpclient.HttpClientBuilder;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.ws.rs.core.MultivaluedMap;

/**
 * 個人番号カードの公的個人認証部分を受け付けるプラットフォームと通信する処理を定義すべき実装を表しています。
 *
 * このクラスを継承したサブクラスは、インスタンス生成元で{@link #init(MultivaluedMap<String, String>)}を実行し、プラットフォームへ送信するパラメータを受け取る必要があります。
 */
public abstract class AbstractPlatformApiClient implements PlatformApiClientImpl {

    /* プラットフォームと通信するときのリクエスト設定 */
    protected long establishConnectionTimeout = 10000L;
    protected TimeUnit establishConnectionTimeoutUnit = TimeUnit.MILLISECONDS;
    protected long maxConnectionIdleTime = 30000L;
    protected TimeUnit maxConnectionIdleTimeUnit = TimeUnit.MILLISECONDS;
    protected long socketTimeout = 30000L;
    protected TimeUnit socketTimeoutUnit = TimeUnit.MILLISECONDS;
    /** プラットフォームに送信するHTTP Body構造 */
    protected HttpEntity httpEntity;
    /** プラットフォームに送信するコンテンツタイプ */
    protected ContentType httpRequestContentType = ContentType.TEXT_PLAIN;
    /** プラットフォームのAPIルートURI */
    protected URI apiRootUri;
    /** プラットフォームに送信するHTTP Bodyの文字セット */
    protected Charset defaultCharset = Charset.forName("UTF-8");
    /** ユーザーリクエストのデータ構造 */
    protected UserRequestModelImpl userRequest;
    /** プラットフォームリクエストのデータ構造 */
    protected Object platformRequest;
    /** プラットフォームレスポンスのデータ構造 */
    protected Object platformResponse;
    /** プラットフォームへ送るIDP識別送信者符号 */
    protected String platformRequestSender = "";

    @Override
    public UserRequestModelImpl getUserRequest() {
        return userRequest;
    }

    @Override
    public Object getPlatformRequest() {
        return platformRequest;
    }

    @Nullable
    @Override
    public Object getPlatformResponse() {
        return platformResponse;
    }

    /**
     * プラットフォームのAPIへデータを送信します。
     *
     * @param apiUri プラットフォームのAPI URI
     * @param headers HTTP リクエストのヘッダー
     * @param entity HTTP リクエストのボディ
     * @return プラットフォームのレスポンス
     */
    protected void post(URI apiUri, Header[] headers, HttpEntity entity) {
        HttpPost httpPost = new HttpPost(apiUri);
        if (Objects.nonNull(headers) && headers.length > 0) {
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, httpRequestContentType.toString());
            httpPost.setHeaders(headers);
        } else if (!Arrays.asList(headers).contains(HttpHeaders.CONTENT_TYPE)) {
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, httpRequestContentType.toString());
        }
        httpPost.setEntity(entity);

        try (CloseableHttpClient httpClient = new HttpClientBuilder().disableTrustManager()
            .establishConnectionTimeout(establishConnectionTimeout, establishConnectionTimeoutUnit)
            .maxConnectionIdleTime(maxConnectionIdleTime, maxConnectionIdleTimeUnit)
            .socketTimeout(socketTimeout, socketTimeoutUnit)
            .build()) {
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                platformResponse = toPlatformResponse(httpResponse);
            } catch (HttpTimeoutException e) {
                String message = "Connect timeout. Platform URL: " + apiUri.toString();
                throw new IllegalArgumentException(message, e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
       } catch (IOException e) {
           throw new UncheckedIOException(e);
       }
    }

    /**
     * 文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(String s) {
        return createHttpEntity(s.getBytes(defaultCharset), httpRequestContentType);
    }

    /**
     * 文字セットを指定し、文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @param charset HTTPリクエストボディの文字セット
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(String s, String charset) {
        try {
            return createHttpEntity(s.getBytes(charset), httpRequestContentType);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * コンテンツタイプと文字セットを指定し、文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @param charset HTTPリクエストボディの文字セット
     * @param contentType HTTPリクエストボディのコンテンツタイプ
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(String s, String charset, ContentType contentType) {
        try {
            return createHttpEntity(s.getBytes(charset), contentType);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 文字セットを指定し、文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @param charset HTTPリクエストボディの文字セット
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(String s, Charset charset) {
        return createHttpEntity(s.getBytes(charset), httpRequestContentType);
    }

    /**
     * コンテンツタイプと文字セットを指定し、文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @param charset HTTPリクエストボディの文字セット
     * @param contentType HTTPリクエストボディのコンテンツタイプ
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(String s, Charset charset, ContentType contentType) {
        return createHttpEntity(s.getBytes(charset), contentType);
    }

    /**
     * コンテンツタイプを指定し、文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @param contentType HTTPリクエストボディのコンテンツタイプ
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(String s, ContentType contentType) {
        return createHttpEntity(s.getBytes(defaultCharset), contentType);
    }

    /**
     * バイト配列からHTTPリクエストのボディを作成します。
     *
     * @param b HTTPリクエストボディのバイト配列
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(byte[] b) {
        return createHttpEntity(b, httpRequestContentType);
    }

    /**
     * コンテンツタイプを指定し、バイト配列からHTTPリクエストのボディを作成します。
     *
     * @param b HTTPリクエストボディのバイト配列
     * @param contentType HTTPリクエストボディのコンテンツタイプ
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(byte[] b, ContentType contentType) {
        return new ByteArrayEntity(b,  contentType);
    }

    /**
     * プラットフォーム APIのレスポンスデータ構造を表すインスタンスへ変換します。
     *
     * @param httpResponse プラットフォームのHTTPレスポンス
     * @return プラットフォームレスポンスのデータ構造インスタンス
     */
    protected abstract Object toPlatformResponse(CloseableHttpResponse httpResponse);

    /**
     * HTMLフォームデータをユーザーリクエスト構造へ変換します。
     *
     * @param formData HTMLフォームデータ
     * @return ユーザーリクエストの構造
     */
    protected abstract UserRequestModelImpl toUserRequest(MultivaluedMap<String, String> formData);

    /**
     * ユーザーリクエスト構造をプラットフォームリクエスト構造へ変換します。
     *
     * @param user ユーザーリクエスト構造のインスタンス
     * @return プラットフォームリクエストの構造
     */
    protected abstract Object toPlatformRequest(UserRequestModelImpl user);

    @Override
    public void init(String apiRootUri, MultivaluedMap<String, String> formData, String idpSender){
        try {
            this.apiRootUri = new URI(apiRootUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        userRequest = toUserRequest(formData);
        platformRequestSender = Objects.isNull(idpSender) ? "" : idpSender;
    }

    @Override
    public abstract void action();

    @Override
    public String getUserActionMode() {
        return userRequest.getActionMode();
    }

    @Override
    public abstract void ensureHasUniqueId();

    @Override
    public abstract UserModel addUserModelAttributes(UserModel user);
}
