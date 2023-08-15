package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.AbstractActionHandler;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ActionHandler;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.network.RequestBuilder;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClientLoader;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.ContentType;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.CommonClientSessionModel.ExecutionStatus;
import org.keycloak.services.util.LocaleUtil;
import org.keycloak.services.validation.Validation;
import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.models.KeycloakSession;

import java.io.InputStream;
import java.lang.Exception;
import java.net.URISyntaxException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * このクラスは個人番号カードの公的個人認証部分を利用する認証SPIです。
 *
 * Keycloakのコンソールログは英語出力であるため、それに倣い、英語で出力します。
 * Keycloakのコンソールへ出力するログレベルはKeycloakが定義しているロギングの構成に倣います。
 * @see <a href="https://www.keycloak.org/server/logging">ロギングの構成</a>
 */
public class MyNumberCardAuthenticator extends AbstractMyNumberCardAuthenticator {
    protected static final String X509_FILE_UPLOAD_ENABLE = "x509Upload"; // 互換性維持
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "内部サーバーエラーが発生しました。";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "サービスが一時的に利用不可です。";
    private static final String UNAUTHORIZED_MESSAGE = "クライアント認証に問題が発生しました。";
    private static final String PLATFORM_URL_ERROR_MESSAGE = "プラットフォームURLに文法エラーがありました。";

    /** コンソール用ロガー */
    private static Logger consoleLogger = Logger.getLogger(MyNumberCardAuthenticator.class);

    /** Keycloakイベントロガー */
    protected static ServicesLogger logger = ServicesLogger.LOGGER;

    @Override
    /**
     * プラットフォームへ公的個人認証部分を送信し、その結果からログインや登録、登録情報の変更処理を呼び出します。
     *
     * ユーザーリクエストの構造はプラットフォームのAPIに依存しています。
     * プラットフォームAPIクライアント（コネクタ）でユーザーリクエスト構造の解析をします。
     * @param context 認証フローのコンテキスト
     */
    public void action(AuthenticationFlowContext context) {
        String platformApiClassFqdn = context.getAuthenticatorConfig()
            .getConfig()
            .get(SpiConfigProperty.PlatformApiClientClassFqdn.NAME);
        if (Objects.isNull(platformApiClassFqdn) || platformApiClassFqdn.length() == 0) {
            consoleLogger.error(SpiConfigProperty.PlatformApiClientClassFqdn.LABEL + " is empty.");
            ResponseCreater.createInternalServerErrorPage(context, null, null);
            return;
        }

        String platformRootApiUri = context.getAuthenticatorConfig()
            .getConfig()
            .get(SpiConfigProperty.PlatformApiClientUri.NAME);
        String idpSender = context.getAuthenticatorConfig()
            .getConfig()
            .get(SpiConfigProperty.PlatformApiIdpSender.NAME);
        PlatformApiClientLoader platformLoader = new PlatformApiClientLoader();
        PlatformApiClient platform = null;
        try {
            platform = platformLoader.load(platformApiClassFqdn, context, platformRootApiUri, idpSender);

            /*
             * 認証を試行するユーザーが希望している動作で処理をする。
             *
             * ActionHandlerクラスが持つメソッドの戻り値はvoid型かつ、
             * publicアクセス修飾子のメソッドはexecuteのみであるため、インスタンスを変数へ格納しない。
             */
            setLoginFormAttributes(context);
            new ActionHandler().execute(context, platform);

        } catch (RuntimeException e) {
            // RuntimeException はそのままスロー
            throw e;
        } catch (Exception e) {
            /*
             * 宣言されたExceptionを呼び出し元へのスローすることは許されていないため、
             * Exceptionを握り潰し、ユーザーへ内部エラーのレスポンス 500を返す。
             * コンソール向けにスタックトレースを表示する。
             */
            ResponseCreater.createInternalServerErrorPage(context, null, null);
            e.printStackTrace();
            return;
        }

        /*
         * [HACK] 認証試行ユーザーのセッション情報から認証フローの結果を取得します。
         *
         * 認証フローの結果が存在している場合はAuthenticatorの処理を終了し、ユーザーへHTTPレスポンスを返します。
         * そうでない場合は、ActionHandlerが呼び出したActionクラスに不備があります。
         */
        String authFlowResult = ResponseCreater.getFlowState(context);

        if (Objects.isNull(authFlowResult) || authFlowResult.length() == 0) {
            /*
             * [TODO] Exceptionを返したときにユーザーへ内部エラーのレスポンス 500を返さない動作を確認した場合、
             *        Exceptionをスローしない処理へ修正する。
             */
            ResponseCreater.createInternalServerErrorPage(context, null, null);
            throw new IllegalStateException("Not found AuthFlowResult in auth note for authentication session.");
        }

        // [HACK] ハードコーディングしている状態文字列の代わりに列挙体を使いたい。
        if ( !(authFlowResult.equals("failure") ||
                authFlowResult.equals("challenge") ||
                authFlowResult.equals("success"))) {
            return;
        }
    }

