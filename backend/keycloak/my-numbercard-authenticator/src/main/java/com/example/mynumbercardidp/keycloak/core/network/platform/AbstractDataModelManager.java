package com.example.mynumbercardidp.keycloak.core.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.UserRequestModelImpl;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.nio.charset.Charset;
import java.util.Objects;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Keycloakへ送信されたユーザーのリクエストデータやプラットフォームへ送信するリクエストデータ、
 * プラットフォームから送信されたレスポンスデータを管理する機能の抽象クラスです。
 */
public abstract class AbstractDataModelManager implements DataModelManagerImpl {
    /** ユーザーが送信したHTMLフォームパラメータ */
    private MultivaluedMap<String, String> userFormData;
    /** プラットフォームへリクエストを送る場合の文字セット */
    private Charset requestCharset = Charset.forName("UTF-8");
    /** プラットフォームへ送信するIdP送信者符号 */
    private String platformRequestSender = "";
    /** ユーザーリクエストのデータ構造 */
    private UserRequestModelImpl userRequest;
    /** プラットフォームリクエストのデータ構造 */
    private Object platformRequest;
    /** プラットフォームレスポンスのデータ構造 */
    private PlatformResponseModelImpl platformResponse;

    @Override
    public void setPlatformRequestSender(final String requestSender) {
        platformRequestSender = Objects.requireNonNull(requestSender);
    }

    @Override
    public void setUserFormData(final MultivaluedMap<String, String> formData) {
        userFormData = formData;
    }

    @Override
    public void setRequestCharset(final Charset charset) {
        requestCharset = charset;
    }

    @Override
    public UserRequestModelImpl getUserRequest() {
        return toUserRequestIfNeeded();
    }

    @Override
    public Object getPlatformRequest() {
        return toPlatformRequestIfNeeded();
    }

    @Override
    public PlatformResponseModelImpl getPlatformResponse() {
        return platformResponse;
    }

    @Override
    public Object getPlatformResponse(final CloseableHttpResponse httpResponse) {
        platformResponse = toPlatformResponse(Objects.requireNonNull(httpResponse));
        return platformResponse;
    }

    protected Charset getRequestCharset() {
        return requestCharset;
    }

    protected String getPlatformRequestSender() {
        return platformRequestSender;
    }

    protected void setUserRequest(final UserRequestModelImpl request) {
        userRequest = request;
    }

    protected void setPlatformRequest(final Object request) {
        platformRequest = request;
    }

    protected void setPlatformResponse(final PlatformResponseModelImpl response) {
        platformResponse = response;
    }

    protected abstract UserRequestModelImpl toUserRequest(MultivaluedMap<String, String> formData);
    protected abstract Object toPlatformRequest(UserRequestModelImpl user);
    protected abstract PlatformResponseModelImpl toPlatformResponse(CloseableHttpResponse httpResponse);

    /**
     * ユーザーリクエストのデータ構造を返します。必要に応じて、HTMLフォームパラメータから変換します。
     *
     * @return ユーザーリクエストのデータ構造
     */
    private UserRequestModelImpl toUserRequestIfNeeded() {
        if (Objects.isNull(userRequest)) {
            userRequest = toUserRequest(userFormData);
        }
        return userRequest;
    }

    /**
     * プラットフォームリクエストのデータ構造を返します。必要に応じて、ユーザーリクエストのデータ構造から変換します。
     *
     * @param request ユーザーリクエストのデータ構造
     * @return ユーザーリクエストのデータ構造
     */
    private Object toPlatformRequestIfNeeded() {
        if (Objects.isNull(platformRequest)) {
            platformRequest = toPlatformRequest(userRequest);
        }
        return platformRequest;
    }
}
