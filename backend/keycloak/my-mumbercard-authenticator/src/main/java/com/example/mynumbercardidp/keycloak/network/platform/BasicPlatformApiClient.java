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

public class BasicPlatformApiClient extends AbstractPlatformApiClient {
   private final static ContentType REQUEST_CONTENT_TYPE = ContentType.APPLICATION_JSON;
   private final static long HTTP_TIMEOUT = 15;
   private final static String API_URI_PATH = "/verify";
   private static ObjectMapper objectMapper;
   private static RequestBuilder requestBuilder;
   private static Logger consoleLogger = Logger.getLogger(BasicPlatformApiClient.class);

   static {
       objectMapper = new ObjectMapper();
   }

   private UserRequestModelImpl userRequest;
   private String platformRequestSender;

   {
       setHttpConnectTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
       setHttpRequestTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
       setHttpRequestContentType(REQUEST_CONTENT_TYPE);
   }

   BasicPlatformApiClient(String apiRootUri) throws URISyntaxException {
       this(new URI(apiRootUri));
   }

   BasicPlatformApiClient(URI apiRootUri) {
       setApiRootUri(apiRootUri);

       //  [TODO] Keycloak管理コンソールにある認証フローのSPI設定からSender の情報を取得する？
       platformRequestSender = "ID123";
   }

   @Override
   public void init(MultivaluedMap<String, String> formData, String idpSender) {
       requestBuilder = new RequestBuilder(toUserRequestModel(formData));
       userRequest = (UserRequestModelImpl) requestBuilder.getUserRequest();
       platformRequestSender = idpSender;
   }

   @Override
   public CommonResponseModel action() {
       try {
           URI apiUri = new URI(getApiRootUri().toString() + API_URI_PATH + userRequest.getActionMode());
           Header[] headers = { new BasicHeader("Content-type", REQUEST_CONTENT_TYPE) };
           PlatformRequestModel platformRequest = (PlatformRequestModel) requestBuilder.toPlatformRequest(PlatformRequestModel.class, platformRequestSender);

           HttpEntity requsetEntity = createHttpEntity(platformRequest.toJsonObject().toString());
           return post(apiUri, headers, requsetEntity);
       } catch (URISyntaxException e) {
           throw new IllegalArgumentException(e);
       }
   }

   private CommonRequestModelImpl toUserRequestModel(MultivaluedMap<String, String> formData) {
        userRequest.setActionMode(formData.getFirst(UserRequestModel.Filed.ACTION_MODE.name()));

        CommonRequestModelImpl certificatePart = (CommonRequestModelImpl) userRequest;
            certificatePart.setApplicantData(formData.getFirst(UserRequestModel.Filed.APPLICANT_DATA.name()))
                .setSign(formData.getFirst(UserRequestModel.Filed.SIGN.name()));

        switch (userRequest.getActionMode().toLowerCase()) {
            case "login":
                certificatePart.setCertificateType(CertificateType.USER_AUTHENTICATION);
                break;
            case "registration":
            case "replacement":
                certificatePart.setCertificateType(CertificateType.ENCRYPTED_DIGITAL_SIGNATURE);
                break;
        }
        String certificateTypeName = certificatePart.getCertificateType().name();
        certificatePart.setCertificate(formData.getFirst(certificateTypeName));
        return (CommonRequestModelImpl) userRequest;
   }

   // private AbstractRequestBean toPlatformRequest() {
   //     AbstractRequestBean platformRequest = new PlatformRequestBean(platformRequestSender).setCertificateType(userRequest.getCertificateType())
   //         .setCertificate(userRequest.getCertificate())
   //         .setApplicantData(userRequest.getApplicantData())
   //         .setSign(userRequest.getSign());
   //    return platformRequest;
   // }

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