    // /**
    //  * 指定された参照がnullの場合はtrueを返します。それ以外の場合はfalseを返します。
    //  *
    //  * Objects.isNull(Object obj) のエイリアス
    //  * @param obj Nullかどうかを判定するオブジェクト
    //  * @return Nullの場合はtrue、そうでない場合はfalse
    //  */
    // protected boolean isNull(Object obj) {
    //     return Objects.isNull(obj);
    // }

    // private void login(AuthenticationFlowContext context, String userAuthenticationCertificate, String applicantData, String sign) {
    //     String thisMethodName = new Object(){}.getClass()
    //                                     .getEnclosingMethod()
    //                                     .getName();
    //     UserModel user;
    //     try {
    //         if (!verifyCertificateAndSignature(context, userAuthenticationCertificate, sign)) {
    //             context.attempted();
    //             return;
    //         } 
    //         JsonNode responseData = null;
    //         try {
    //             // POST用のデータを作成する
    //             responseData = postDataToPlatform(context, thisMethodName, "userAuthenticationCertificate", userAuthenticationCertificate, applicantData, sign);
    //         } 
    //         catch (Exception e) {
    //             e.printStackTrace();
    //             context.form().setError(INTERNAL_SERVER_ERROR_MESSAGE, "");
    //             context.failure(AuthenticationFlowError.INTERNAL_ERROR,
    //                             context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
    //             return;
    //         }

    //         createErrorResponseIfNeeded(context, thisMethodName);
    //         if (!Objects.isNull(context.getAuthenticationSession().getAuthNote("immediateSendResponse"))) {
    //             context.getAuthenticationSession().removeAuthNote("immediateSendResponse");
    //             return;
    //         }

    //         ensureResponseData(context, responseData);
    //         if (!Objects.isNull(context.getAuthenticationSession().getAuthNote("immediateSendResponse"))) {
    //             context.getAuthenticationSession().removeAuthNote("immediateSendResponse");
    //             return;
    //         }

    //         String strUniqueId = responseData.get("identityInfo").get("uniqueId").toString().replace("\"", "");
    //         user = getUserIdentityToModelMapper("uniqueId")
    //                    .find(context, strUniqueId);
    //         context.setUser(user);

    //         if (context.getUser() != null) {
    //             context.success();
    //             return;
    //         }

    //         // 認証フローを初期化する
    //         restartAuthentication(context, "registration");
    //         return;
    //     }
    //     catch (ModelDuplicateException e) {
    //         logger.modelDuplicateException(e);
    //         context.attempted();
    //         return;
    //     }
    //     catch (Exception e) {
    //         e.printStackTrace();
    //         context.attempted();
    //         return;
    //     }
    // }

