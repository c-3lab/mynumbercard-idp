package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.core.network.platform.CertificateType;
import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.core.network.AuthenticationRequest;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformAuthenticationResponseStructure;
import com.example.mynumbercardidp.keycloak.util.Encryption;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import com.example.mynumbercardidp.keycloak.core.network.platform.AbstractDataModelManager;
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
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.RealmModel;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.Key;
import javax.ws.rs.core.MultivaluedMap;

public class DataModelManager extends AbstractDataModelManager {
    private static Logger consoleLogger = Logger.getLogger(DataModelManager.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static JsonFactory jsonFactory = new JsonFactory();
    private AuthenticationFlowContext context;
    private String decryptedJWE = null;

    void setContext(final AuthenticationFlowContext context) {
        this.context = context;
    }

    @Override
    protected AuthenticationRequest toUserRequest(final MultivaluedMap<String, String> formData) {
        formData.forEach((k, v) -> DataModelManager.consoleLogger.debug("Key " + k + " -> " + v));
        String actionMode = formData.getFirst(AuthenticationRequest.Filed.ACTION_MODE.getName());
        DataModelManager.consoleLogger.debug("actionMode: " + actionMode);
        AuthenticationRequest userRequest = new AuthenticationRequest();
        userRequest.setActionMode(actionMode);
        userRequest.setApplicantData(formData.getFirst(AuthenticationRequest.Filed.APPLICANT_DATA.getName()))
                .setSign(formData.getFirst(AuthenticationRequest.Filed.SIGN.getName()));

        switch (userRequest.getActionMode().toLowerCase()) {
            case "login":
                userRequest.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION);
                break;
            case "registration":
                userRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
                break;
            case "replacement":
                userRequest.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
                break;
        }
        String certificateTypeName = userRequest.getCertificateType().getName();
        // プラットフォーム通信時に証明書の元データを利用するため、decrypt済みのデータを別で保管しておく
        try {
            RealmModel realm = this.context.getRealm();
            Key privateKey = context.getSession().keys().getActiveKey(realm, KeyUse.ENC, "RSA-OAEP-256").getPrivateKey();
            this.decryptedJWE = Encryption.decrypt(formData.getFirst(certificateTypeName), privateKey).get("claim").asText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        userRequest.setCertificate(formData.getFirst(certificateTypeName));
        return userRequest;
    }

    @Override
    protected Object toPlatformRequest(final AuthenticationRequest userRequest) {
        String requestSender = super.getPlatformRequestSender();
        PlatformAuthenticationRequest platform = new PlatformAuthenticationRequest(requestSender);
        String encryptedJWE = null;
        try {
            String platformRootUrl = CurrentConfig.getValue(context, SpiConfigProperty.CertificateValidatorRootUri.CONFIG.getName());
            String jwksUrl = platformRootUrl + "/key/jwks.json";
            encryptedJWE = Encryption.encrypt(context.getSession(), this.decryptedJWE, jwksUrl);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if (userRequest.getCertificateType() == CertificateType.ENCRYPTED_USER_AUTHENTICATION) {
            platform.setCertificateType(CertificateType.ENCRYPTED_USER_AUTHENTICATION_FOR_PLATFORM)
                .setCertificate(encryptedJWE)
                .setNonceData(userRequest.getApplicantData())
                .setNonceSign(userRequest.getSign());
        } else {
            platform.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE_FOR_PLATFORM)
                .setCertificate(encryptedJWE)
                .setApplicantData(userRequest.getApplicantData())
                .setSign(userRequest.getSign());
        }
        return (Object) platform;
    }

    @Override
    protected PlatformAuthenticationResponseStructure toPlatformResponse(final CloseableHttpResponse httpResponse) {
        PlatformAuthenticationResponse response = new PlatformAuthenticationResponse();
        try (InputStream inputStream = httpResponse.getEntity().getContent()) {
            String contentsBody = IOUtils.toString(inputStream, super.getRequestCharset());
            ObjectReader objectReader = DataModelManager.objectMapper.readerFor(PlatformAuthenticationResponse.class);
            try (JsonParser parser = DataModelManager.jsonFactory.createParser(contentsBody)) {
                response = objectReader.readValue(parser);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        response.setHttpStatusCode(httpResponse.getStatusLine().getStatusCode());
        return (PlatformAuthenticationResponseStructure) response;
    }

    protected String convertPlatformRequestToJson() {
        try {
            Object requestObj = super.getPlatformRequest();
            PlatformAuthenticationRequest request = (PlatformAuthenticationRequest) requestObj;
            ObjectWriter objectWriter = DataModelManager.objectMapper.writerFor(PlatformAuthenticationRequest.class);
            String baseJson = objectWriter.writeValueAsString(request);
            ObjectReader objectReader = DataModelManager.objectMapper.readerFor(PlatformAuthenticationRequest.class);
            ObjectNode objectNode = objectReader.readTree(baseJson).deepCopy();
            objectNode.put(request.getCertificateType().getName(), request.getCertificate());
            objectWriter = DataModelManager.objectMapper.writer();
            return objectWriter.writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
