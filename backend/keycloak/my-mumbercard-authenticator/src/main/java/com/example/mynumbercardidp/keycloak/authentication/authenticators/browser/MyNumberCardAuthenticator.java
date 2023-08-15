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
        PlatformApiClientImpl platform = null;
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
        return UUID.randomUUID().toString();
    }
}