    // private void registration(AuthenticationFlowContext context, String encryptedDigitalSignatureCertificate, String applicantData, String sign) {
    //     String thisMethodName = new Object(){}.getClass()
    //                                     .getEnclosingMethod()
    //                                     .getName();
    //     UserModel user;
    //     try {
    //         if (!verifyCertificateAndSignature(context, encryptedDigitalSignatureCertificate, sign)) {
    //             context.attempted();
    //             return;
    //         } 
    //         JsonNode responseData = null;
    //         try {
    //             // POST用のデータを作成する
    //             responseData = postDataToPlatform(context, thisMethodName, "encryptedDigitalSignatureCertificate", encryptedDigitalSignatureCertificate, applicantData, sign);
    //         } 
    //         catch (Exception e) {
    //             e.printStackTrace();
    //             context.attempted();
    //             return;
    //         }
    //         
    //         createErrorResponseIfNeeded(context, thisMethodName);
    //         if (!Objects.isNull(context.getAuthenticationSession().getAuthNote("immediateSendResponse"))) {
    //             context.getAuthenticationSession().removeAuthNote("immediateSendResponse");
    //             return;
    //         }

    //         ensureResponseData(context, responseData);
    //         if (!Objects.isNull(context.getAuthenticationSession().getAuthNote("immediateSendResponse"))) {
    //             context.getAuthenticationSession().removeAuthNote("immediateSendResponse");
    //             return;
    //         }

    //         String strUniqueId = responseData.get("identityInfo").get("uniqueId").toString().replace("\"", "");
    //         user = getUserIdentityToModelMapper("uniqueId")
    //                    .find(context, strUniqueId);
    //         context.setUser(user);

    //         if (context.getUser() == null) {
    //             user = context.getSession().users().addUser(context.getRealm(), strUniqueId);
    //             // ユーザー属性を設定する
    //             String[] attributeName = {
    //                 "uniqueId",
    //                 "name"
    //             };
    //             for (String key : attributeName) {
    //                 user.setSingleAttribute(key, responseData.get("identityInfo").get(key).toString().replace("\"", "")); 
    //             }
    //             user.setSingleAttribute("gender_code", responseData.get("identityInfo").get("gender").toString().replace("\"", "")); 
    //             user.setSingleAttribute("user_address", responseData.get("identityInfo").get("address").toString().replace("\"", "")); 
    //             user.setSingleAttribute("birth_date", responseData.get("identityInfo").get("dateOfBirth").toString().replace("\"", "")); 
    //             user.setEnabled(true);
    //             context.setUser(user);
    //             context.success();
    //             return;
    //         }
    //         
    //         // 認証フローを初期化する
    //         restartAuthentication(context, "login");
    //         return;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return;
    //     }
    // }

    // private void replacement(AuthenticationFlowContext context, String encryptedDigitalSignatureCertificate, String applicantData, String sign) {
    //     String thisMethodName = new Object(){}.getClass()
    //                                     .getEnclosingMethod()
    //                                     .getName();
    //     UserModel user;
    //     try {
    //         if (!verifyCertificateAndSignature(context, encryptedDigitalSignatureCertificate, sign)) {
    //             context.attempted();
    //             return;
    //         } 
    //         JsonNode responseData = null;
    //         try {
    //             // POST用のデータを作成する
    //             responseData = postDataToPlatform(context, thisMethodName, "encryptedDigitalSignatureCertificate", encryptedDigitalSignatureCertificate, applicantData, sign);
    //         } 
    //         catch (Exception e) {
    //             e.printStackTrace();
    //             context.attempted();
    //             return;
    //         }
    //         
    //         createErrorResponseIfNeeded(context, thisMethodName);
    //         if (!Objects.isNull(context.getAuthenticationSession().getAuthNote("immediateSendResponse"))) {
    //             context.getAuthenticationSession().removeAuthNote("immediateSendResponse");
    //             return;
    //         }

    //         ensureResponseData(context, responseData);
    //         if (!Objects.isNull(context.getAuthenticationSession().getAuthNote("immediateSendResponse"))) {
    //             context.getAuthenticationSession().removeAuthNote("immediateSendResponse");
    //             return;
    //         }

    //         String strUniqueId = responseData.get("identityInfo").get("uniqueId").toString().replace("\"", "");
    //         user = getUserIdentityToModelMapper("uniqueId")
    //                    .find(context, strUniqueId);
    //         context.setUser(user);

