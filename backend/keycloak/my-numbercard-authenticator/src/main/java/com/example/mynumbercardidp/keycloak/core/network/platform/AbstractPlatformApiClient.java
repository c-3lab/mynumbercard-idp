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
import org.jboss.logging.Logger;

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
    private long establishConnectionTimeout = 10000L;
    private TimeUnit establishConnectionTimeoutUnit = TimeUnit.MILLISECONDS;
    private long maxConnectionIdleTime = 30000L;
    private TimeUnit maxConnectionIdleTimeUnit = TimeUnit.MILLISECONDS;
    private long socketTimeout = 30000L;
    private TimeUnit socketTimeoutUnit = TimeUnit.MILLISECONDS;
    /** プラットフォームに送信するHTTP Body構造 */
    private HttpEntity httpEntity;
    /** プラットフォームに送信するコンテンツタイプ */
    private ContentType httpRequestContentType = ContentType.TEXT_PLAIN;
    /** プラットフォームのAPIルートURI */
    private URI apiRootUri;
    /** プラットフォームに送信するHTTP Bodyの文字セット */
    private Charset defaultCharset = Charset.forName("UTF-8");
    /** ユーザーリクエスト、プラットフォームリクエスト、レスポンス管理クラス */
    private DataModelManagerImpl dataManager;
    /** プラットフォームへ送るIDP識別送信者符号 */
    private String platformRequestSender = "";

    @Override
    public UserRequestModelImpl getUserRequest() {
        return dataManager.getUserRequest();
    }

    @Override
    public Object getPlatformRequest() {
        return dataManager.getPlatformRequest();
    }

    @Override
    public Object getPlatformResponse() {
        return dataManager.getPlatformResponse();
    }

    @Override
    public String getUserActionMode() {
        return dataManager.getUserRequest().getActionMode();
    }

    @Override
    public void init(final String apiRootUri, final MultivaluedMap<String, String> formData, final String idpSender){
        try {
            this.apiRootUri = new URI(apiRootUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        dataManager = createDataManager(formData);
        platformRequestSender = Objects.isNull(idpSender) ? "" : idpSender;
        dataManager.setPlatformRequestSender(platformRequestSender);
        dataManager.setRequestCharset(defaultCharset);
    }

    @Override
    public abstract void action();

    protected long getEstablishConnectionTimeout() {
        return establishConnectionTimeout;
    }

    protected void setEstablishConnectionTimeout(final long timeout) {
        establishConnectionTimeout = timeout;
    }

    protected long getMaxConnectionIdleTime() {
        return maxConnectionIdleTime;
    }

    protected void setMaxConnectionIdleTime(final long timeout) {
        maxConnectionIdleTime = timeout;
    }

    protected long getSocketTimeout() {
        return socketTimeout;
    }

    protected void setSocketTimeoutUnit(final long timeout) {
        socketTimeout = timeout;
    }

    protected URI getApiRootUri() {
        return apiRootUri;
    }

    protected void setApiRootUri(final URI uri) {
        apiRootUri = uri;
    }

    protected Charset getDefaultCharset() {
        return defaultCharset;
    }

    protected void setDefaultCharset(final Charset charset) {
        defaultCharset = charset;
    }

    protected ContentType getHttpRequestContentType() {
        return httpRequestContentType;
    }

    protected void setHttpRequestContentType(final ContentType contentType) {
        httpRequestContentType = contentType;
    }

    protected DataModelManagerImpl getDataModelManager() {
        return dataManager;
    }

    /**
     * プラットフォームのAPIへデータを送信します。
     *
     * @param apiUri プラットフォームのAPI URI
     * @param headers HTTP リクエストのヘッダー
     * @param entity HTTP リクエストのボディ
     * @return プラットフォームのレスポンス
     */
    protected void post(final URI apiUri, final Header[] headers, final HttpEntity entity) {
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
                dataManager.getPlatformResponse(httpResponse);
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
    protected final HttpEntity createHttpEntity(final String s) {
        return createHttpEntity(s.getBytes(defaultCharset), httpRequestContentType);
    }

    /**
     * 文字セットを指定し、文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @param charset HTTPリクエストボディの文字セット
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(final String s, String charset) {
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
    protected final HttpEntity createHttpEntity(final String s, final String charset, final ContentType contentType) {
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
    protected final HttpEntity createHttpEntity(final String s, final Charset charset) {
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
    protected final HttpEntity createHttpEntity(final String s, final Charset charset, final ContentType contentType) {
        return createHttpEntity(s.getBytes(charset), contentType);
    }

    /**
     * コンテンツタイプを指定し、文字列からHTTPリクエストのボディを作成します。
     *
     * @param s HTTPリクエストボディの文字列
     * @param contentType HTTPリクエストボディのコンテンツタイプ
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(final String s, final ContentType contentType) {
        return createHttpEntity(s.getBytes(defaultCharset), contentType);
    }

    /**
     * バイト配列からHTTPリクエストのボディを作成します。
     *
     * @param b HTTPリクエストボディのバイト配列
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(final byte[] b) {
        return createHttpEntity(b, httpRequestContentType);
    }

    /**
     * コンテンツタイプを指定し、バイト配列からHTTPリクエストのボディを作成します。
     *
     * @param b HTTPリクエストボディのバイト配列
     * @param contentType HTTPリクエストボディのコンテンツタイプ
     * @return HTTPリクエストボディ
     */
    protected final HttpEntity createHttpEntity(final byte[] b, final ContentType contentType) {
        return new ByteArrayEntity(b,  contentType);
    }

    /**
     * ユーザーリクエスト、プラットフォームリクエスト、レスポンス管理クラスのインスタンスを生成します。
     *
     * @param formData ユーザーが送信したHTMLフォームパラメータの配列
     * @return ユーザーリクエスト、プラットフォームリクエスト、レスポンス管理クラスのインスタンス
     */
    protected abstract DataModelManagerImpl createDataManager(final MultivaluedMap<String, String> formData);
}
