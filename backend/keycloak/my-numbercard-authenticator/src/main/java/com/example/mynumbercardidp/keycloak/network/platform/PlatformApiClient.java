package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.AbstractPlatformApiClient;
import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.core.network.platform.UserRequestModelImpl;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
    private final static String API_URI_PATH = "/verify/";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static JsonFactory jsonFactory = new JsonFactory();
    private static Logger consoleLogger = Logger.getLogger(PlatformApiClient.class);
    private String platformRequestSender = "";

    {
        httpRequestContentType = REQUEST_CONTENT_TYPE;
    }

    @Override
    public void action() {
        try {
            UserRequestModel userRequest = (UserRequestModel) this.userRequest;
            URI apiUri = new URI(apiRootUri.toString() + API_URI_PATH + userRequest.getActionMode());
            Header[] headers = {};
            platformRequest = (Object) toPlatformRequest((UserRequestModelImpl) userRequest);
            PlatformRequestModel request = (PlatformRequestModel) platformRequest;
            ObjectWriter objectWriter = objectMapper.writerFor(PlatformRequestModel.class);
            ObjectReader objectReader = objectMapper.readerFor(PlatformRequestModel.class);
            ObjectNode objectNode = objectReader.readTree(objectWriter.writeValueAsString(request)).deepCopy();
            objectNode.put(request.getCertificateType().getName(), request.getCertificate());
            objectWriter = objectMapper.writer();
            HttpEntity requsetEntity = createHttpEntity(objectWriter.writeValueAsString(objectNode));
            consoleLogger.debug("Platform API URI: " + apiUri);
            post(apiUri, headers, requsetEntity);
        } catch (URISyntaxException | JsonProcessingException e) {
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
        PlatformRequestModel platform = new PlatformRequestModel(platformRequestSender);
        UserRequestModel userReq = (UserRequestModel) user;
        platform.setCertificateType(userReq.getCertificateType())
            .setCertificate(userReq.getCertificate())
            .setApplicantData(userReq.getApplicantData())
            .setSign(userReq.getSign());
        return (Object) platform;
    }

    @Override
    protected Object toPlatformResponse(CloseableHttpResponse httpResponse) {
        PlatformResponseModel response = new PlatformResponseModel();
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            String contentsBody = IOUtils.toString(inputStream, defaultCharset);
            ObjectReader objectReader = objectMapper.readerFor(PlatformResponseModel.class);
            try (JsonParser parser = jsonFactory.createParser(contentsBody)) {
                response = objectReader.readValue(parser);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        response.setHttpStatusCode(httpResponse.getStatusLine().getStatusCode());
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
