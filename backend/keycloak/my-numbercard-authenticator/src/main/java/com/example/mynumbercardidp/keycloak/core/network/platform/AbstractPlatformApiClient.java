package com.example.mynumbercardidp.keycloak.core.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.keycloak.connections.httpclient.HttpClientBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;

/**
 * 個人番号カードの公的個人認証部分を受け付けるプラットフォームと通信する処理を定義すべき機能を表しています。
 *
 * このクラスを継承したサブクラスは、インスタンス生成元で{@link #init(MultivaluedMap<String, String>)}を実行し、
 * プラットフォームへ送信するパラメータを受け取る必要があります。
 */
public abstract class AbstractPlatformApiClient implements PlatformApiClientInterface {

    // プラットフォームと通信するときのリクエスト設定
    private long establishConnectionTimeout = 10000L;
    private TimeUnit establishConnectionTimeoutUnit = TimeUnit.MILLISECONDS;
    private long maxConnectionIdleTime = 30000L;
    private TimeUnit maxConnectionIdleTimeUnit = TimeUnit.MILLISECONDS;
    private long socketTimeout = 30000L;
    private TimeUnit socketTimeoutUnit = TimeUnit.MILLISECONDS;
    /** プラットフォームに送信するコンテンツタイプ */
    private ContentType httpRequestContentType = ContentType.TEXT_PLAIN;
    /** プラットフォームのAPIルートURI */
    private URI apiRootUri;
    /** プラットフォームに送信するHTTP Bodyの文字セット */
    private Charset defaultCharset = Charset.forName("UTF-8");
    /** ユーザーリクエスト、プラットフォームリクエスト、レスポンス管理クラス */
    private RequestAndResponseDataManager dataManager;
    /** プラットフォームへ送るIDP識別送信者符号 */
    private String platformRequestSender = "";

    @Override
    public AuthenticationRequest getUserRequest() {
        return this.dataManager.getUserRequest();
    }

    @Override
    public Object getPlatformRequest() {
        return this.dataManager.getPlatformRequest();
    }

    @Override
    public PlatformAuthenticationResponseStructure getPlatformResponse() {
        return this.dataManager.getPlatformResponse();
    }

    @Override
    public void init(final String apiRootUri, final MultivaluedMap<String, String> formData, final String idpSender) {
        try {
            this.apiRootUri = new URI(apiRootUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        this.dataManager = createDataManager(formData);
        this.platformRequestSender = Optional.ofNullable(idpSender).orElse("");
        this.dataManager.setPlatformRequestSender(this.platformRequestSender);
        this.dataManager.setRequestCharset(this.defaultCharset);
    }

    @Override
    public abstract void sendRequest();

    protected long getEstablishConnectionTimeout() {
        return this.establishConnectionTimeout;
    }

    protected void setEstablishConnectionTimeout(final long timeout) {
        this.establishConnectionTimeout = timeout;
    }

    protected long getMaxConnectionIdleTime() {
        return this.maxConnectionIdleTime;
    }

    protected void setMaxConnectionIdleTime(final long timeout) {
        this.maxConnectionIdleTime = timeout;
    }

    protected long getSocketTimeout() {
        return this.socketTimeout;
    }

    protected void setSocketTimeoutUnit(final long timeout) {
        this.socketTimeout = timeout;
    }

    protected URI getApiRootUri() {
        return this.apiRootUri;
    }

    protected void setApiRootUri(final URI uri) {
        this.apiRootUri = uri;
    }

    protected Charset getDefaultCharset() {
        return this.defaultCharset;
    }

    protected void setDefaultCharset(final Charset charset) {
        this.defaultCharset = charset;
    }

    protected ContentType getHttpRequestContentType() {
        return this.httpRequestContentType;
    }

    protected void setHttpRequestContentType(final ContentType contentType) {
        this.httpRequestContentType = contentType;
    }

    protected RequestAndResponseDataManager getDataModelManager() {
        return this.dataManager;
    }

    /**
     * プラットフォームのAPIへデータを送信します。
     *
     * @param apiUri  プラットフォームのAPI URI
     * @param headers HTTP リクエストのヘッダー
     * @param entity  HTTP リクエストのボディ
     * @return プラットフォームのレスポンス
     */
    protected void sendEntity(final URI apiUri, final Header[] headers, final HttpEntity entity) {
        HttpPost httpPost = new HttpPost(apiUri);
        if (Objects.nonNull(headers) && 0 < headers.length) {
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, this.httpRequestContentType.toString());
            httpPost.setHeaders(headers);
        } else if (!Arrays.asList(headers).contains(HttpHeaders.CONTENT_TYPE)) {
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, this.httpRequestContentType.toString());
        }
        httpPost.setEntity(entity);

        try (CloseableHttpClient httpClient = new HttpClientBuilder().disableTrustManager()
                .establishConnectionTimeout(this.establishConnectionTimeout, this.establishConnectionTimeoutUnit)
                .maxConnectionIdleTime(this.maxConnectionIdleTime, this.maxConnectionIdleTimeUnit)
                .socketTimeout(this.socketTimeout, this.socketTimeoutUnit)
                .build()) {
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                this.dataManager.setPlatformResponseFromHttpResponse(httpResponse);
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
     * ユーザーリクエスト、プラットフォームリクエスト、レスポンス管理クラスのインスタンスを生成します。
     *
     * @param formData ユーザーが送信したHTMLフォームパラメータの配列
     * @return ユーザーリクエスト、プラットフォームリクエスト、レスポンス管理クラスのインスタンス
     */
    protected abstract RequestAndResponseDataManager createDataManager(final MultivaluedMap<String, String> formData);
}
