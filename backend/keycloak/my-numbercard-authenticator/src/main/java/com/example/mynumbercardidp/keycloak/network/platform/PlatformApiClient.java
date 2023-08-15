package com.example.mynumbercardidp.keycloak.network.platform;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.Header;
import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;

public class PlatformApiClient extends AbstractPlatformApiClient {
    private final static ContentType REQUEST_CONTENT_TYPE = ContentType.APPLICATION_JSON;
    private final static long HTTP_TIMEOUT = 15;
    private final static String API_URI_PATH = "/verify/";
    private static ObjectMapper objectMapper;
    private static Logger consoleLogger = Logger.getLogger(PlatformApiClient.class);
    private String platformRequestSender = "";

    static {
        objectMapper = new ObjectMapper();
    }


    {
        setHttpConnectTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
        setHttpRequestTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
        setHttpRequestContentType(REQUEST_CONTENT_TYPE);
    }

    @Override
    public void action() {
        try {
            UserRequestModel userRequest = getUserRequest();
            URI apiUri = new URI(getApiRootUri().toString() + API_URI_PATH + userRequest.getActionMode());
            Header[] headers = { new BasicHeader("Content-type", REQUEST_CONTENT_TYPE) };
            setPlatformRequest(toPlatformRequest(userRequest));

            HttpEntity requsetEntity = createHttpEntity(getPlatformRequest().toJsonObject().toString());
            consoleLogger.debug("Platform API URI: " + apiUri);
            post(apiUri, headers, requsetEntity);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected UserRequestModel toUserRequest(MultivaluedMap<String, String> formData) {
        formData.forEach((k, v) -> consoleLogger.debug("Key " + k + " -> " + v));
        String actionMode = formData.getFirst(UserRequestModel.Filed.ACTION_MODE.getName());
        consoleLogger.debug("actionMode: " + actionMode);
        UserRequestModel user = new UserRequestModel();
        user.setActionMode(actionMode)
            .setApplicantData(formData.getFirst(UserRequestModel.Filed.APPLICANT_DATA.getName()))
            .setSign(formData.getFirst(UserRequestModel.Filed.SIGN.getName()));

        switch (user.getActionMode().toLowerCase()) {
            case "login":
                user.setCertificateType(CertificateType.USER_AUTHENTICATION);
                break;
            case "registration":
            case "replacement":
                user.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
                break;
        }
        String certificateTypeName = user.getCertificateType().getName();
        user.setCertificate(formData.getFirst(certificateTypeName));
        return user;
    }

    @Override
    protected PlatformRequestModel toPlatformRequest(UserRequestModel user) {
        PlatformRequestModel platform = new PlatformRequestModel();
        platform.setCertificateType(user.getCertificateType())
            .setCertificate(user.getCertificate())
            .setApplicantData(user.getApplicantData())
            .setSign(user.getSign());
        platform.getRequestInfo().setSender(getPlatformRequestSender());
        return platform;
    }

    @Override
    protected PlatformResponseModel toPlatformResponse(CloseableHttpResponse httpResponse) {
        PlatformResponseModel response = new PlatformResponseModel();
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            ObjectReader objectReader = objectMapper.reader();
            String contentsBody = IOUtils.toString(inputStream, getDefaultCharset());
            response = objectMapper.readValue(contentsBody, PlatformResponseModel.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        response.setHttpStatusCode(httpResponse.getCode());
        return response;
    }

    @Override
    public void ensureHasUniqueId() {
        String uniqueId = platformResponse.getUniqueId();
        if (StringUtil.isStringEmpty(uniqueId)) {
            throw new IllegalStateException("The unique id is empty in platform response.");
        }
    }

    @Override
    public UserModel addUserModelAttributes(UserModel user) {
        consoleLogger.debug("Start method: addUserModelAttributes");
        HashMap<String, String> userAttributes = new HashMap<>();
        PlatformResponseModel.IdentityInfo identity = platformResponse.identityInfo;
        userAttributes.put("uniqueId",  identity.uniqueId);
        userAttributes.put("name",  identity.name);
        userAttributes.put("gender_code",  identity.gender);
        userAttributes.put("user_address",  identity.address);
        userAttributes.put("birth_date",  identity.dateOfBirth);
        userAttributes.forEach((k, v) -> user.setSingleAttribute(k, v));
        return user;
    }
}