    //         // ユーザー属性を変更する
    //         if (context.getUser() != null) {
    //             context.getUser().setSingleAttribute("name", responseData.get("identityInfo").get("name").toString().replace("\"", ""));
    //             context.getUser().setSingleAttribute("gender_code", responseData.get("identityInfo").get("gender").toString().replace("\"", ""));
    //             context.getUser().setSingleAttribute("user_address", responseData.get("identityInfo").get("address").toString().replace("\"", ""));
    //             context.getUser().setSingleAttribute("birth_date", responseData.get("identityInfo").get("dateOfBirth").toString().replace("\"", ""));
    //             context.success();
    //             return;
    //         }

    //         // 認証フローを初期化する
    //         restartAuthentication(context, "registration");
    //         return;
    //     }
    //     catch (ModelDuplicateException e) {
    //         logger.modelDuplicateException(e);
    //         context.attempted();
    //         return;
    //     }
    //     catch (Exception e) {
    //         e.printStackTrace();
    //         context.attempted();
    //         return;
    //     }
    // }

    // /*
    //  *  HTTP GET method  --> authenticate()
    //  *      公開鍵の所有権は、秘密鍵で署名された文字列をその公開鍵で検証できるかどうかで判断する
    //  *      署名対象nonce文字列を用意する
    //  *      nonce文字列を含めたネイティブアプリのリダイレクトをリターンする
    //  */
    // @Override
    // public void authenticate(AuthenticationFlowContext context) {
    //     MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();

    //     String loginHint = context.getAuthenticationSession()
    //                             .getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

    //     LoginFormsProvider form = context.form();
    //     form.setAttribute(X509_FILE_UPLOAD_ENABLE, true);
    //     try {
    //         form.setAttribute(
    //             "debug",
    //             context.getAuthenticatorConfig().getConfig()
    //                 .get("x509-relay-auth.debug-mode")
    //                 .toString()
    //                 .toLowerCase()
    //             );
    //     } catch (java.lang.NullPointerException e) {
    //             form.setAttribute("debug", "false");
    //     }

    //     form.setAttribute(
    //     "androidAppUri",
    //         context.getAuthenticatorConfig()
    //             .getConfig()
    //             .get("x509-relay-auth.android-app-uri")
    //     );
    //     form.setAttribute(
    //     "iosAppUri",
    //         context.getAuthenticatorConfig()
    //             .getConfig()
    //             .get("x509-relay-auth.ios-app-uri")
    //     );
    //     form.setAttribute(
    //     "otherAppUri",
    //         context.getAuthenticatorConfig()
    //             .getConfig()
    //             .get("x509-relay-auth.app-uri")
    //     );
    //     String nonce = UUID.randomUUID().toString();
    //     context.getAuthenticationSession().setAuthNote("nonce", nonce);
    //     form.setAttribute("nonce", nonce);

    //     if (context.getUser() != null) {
    //         form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
    //         form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true);
    //         context.getAuthenticationSession().setAuthNote(
    //             USER_SET_BEFORE_USERNAME_PASSWORD_AUTH,
    //             "true"
    //         );
    //     } else {
    //         context.getAuthenticationSession()
    //             .removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
    //     }

    //     if (formData.containsKey("cancel")) {
    //         context.cancelLogin();
    //         return;
    //     }
    //     // initialView（初期表示）を設定する
    //     String initialView = context.getAuthenticationSession().getAuthNote("initialView");
    //     context.getAuthenticationSession().setAuthNote("initialView", initialView);
    //     form.setAttribute("initialView", initialView);
    //     form.setAttribute("refreshUrl", context.getRefreshUrl(true).toString());
    //     Response challengeResponse = challenge(context, formData);
    //     context.challenge(challengeResponse);
    //     return;
    // }

    // @Override
    // protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
    //     LoginFormsProvider forms = context.form();

    //     if (formData.size() > 0) forms.setFormData(formData);

    //     return forms.createLoginUsernamePassword();
    // }

