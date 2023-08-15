package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.core.network.UserRequestModelImpl;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformResponseModelImpl;
import com.example.mynumbercardidp.keycloak.core.network.platform.AbstractDataModelManager;
import com.example.mynumbercardidp.keycloak.network.UserRequestModel;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import javax.ws.rs.core.MultivaluedMap;

public class DataModelManager extends AbstractDataModelManager {
    private static Logger consoleLogger = Logger.getLogger(DataModelManager.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static JsonFactory jsonFactory = new JsonFactory();

    @Override
    protected UserRequestModelImpl toUserRequest(final MultivaluedMap<String, String> formData) {
        formData.forEach((k, v) -> consoleLogger.debug("Key " + k + " -> " + v));
        String actionMode = formData.getFirst(UserRequestModel.Filed.ACTION_MODE.getName());
        consoleLogger.debug("actionMode: " + actionMode);
        UserRequestModel user = new UserRequestModel();
        user.setActionMode(actionMode);
        user.setApplicantData(formData.getFirst(UserRequestModel.Filed.APPLICANT_DATA.getName()))
            .setSign(formData.getFirst(UserRequestModel.Filed.SIGN.getName()));

        switch (user.getActionMode().toLowerCase()) {
            case "login":
                user.setCertificateType(CertificateType.USER_AUTHENTICATION);
                break;
            case "registration":
                // フォールスルー
            case "replacement":
                user.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
                break;
        }
        String certificateTypeName = user.getCertificateType().getName();
        user.setCertificate(formData.getFirst(certificateTypeName));
        return user;
    }

    @Override
    protected Object toPlatformRequest(final UserRequestModelImpl user) {
        String requestSender = super.getPlatformRequestSender();
        PlatformRequestModel platform = new PlatformRequestModel(requestSender);
        UserRequestModel userReq = (UserRequestModel) user;
        platform.setCertificateType(userReq.getCertificateType())
            .setCertificate(userReq.getCertificate())
            .setApplicantData(userReq.getApplicantData())
            .setSign(userReq.getSign());
        return (Object) platform;
    }

    @Override
    protected PlatformResponseModelImpl toPlatformResponse(final CloseableHttpResponse httpResponse) {
        PlatformResponseModel response = new PlatformResponseModel();
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            String contentsBody = IOUtils.toString(inputStream, super.getRequestCharset());
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
        return (PlatformResponseModelImpl) response;
    }

    protected String convertPlatformRequestToJson() {
        try {
            Object requestObj = super.getPlatformRequest();
            PlatformRequestModel request = (PlatformRequestModel) requestObj;
            ObjectWriter objectWriter = objectMapper.writerFor(PlatformRequestModel.class);
            String baseJson = objectWriter.writeValueAsString(request);
            ObjectReader objectReader = objectMapper.readerFor(PlatformRequestModel.class);
            ObjectNode objectNode = objectReader.readTree(baseJson).deepCopy();
            objectNode.put(request.getCertificateType().getName(), request.getCertificate());
            objectWriter = objectMapper.writer();
            return objectWriter.writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
