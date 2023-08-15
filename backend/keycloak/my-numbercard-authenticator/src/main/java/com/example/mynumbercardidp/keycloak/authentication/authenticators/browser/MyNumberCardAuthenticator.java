package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ActionHandler;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClientImpl;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClientLoader;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.services.ServicesLogger;
import org.keycloak.sessions.CommonClientSessionModel.ExecutionStatus;

import java.util.Objects;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import javax.ws.rs.core.MultivaluedMap;

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
        String platformApiClassFqdn = CurrentConfig.getValue(context, SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName());
        if (isStringEmpty(platformApiClassFqdn)) {
            consoleLogger.error(SpiConfigProperty.PlatformApiClientClassFqdn.LABEL + " is empty.");
            ResponseCreater.createInternalServerErrorPage(context, null, null);
            return;
        }

        String platformRootApiUri = CurrentConfig.getValue(context, SpiConfigProperty.CertificateValidatorRootUri.NAME);
        String idpSender = CurrentConfig.getValue(context, SpiConfigProperty.PlatformApiIdpSender.CONFIG.getName());
        PlatformApiClientLoader platformLoader = new PlatformApiClientLoader();
        String verifyNonce = context.getAuthenticationSession().getAuthNote("nonce");
        context.getAuthenticationSession().setAuthNote("verifyNonce", verifyNonce);
        PlatformApiClientImpl platform = platformLoader.load(platformApiClassFqdn, context, platformRootApiUri, idpSender);
        /*
         * 認証を試行するユーザーが希望している動作で処理をする。
         *
         * ActionHandlerクラスが持つメソッドの戻り値はvoid型かつ、
         * publicアクセス修飾子のメソッドはexecuteのみであるため、インスタンスを変数へ格納しない。
         */
        setLoginFormAttributes(context);
        new ActionHandler().execute(context, platform);

        /*
         * [HACK] 認証試行ユーザーのセッション情報から認証フローの結果を取得します。
         *
         * 認証フローの結果が存在している場合はAuthenticatorの処理を終了し、ユーザーへHTTPレスポンスを返します。
         * そうでない場合は、ActionHandlerが呼び出したActionクラスに不備があります。
         */
        String authFlowResult = ResponseCreater.getFlowState(context);

        if (isStringEmpty(authFlowResult)) {
            /*
             * [TODO] Exceptionを返したときにユーザーへ内部エラーのレスポンス 500を返さない動作を確認した場合、
             *        Exceptionをスローしない処理へ修正する。
             */
            ResponseCreater.createInternalServerErrorPage(context, null, null);
            throw new IllegalStateException("Not found AuthFlowResult in auth note for authentication session.");
        }

        if (!(authFlowResult.equals(ExecutionStatus.FAILED.toString()) ||
                authFlowResult.equals(ExecutionStatus.CHALLENGED.toString()) ||
                authFlowResult.equals(ExecutionStatus.SUCCESS.toString()))) {
            return;
        }
    }

    /**
     * 個人番号カードの公的個人認証部分を送信するアプリが起動できるフォームを返します。
     *
     * @param context 認証フローのコンテキスト
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        setLoginFormAttributes(context);

        String initialView = context.getAuthenticationSession().getAuthNote("initialView");
        if (isStringEmpty(initialView)) {
            initialView = "";
        }
        javax.ws.rs.core.Response response = ResponseCreater.createChallengePage(context, initialView);
        ResponseCreater.setFlowStepChallenge(context, response);
    }

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

        String iosConfigName = SpiConfigProperty.RunUriOfiOSApplication.CONFIG.getName();
        form.setAttribute("iosAppUri", CurrentConfig.getValue(context, iosConfigName));

        String otherAppUri = SpiConfigProperty.InstallationUriOfSmartPhoneApplication.CONFIG.getName();
        form.setAttribute("otherAppUri", CurrentConfig.getValue(context, otherAppUri));

        String debugMode = SpiConfigProperty.DebugMode.CONFIG.getName();
        String debugModeValue = CurrentConfig.getValue(context, debugMode).toLowerCase();
        debugModeValue = isStringEmpty(debugModeValue) ? "false" : debugModeValue.toLowerCase();
        form.setAttribute("debug", debugModeValue);
    }

    /**
     * Nonceを生成します。
     *
     * @return nonce UUIDの文字列
     */
    protected String createNonce() {
        return UUID.randomUUID().toString();
    }

    /**
     * String型がNullまたは文字列の長さがゼロであるかを判定します。
     *
     * @param str Nullまたは長さがゼロであるか判定したい文字列
     * @return Nullまたは長さがゼロの場合はtrue、そうでない場合はfalse
     */
    protected boolean isStringEmpty(String str) {
        return Objects.isNull(str) || str.length() == 0;
    }
}
