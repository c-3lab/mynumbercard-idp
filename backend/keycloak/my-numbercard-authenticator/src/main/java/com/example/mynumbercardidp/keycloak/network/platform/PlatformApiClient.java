package com.example.mynumbercardidp.keycloak.network.platform;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.Header;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;

public class PlatformApiClient extends AbstractPlatformApiClient {
    private final static ContentType REQUEST_CONTENT_TYPE = ContentType.APPLICATION_JSON;
    private final static long HTTP_TIMEOUT = 15;
    private final static String API_URI_PATH = "/verify";
    private static ObjectMapper objectMapper;
    private static Logger consoleLogger = Logger.getLogger(PlatformApiClient.class);
    /** ユーザーリクエストのデータ構造 */
    protected UserRequestMode userRequest;
    /** プラットフォームリクエストのデータ構造 */
    protected PlatformRequestMode platformRequest;
    /** プラットフォームレスポンスのデータ構造 */
    protected PlatformResponseModel platformResponse;
    private String platformRequestSender = "";


    static {
        objectMapper = new ObjectMapper();
    }


    {
        setHttpConnectTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
        setHttpRequestTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
        setHttpRequestContentType(REQUEST_CONTENT_TYPE);
    }

    PlatformApiClient(String apiRootUri) throws URISyntaxException {
        this(new URI(apiRootUri));
    }

    PlatformApiClient(URI apiRootUri) {
        setApiRootUri(apiRootUri);
    }

    @Override
    public void init(MultivaluedMap<String, String> formData, String idpSender) {
        toUserRequest(formData);
        platformRequestSender = idpSender;
    }

    @Override
    public CommonResponseModel action() {
        try {
            URI apiUri = new URI(getApiRootUri().toString() + API_URI_PATH + userRequest.getActionMode());
            Header[] headers = { new BasicHeader("Content-type", REQUEST_CONTENT_TYPE) };
            PlatformRequestModel platformRequest = (PlatformRequestModel) requestBuilder.toPlatformRequest(platformRequestSender);

            HttpEntity requsetEntity = createHttpEntity(platformRequest.toJsonObject().toString());
            return post(apiUri, headers, requsetEntity);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected UserRequestModel toUserRequest(MultivaluedMap<String, String> formData) {
        formData.forEach((k, v) -> consoleLogger.debug("Key " + k + " -> " + v));
        String actionMode = formData.getFirst(UserRequestModel.Filed.ACTION_MODE.getName());
        consoleLogger.debug("actionMode: " + actionMode);
        userRequest.setActionMode(actionMode)
            .setApplicantData(formData.getFirst(UserRequestModel.Filed.APPLICANT_DATA.getName()))
            .setSign(formData.getFirst(UserRequestModel.Filed.SIGN.getName()));

        switch (userRequest.getActionMode().toLowerCase()) {
            case "login":
                userRequest.setCertificateType(CertificateType.USER_AUTHENTICATION);
                break;
            case "registration":
            case "replacement":
                userRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
                break;
        }
        String certificateTypeName = certificatePart.getCertificateType().getName();
        userRequest.setCertificate(formData.getFirst(certificateTypeName));
        return userRequest;
    }

    @Override
    protected PlatformRequestModel toPlatformRequest() {
        platformRequest.setCertificateType(userRequest.getCertificateType())
            .setCertificate(userRequest.getCertificate())
            .setApplicantData(userRequest.getApplicantData())
            .setSign(userRequest.getSign());
        platformRequest.PlatformRequest.setSender(platformRequestSender);
        return platformRequest;
    }

    @Override
    protected CommonResponseModel toPlatformResponse(CloseableHttpResponse httpResponse) {
        CommonResponseModel response = null;
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            ObjectReader objectReader = objectMapper.reader();
            String contentsBody = IOUtils.toString(inputStream, getDefaultCharset());
            response = objectMapper.readValue(contentsBody, PlatformResponseModel.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        CommonResponseModel platformResponse = getPlatformResponse();
        response.setHttpStatusCode(platformResponse.getHttpStatusCode());
        setPlatformResponse(response);

        return response;
    }
}
