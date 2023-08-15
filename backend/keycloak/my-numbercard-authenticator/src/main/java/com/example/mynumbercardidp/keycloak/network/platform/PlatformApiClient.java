package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.AbstractPlatformApiClient;
import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.core.network.platform.UserRequestModelImpl;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
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
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Logger consoleLogger = Logger.getLogger(PlatformApiClient.class);
    private String platformRequestSender = "";

    {
        setHttpConnectTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
        setHttpRequestTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
        setHttpRequestContentType(REQUEST_CONTENT_TYPE);
    }

    @Override
    public void action() {
        try {
            UserRequestModel userRequest = (UserRequestModel) this.userRequest;
            URI apiUri = new URI(getApiRootUri().toString() + API_URI_PATH + userRequest.getActionMode());
            Header[] headers = { new BasicHeader("Content-type", REQUEST_CONTENT_TYPE) };
            this.platformRequest = (Object) toPlatformRequest((UserRequestModelImpl) userRequest);

            PlatformRequestModel platformRequest = (PlatformRequestModel) this.platformRequest;
            HttpEntity requsetEntity = createHttpEntity(platformRequest.toJsonObject().toString());
            consoleLogger.debug("Platform API URI: " + apiUri);
            post(apiUri, headers, requsetEntity);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected UserRequestModelImpl toUserRequest(MultivaluedMap<String, String> formData) {
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
        return (UserRequestModelImpl) user;
    }

    @Override
    protected Object toPlatformRequest(UserRequestModelImpl user) {
        PlatformRequestModel platform = new PlatformRequestModel();
        UserRequestModel userReq = (UserRequestModel) user;
        platform.setCertificateType(userReq.getCertificateType())
            .setCertificate(userReq.getCertificate())
            .setApplicantData(userReq.getApplicantData())
            .setSign(userReq.getSign());
        platform.getRequestInfo().setSender(platformRequestSender);
        return (Object) platform;
    }

    @Override
    protected Object toPlatformResponse(CloseableHttpResponse httpResponse) {
        PlatformResponseModel response = new PlatformResponseModel();
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            ObjectReader objectReader = objectMapper.reader();
            String contentsBody = IOUtils.toString(inputStream, getDefaultCharset());
            response = objectMapper.readValue(contentsBody, PlatformResponseModel.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        response.setHttpStatusCode(httpResponse.getCode());
        return (Object) response;
    }

    @Override
    public void ensureHasUniqueId() {
        PlatformResponseModel response = (PlatformResponseModel) platformResponse;
        String uniqueId = response.getUniqueId();
        if (StringUtil.isStringEmpty(uniqueId)) {
            throw new IllegalStateException("The unique id is empty in platform response.");
        }
    }

    @Override
    public UserModel addUserModelAttributes(UserModel user) {
        HashMap<String, String> userAttributes = new HashMap<>();
        PlatformResponseModel response = (PlatformResponseModel) platformResponse;
        PlatformResponseModel.IdentityInfo identity = response.identityInfo;
        userAttributes.put("uniqueId",  identity.uniqueId);
        userAttributes.put("name",  identity.name);
        userAttributes.put("gender_code",  identity.gender);
        userAttributes.put("user_address",  identity.address);
        userAttributes.put("birth_date",  identity.dateOfBirth);
        userAttributes.forEach((k, v) -> user.setSingleAttribute(k, v));
        return user;
    }
}