    // // プラットフォームへPOSTリクエストを送る
    // private JsonNode postDataToPlatform(AuthenticationFlowContext context, String thisMethodName, String certificateType, String certificate, String applicantData, String sign) {
    //     try {
    //         HttpPost httpPost = new HttpPost(context.getAuthenticatorConfig().getConfig().get("x509-relay-auth.certificate-validator-uri") + "/verify/" + thisMethodName);
    //         httpPost.setHeader("Content-type", "application/json");

    //         JSONObject requestInfo = new JSONObject();
    //         requestInfo.put("transactionId", UUID.randomUUID().toString());
    //         requestInfo.put("recipient", "JPKI");
    //         requestInfo.put("sender", "ID123");
    //         requestInfo.put("ts", new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS"));

    //         JSONObject requestParameters = new JSONObject();
    //         requestParameters.put("requestInfo", requestInfo);
    //         requestParameters.put(certificateType, certificate);
    //         requestParameters.put("applicantData", applicantData);
    //         requestParameters.put("sign", sign);

    //         String requestInfoString = requestParameters.toString();

    //         HttpEntity entity = new ByteArrayEntity(requestInfoString.getBytes("UTF-8"),  ContentType.APPLICATION_JSON);
    //         
    //         httpPost.setEntity(entity);

    //         // 証明書を送付する
    //         JsonNode responseData = null;
    //         //CloseableHttpClient httpClient = HttpClients.createDefault();
    //         // プラットフォームから指定時間内に応答が無ければタイムアウトと判断する。
    //         long connectTimeout = 5;
    //         RequestConfig config = RequestConfig.custom()
    //                                             .setConnectTimeout(connectTimeout, TimeUnit.SECONDS)
    //                                             .setConnectionRequestTimeout(connectTimeout, TimeUnit.SECONDS)
    //                                             .build();
    //         CloseableHttpClient httpClient = HttpClientBuilder.create()
    //                                                         .setDefaultRequestConfig(config)
    //                                                         .build();
    //         CloseableHttpResponse httpResponse = null;
    //         try {
    //             httpResponse = httpClient.execute(httpPost);
    //         }
    //         catch (HttpConnectTimeoutException e) {
    //             context.form().setStatus(Response.Status.INTERNAL_SERVER_ERROR);
    //             try {
    //                 consoleLogger.error("接続がタイムアウトしました。プラットフォーム URL: " + httpPost.getUri().toString());
    //             }
    //             catch (java.net.URISyntaxException uriException) {
    //                 consoleLogger.error(PLATFORM_URL_ERROR_MESSAGE);
    //             }
    //             e.printStackTrace();
    //         }
    //         catch (HttpTimeoutException e) {
    //             context.form().setStatus(Response.Status.INTERNAL_SERVER_ERROR);
    //             try {
    //                 consoleLogger.error("リクエストがタイムアウトしました。プラットフォーム URL: " + httpPost.getUri().toString());
    //             }
    //             catch (java.net.URISyntaxException uriException) {
    //                 consoleLogger.error(PLATFORM_URL_ERROR_MESSAGE);
    //             }
    //             e.printStackTrace();
    //         }

    //         final HttpEntity responseEntity = httpResponse.getEntity();
    //         if (responseEntity == null) {
    //             context.form().setStatus(Response.Status.INTERNAL_SERVER_ERROR);
    //             consoleLogger.error("プラットフォームのレスポンスが空でした。");
    //         }
    //         try (InputStream inputStream = responseEntity.getContent()) {
    //             ObjectMapper objectMapper = new ObjectMapper();

    //             String contentsBody = IOUtils.toString(inputStream, "UTF-8");

    //             responseData = objectMapper.readTree(contentsBody);
    //         } catch (com.fasterxml.jackson.core.JsonParseException e) {
    //             consoleLogger.error("プラットフォームのレスポンスをJSONへの変換にエラーが発生しました。");
    //         } catch (java.io.IOException e) {
    //             e.printStackTrace();
    //         }

    //         context.getAuthenticationSession().setAuthNote("platformStatusCode", String.valueOf(httpResponse.getCode()));

