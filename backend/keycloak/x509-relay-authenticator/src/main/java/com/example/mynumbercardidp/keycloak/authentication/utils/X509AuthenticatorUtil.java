package com.example.mynumbercardidp.keycloak.authentication.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.RelayResponse;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.lang.Exception;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import javax.ws.rs.core.MediaType;


public final class X509AuthenticatorUtil {
    protected static final String HTML_HEADER_ENCODING_TYPE = "enctype";
    public static final String RELAY_DEST_FILE_ATTR_NAME = "x509File";
    public static final String USER_IDENTITY_ATTR_NAME = "uniqueId";
    public static final String SIGNATURE_ATTR_NAME = "signature";

    private static Logger consoleLogger = Logger.getLogger(X509AuthenticatorUtil.class);

    public static boolean checkCertificateFormat(String contents) {
        boolean result = false;
        try {
            InputStream inputStream =
                new ByteArrayInputStream(Base64.getDecoder().decode(contents.getBytes("utf-8")));
            try {
            // Accept PEM (X.509 Certificate) file only.
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(inputStream);
                consoleLogger.debug("Certificate type: " + cert.getType());
                result = true;
            } catch (CertificateException pem_e) {
                consoleLogger.warn("CertificateException: Not certificate file format.");
                result = false;
            } finally {
                try {
                        inputStream.close();
                } catch (IOException io_e) {
                            io_e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException stream_e) {
            consoleLogger.warn(
                "UnsupportedEncodingException: Expect a Base64 encoded PEM file contents without newlines."
            );
            stream_e.printStackTrace();
            result = false;
        }

        return result;
    }

    public static boolean verifySignature(String signature, String certificateBase64Content, String nonce) {
        try {
            Certificate x509 = CertificateFactory.getInstance("X.509").generateCertificate(
                                   new ByteArrayInputStream(Base64.getDecoder().decode(
                                           certificateBase64Content.getBytes("utf-8")
                                       )));
            Signature engine = Signature.getInstance("SHA256withRSA");
            engine.initVerify(x509);
            engine.update(nonce.getBytes("utf-8"));
            boolean result = engine.verify(Base64.getDecoder().decode(signature.getBytes("utf-8")));
            consoleLogger.debug("Signature verify result: " + (result ? "success" : "failed"));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JsonNode verifyCertificate(String verifyDestUrl,
                                             String fileAttributeName,
                                             String certificateBase64Content)
            throws NullPointerException {
        // Create post data.
        HttpPost httpPost = new HttpPost(verifyDestUrl);
        httpPost.setHeader(HTML_HEADER_ENCODING_TYPE, MediaType.MULTIPART_FORM_DATA);

        MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
        multiPartEntityBuilder.setMode(HttpMultipartMode.LEGACY);
        multiPartEntityBuilder.setCharset(StandardCharsets.UTF_8);

        multiPartEntityBuilder.addBinaryBody(
            fileAttributeName,
            certificateBase64Content.getBytes());

        httpPost.setEntity(multiPartEntityBuilder.build());

        // Send certificate file.
        JsonNode responseData = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            final HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity == null) {
                consoleLogger.error("responceEntity is empty.");
                consoleLogger.error("Please check certificate verify server status.");
                return null;
            }
            try (InputStream inputStream = responseEntity.getContent()) {
                ObjectMapper objectMapper = new ObjectMapper();

                consoleLogger.trace("HTTP Response:");
                String contentsBody = IOUtils.toString(inputStream, "UTF-8");
                consoleLogger.trace(contentsBody);

                responseData = objectMapper.readTree(contentsBody);
            } catch (Exception e) {
                 // [Note] This block is reserved to catch certain errors related to InputStream.
                 throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return responseData;

    }

    public static RelayResponse convertRelayResponse(JsonNode responseData) {
       // Parse the JSON format and assign it to the RelayResponse type.
       // Return a RelayResponse type to the caller.
       ObjectMapper mapper = new ObjectMapper();
       try {
           RelayResponse responce =
               new RelayResponse(
                   responseData.get("uniqueId").textValue(),
                   responseData.get("verifyResultCode").intValue(),
                   responseData.get("name").textValue(),
                   responseData.get("gender").textValue(),
                   responseData.get("address").textValue(),
                   responseData.get("birthDate").textValue());
           consoleLogger.trace("convert result: " + responce.toString());
           return responce;
       } catch (Exception e) {
           e.printStackTrace();
           return new RelayResponse("", RelayResponse.VERIFY_RESULT_INTERNAL_ERROR);
       }
    }
}
