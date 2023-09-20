package com.example.mynumbercardidp.keycloak.core.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.nio.charset.Charset;
import java.util.Objects;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Keycloakへ送信されたユーザーのリクエストデータやプラットフォームへ送信するリクエストデータ、
 * プラットフォームから送信されたレスポンスデータを管理する機能の抽象クラスです。
 */
public abstract class AbstractDataModelManager implements RequestAndResponseDataManager {
    /** ユーザーが送信したHTMLフォームパラメータ */
    private MultivaluedMap<String, String> userFormData;
    /** プラットフォームへリクエストを送る場合の文字セット */
    private Charset requestCharset = Charset.forName("UTF-8");
    /** プラットフォームへ送信するIdP送信者符号 */
    private String platformRequestSender = "";
    /** ユーザーリクエストのデータ構造 */
    private AuthenticationRequest userRequest;
    /** プラットフォームリクエストのデータ構造 */
    private Object platformRequest;
    /** プラットフォームレスポンスのデータ構造 */
    private PlatformAuthenticationResponseStructure platformResponse;

    @Override
    public void setPlatformRequestSender(final String requestSender) {
        this.platformRequestSender = Objects.requireNonNull(requestSender);
    }

    @Override
    public void setUserFormData(final MultivaluedMap<String, String> formData) {
        this.userFormData = formData;
    }

    @Override
    public void setRequestCharset(final Charset charset) {
        this.requestCharset = charset;
    }

    /**
     * ユーザーリクエストのデータ構造を返します。必要に応じて、HTMLフォームパラメータから変換します。
     *
     * @return ユーザーリクエストのデータ構造
     */
    @Override
    public AuthenticationRequest getUserRequest() {
        if (Objects.isNull(this.userRequest)) {
            this.userRequest = toUserRequest(this.userFormData);
        }
        return this.userRequest;
    }

    /**
     * プラットフォームリクエストのデータ構造を返します。必要に応じて、ユーザーリクエストのデータ構造から変換します。
     *
     * @return ユーザーリクエストのデータ構造
     */
    @Override
    public Object getPlatformRequest() {
        if (Objects.isNull(this.platformRequest)) {
            this.platformRequest = toPlatformRequest(this.userRequest);
        }
        return this.platformRequest;
    }

    @Override
    public PlatformAuthenticationResponseStructure getPlatformResponse() {
        return this.platformResponse;
    }

    @Override
    public void setPlatformResponseFromHttpResponse(final CloseableHttpResponse httpResponse) {
        this.platformResponse = toPlatformResponse(Objects.requireNonNull(httpResponse));
    }

    protected Charset getRequestCharset() {
        return this.requestCharset;
    }

    protected String getPlatformRequestSender() {
        return this.platformRequestSender;
    }

    protected void setUserRequest(final AuthenticationRequest request) {
        this.userRequest = request;
    }

    protected void setPlatformRequest(final Object request) {
        this.platformRequest = request;
    }

    protected void setPlatformResponse(final PlatformAuthenticationResponseStructure response) {
        this.platformResponse = response;
    }

    protected abstract AuthenticationRequest toUserRequest(MultivaluedMap<String, String> formData);

    protected abstract Object toPlatformRequest(AuthenticationRequest userRequestStructure);

    protected abstract PlatformAuthenticationResponseStructure toPlatformResponse(CloseableHttpResponse httpResponse);
}