    //         return responseData;
    //     }
    //     catch (java.io.IOException e) {
    //         context.form().setStatus(Response.Status.INTERNAL_SERVER_ERROR);
    //         consoleLogger.error(INTERNAL_SERVER_ERROR_MESSAGE);
    //         e.printStackTrace();
    //         return null;
    //     }
    //     catch (Exception e) {
    //         context.form().setStatus(Response.Status.INTERNAL_SERVER_ERROR);
    //         consoleLogger.error(INTERNAL_SERVER_ERROR_MESSAGE);
    //         e.printStackTrace();
    //         throw e;
    //     }
    // }

    // public void restartAuthentication(AuthenticationFlowContext context, String initialView) {
    //     // authenticationメソッドを参考した際challengeする
    //     MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
    //     LoginFormsProvider form = context.form();
    //     form.setAttribute(X509_FILE_UPLOAD_ENABLE, true);
    //     String nonce = UUID.randomUUID().toString();
    //     context.getAuthenticationSession().setAuthNote("nonce", nonce);
    //     form.setAttribute("nonce", nonce);
    //     form.setAttribute(
    //         "androidAppUri",
    //             context.getAuthenticatorConfig()
    //                 .getConfig()
    //                 .get("x509-relay-auth.android-app-uri")
    //     );
    //     form.setAttribute(
    //         "iosAppUri",
    //             context.getAuthenticatorConfig()
    //                 .getConfig()
    //                 .get("x509-relay-auth.ios-app-uri")
    //     );
    //     form.setAttribute(
    //         "otherAppUri",
    //             context.getAuthenticatorConfig()
    //                 .getConfig()
    //                 .get("x509-relay-auth.app-uri")
    //     );
    //     context.getAuthenticationSession()
    //         .removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
    //     try {
    //         form.setAttribute(
    //             "debug",
    //             context.getAuthenticatorConfig().getConfig()
    //                 .get("x509-relay-auth.debug-mode")
    //                 .toString()
    //                 .toLowerCase()
    //             );
    //     } catch (java.lang.NullPointerException e) {
    //             form.setAttribute("debug", "false");
    //     }
    //     // initialView（初期表示）をテンプレート変数に入れる
    //     context.getAuthenticationSession().setAuthNote("initialView", initialView);
    //     form.setAttribute("initialView", initialView);
    //     if (initialView == "registration") {
    //         form.setStatus(Response.Status.NOT_FOUND);
    //     } else {
    //         form.setStatus(Response.Status.CONFLICT);
    //     }
    //     form.setAttribute("refreshUrl", context.getRefreshUrl(true).toString());

    //     Response challengeResponse = challenge(context, formData);
    //     context.challenge(challengeResponse);
    // }

    // // プラットフォームの応答がエラーの場合、エラー画面を表示または認証、登録、置換の処理へ切り替える。
    // private void createErrorResponseIfNeeded(AuthenticationFlowContext context, String actionMode) {
    //     int platformStatusCode = Integer.parseInt(context.getAuthenticationSession().getAuthNote("platformStatusCode"));
    //     Response.Status platformStatus = Response.Status.fromStatusCode(platformStatusCode);

    //     switch (platformStatusCode) {
    //         case 200:
    //             return;
    //         case 400:
    //             // 不正なリクエスト
    //             consoleLogger.error("リクエストに問題が発生しました。");
    //             initializePage(context, actionMode);
    //             context.challenge(createPage(context, platformStatus));
    //             return;
    //         case 500:
    //             consoleLogger.error(INTERNAL_SERVER_ERROR_MESSAGE);
    //             context.form().setError(INTERNAL_SERVER_ERROR_MESSAGE, "");
    //             context.failure(AuthenticationFlowError.INTERNAL_ERROR,
    //                             context.form().createErrorPage(platformStatus));
    //             context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //             return;
    //         case 503:
    //             consoleLogger.error(SERVICE_UNAVAILABLE_MESSAGE);
    //             context.form().setError(SERVICE_UNAVAILABLE_MESSAGE, "");
    //             context.failure(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR,
    //                             context.form().createErrorPage(platformStatus));
    //             context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //             return;
    //     }

