package com.example.mynumbercardidp.keycloak.authentication.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
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
    public static final String RELAY_DEST_FILE_ATTR_NAME_SIGN = "encryptedDigitalSignatureCertificate";
    public static final String RELAY_DEST_FILE_ATTR_NAME_USER = "userAuthenticationCertificate";
    public static final String USER_IDENTITY_ATTR_NAME = "uniqueId";
    public static final String SIGNATURE_ATTR_NAME = "sign";

    public static boolean checkCertificateFormat(String contents) {
        boolean result = false;
        try {
            InputStream inputStream =
                new ByteArrayInputStream(Base64.getDecoder().decode(contents.getBytes("utf-8")));
            try {
            // PEM（X.509証明書）のみを受け入れる
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(inputStream);
                result = true;
            } catch (CertificateException pem_e) {
                result = false;
            } finally {
                try {
                        inputStream.close();
                } catch (IOException io_e) {
                            io_e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException stream_e) {
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
        // POSTデータを作成する
        HttpPost httpPost = new HttpPost(verifyDestUrl);
        httpPost.setHeader(HTML_HEADER_ENCODING_TYPE, MediaType.MULTIPART_FORM_DATA);

        MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
        multiPartEntityBuilder.setMode(HttpMultipartMode.LEGACY);
        multiPartEntityBuilder.setCharset(StandardCharsets.UTF_8);

        multiPartEntityBuilder.addBinaryBody(
            fileAttributeName,
            certificateBase64Content.getBytes());

        httpPost.setEntity(multiPartEntityBuilder.build());

        // 証明書ファイルを送付する
        JsonNode responseData = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            final HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity == null) {
                return null;
            }
            try (InputStream inputStream = responseEntity.getContent()) {
                ObjectMapper objectMapper = new ObjectMapper();

                String contentsBody = IOUtils.toString(inputStream, "UTF-8");

                responseData = objectMapper.readTree(contentsBody);
            } catch (Exception e) {
                // InputStream用のエラーをcatchする
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return responseData;

    }

}