    //     switch (actionMode) {
    //         case "login":
    //             switch (platformStatusCode) {
    //                 case 401:
    //                     // 検証処理が失敗、または利用者証明用電子証明書が失効している場合
    //                     consoleLogger.error(UNAUTHORIZED_MESSAGE);
    //                     context.form().setError(UNAUTHORIZED_MESSAGE, "");
    //                     context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
    //                                     context.form().createErrorPage(platformStatus));
    //                     context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //                     return;
    //                 case 404:
    //                     // ユーザー未登録の場合
    //                     // 登録画面を表示する。
    //                     consoleLogger.error("ユーザーが未登録です。");
    //                     initializePage(context, "registration");
    //                     context.challenge(createPage(context, platformStatus));
    //                     context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //                     return;
    //                 case 410:
    //                     // プラットフォームが保持している署名用電子証明書が失効している場合
    //                     // 認証フローセッションを継続する。
    //                     consoleLogger.error("署名用電子証明書が失効しています。");
    //                     initializePage(context, actionMode);
    //                     context.challenge(createPage(context, platformStatus));
    //                     context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //                     return;
    //             }
    //         case "registration":
    //             switch (platformStatusCode) {
    //                 case 401:
    //                     // 検証処理が失敗、または署名用電子証明書が失効している場合
    //                     consoleLogger.error(UNAUTHORIZED_MESSAGE);
    //                     context.form().setError(UNAUTHORIZED_MESSAGE, "");
    //                     context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
    //                                     context.form().createErrorPage(platformStatus));
    //                     context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //                     return;
    //                 case 409:
    //                     // ユーザー重複登録
    //                     // 認証画面を表示する。
    //                     consoleLogger.error("ユーザーが既に登録済みです。");
    //                     initializePage(context, "login");
    //                     context.challenge(createPage(context, platformStatus));
    //                     context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //                     return;
    //             }
    //         case "replacement":
    //             switch (platformStatusCode) {
    //                 case 401:
    //                     // 検証処理が失敗した場合
    //                     consoleLogger.error(UNAUTHORIZED_MESSAGE);
    //                     context.form().setError(UNAUTHORIZED_MESSAGE, "");
    //                     context.failure(AuthenticationFlowError.INVALID_CREDENTIALS,
    //                                     context.form().createErrorPage(platformStatus));
    //                     context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //                     return;
    //                 case 404:
    //                     // 旧シリアル番号を取得できなかった場合
    //                     // 認証画面を表示する。
    //                     consoleLogger.error("ユーザーが未登録です。");
    //                     initializePage(context, "registration");
    //                     context.challenge(createPage(context, platformStatus));
    //                     context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //                     return;
    //             }
    //     }
    // }

    // // 認証画面を表示する初期化処理
    // private void initializePage(AuthenticationFlowContext context, String initialView) {
    //     MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
    //     LoginFormsProvider form = context.form();
    //     form.setAttribute(X509_FILE_UPLOAD_ENABLE, true);
    //     String nonce = UUID.randomUUID().toString();
    //     context.getAuthenticationSession().setAuthNote("nonce", nonce);
    //     form.setAttribute("nonce", nonce);
    //     form.setAttribute(
    //         "androidAppUri",
    //             context.getAuthenticatorConfig()
    //                 .getConfig()
    //                 .get("x509-relay-auth.android-app-uri")
    //     );
    //     form.setAttribute(
    //         "iosAppUri",
    //             context.getAuthenticatorConfig()
    //                 .getConfig()
    //                 .get("x509-relay-auth.ios-app-uri")
    //     );
    //     form.setAttribute(
    //         "otherAppUri",
    //             context.getAuthenticatorConfig()
    //                 .getConfig()
    //                 .get("x509-relay-auth.app-uri")
    //     );
    //     try {
    //         form.setAttribute(
    //             "debug",
    //             context.getAuthenticatorConfig().getConfig()
    //                 .get("x509-relay-auth.debug-mode")
    //                 .toString()
    //                 .toLowerCase()
    //             );
    //     } catch (java.lang.NullPointerException e) {
    //             form.setAttribute("debug", "false");
    //     }
    //     // initialView（初期表示）をテンプレート変数に入れる
    //     context.getAuthenticationSession().setAuthNote("initialView", initialView);
    //     form.setAttribute("initialView", initialView);
    //     form.setAttribute("refreshUrl", context.getRefreshUrl(true).toString());
    // }

    // // 任意のステータスコードで認証画面を生成する。
    // private Response createPage(AuthenticationFlowContext context, Response.Status status) {
    //    Response templateResponse = challenge(context, new MultivaluedMapImpl<>());
    //    return Response.fromResponse(templateResponse)
    //                   .status(status)
    //                   .build();
    // }

    // // JSONオブジェクトからidentityInfo.uniqueIdを取得できる状態かチェックし、
    // // そうでない場合はエラー画面を出力する。
    // private void ensureResponseData(AuthenticationFlowContext context, JsonNode responseData) {
    //     if (Objects.isNull(responseData) ||
    //         Objects.isNull(responseData.get("identityInfo")) ||
    //         Objects.isNull(responseData.get("identityInfo").get("uniqueId"))) {
    //         consoleLogger.error("プラットフォームのレスポンスにuniqueIdがありませんでした。");
    //         context.form().setError(INTERNAL_SERVER_ERROR_MESSAGE, "");
    //         context.failure(AuthenticationFlowError.INTERNAL_ERROR,
    //                         context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
    //         context.getAuthenticationSession().setAuthNote("immediateSendResponse", "true");
    //         return;
    //     }
    // }

    /**
     * 共通で使うテンプレート変数をユーザーに表示する画面のテンプレートへ設定します。
     *
     * Nonceを生成します。
     *
     * @param context 認証フローのコンテキスト
     */
    protected void setLoginFormAttributes(AuthenticationFlowContext context) {
        setLoginFormAttributes(context, true);
    }

    /**
     * 共通で使うテンプレート変数をユーザーに表示する画面のテンプレートへ設定します。
     *
     * Nonceの再生成をするか選択することができます。
     *
     * @param context 認証フローのコンテキスト
     * @param createNonceFlag Nonce文字列生成の有効化フラグ
     */
    protected void setLoginFormAttributes(AuthenticationFlowContext context, boolean createNonceFlag) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        LoginFormsProvider form = context.form();
        form.setAttribute(X509_FILE_UPLOAD_ENABLE, true); // 互換性維持のパラメータ

        if (createNonceFlag) {
            String nonce = createNonce();
            context.getAuthenticationSession().setAuthNote("nonce", nonce);
            form.setAttribute("nonce", nonce);
        }

        Map<String, String> spiConfig = new LinkedHashMap<>();

        String androidConfigName = SpiConfigProperty.RunUriOfAndroidApplication.CONFIG.getName();
        spiConfig.put("androidAppUri", CurrentConfig.getValue(context, androidConfigName));

        /*
         * [NOTE] 内部クラス名の単語 Ios は意図したものです。
         *        詳細は SpiConfigProperty.RunUriOfIosApplication クラスにコメントアウトを参照してください。
         */
        String iosConfigName = SpiConfigProperty.RunUriOfIosApplication.CONFIG.getName();
        form.setAttribute("iosAppUri", CurrentConfig.getValue(context, androidConfigName));

        String otherAppUri = SpiConfigProperty.InstallationUriOfSmartPhoneApplication.CONFIG.getName();
        form.setAttribute("otherAppUri", CurrentConfig.getValue(context, otherAppUri));

        String debugMode = SpiConfigProperty.DebugMode.CONFIG.getName();
        String debugModeValue = CurrentConfig.getValue(context, debugMode).toLowerCase();
        form.setAttribute("debug", debugModeValue);
    }

    /**
     * Nonceを生成します。
     *
     * @return nonce UUIDの文字列
     */
    private String createNonce() {
        return java.util.UUID.randomUUID().toString();
    }
}
