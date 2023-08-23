package com.example.mynumbercardidp.keycloak.core.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientImpl;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientResolver;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;

import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.Response;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出す抽象クラスです。
 *
 * このクラスを継承したサブクラスは、ユーザーの希望する処理が定義されているクラスのパッケージ名を定義する必要があります。
 */
public abstract class AbstractActionResolver implements ApplicationResolverImpl {

    /** プラットフォームAPIクライアント検索クラスのインスタンス */
    private PlatformApiClientResolver platformResolver = new PlatformApiClientResolver();
    /** ユーザーの希望する処理が定義されているクラスが存在するパッケージ名 */
    private String userActionPackageName;
    /** ユーザーの希望する処理が定義されているクラス名の接尾文字列 */
    private String userActionClassNameSuffix = "Action";
    /** ユーザーの希望する処理が定義されているクラスのインスタンス */
    private ApplicationProcedure action;

    protected AbstractActionResolver(final String packageName) {
        this.userActionPackageName = packageName;
    }

    protected AbstractActionResolver(final String packageName, final String actionSuffix) {
        this.userActionPackageName = packageName;
        this.userActionClassNameSuffix = actionSuffix;
    }

    /**
     * ユーザーからKeycloakへ送られたHTTPリクエストを元に、実行する各処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     */
    @Override
    public final void action(final AuthenticationFlowContext context) {
        PlatformApiClientImpl platform = loadPlatform(context);
        preAction(context, platform);
        onAction(context, platform);
        postAction(context, platform);
    }

    /**
     * ユーザーからKeycloakへ送られたHTTPリクエストを元に、実行する事前処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    protected void preAction(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        this.action = resolveActionClass(context, platform);
        this.action.preAction(context, platform);
    } 

    /**
     * ユーザーからKeycloakへ送られたHTTPリクエストを元に、実行する処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    protected void onAction(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        this.action.onAction(context, platform);
    } 

    /**
     * ユーザーからKeycloakへ送られたHTTPリクエストを元に、実行する事後処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    protected void postAction(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        this.action.postAction(context, platform);
    }

    protected String extractActionMode(final PlatformApiClientImpl platform) {
        return StringUtil.toFirstUpperCase(platform.getUserActionMode());
    }

    /**
     * プラットフォームAPIクライアントのインスタンスを読み込みます。
     *
     * @param context 認証フローのコンテキスト
     * @exception IllegalStateException プラットフォームAPIのURLが空値の場合
     */
    private PlatformApiClientImpl loadPlatform(final AuthenticationFlowContext context) {
        String platformApiClassFqdn = CurrentConfig.getValue(context, SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName());
        if (StringUtil.isEmpty(platformApiClassFqdn)) {
            throw new IllegalStateException(SpiConfigProperty.PlatformApiClientClassFqdn.LABEL + " is empty.");
        }
        String platformRootApiUri = CurrentConfig.getValue(context, SpiConfigProperty.CertificateValidatorRootUri.CONFIG.getName());
        String idpSender = CurrentConfig.getValue(context, SpiConfigProperty.PlatformApiIdpSender.CONFIG.getName());
        return this.platformResolver.load(platformApiClassFqdn, context, platformRootApiUri, idpSender);
    }

    /**
     * ユーザーからKeycloakへ送られたHTTPリクエストを元に、実行する処理のインスタンスを生成します。
     *
     * @param context 認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    private ApplicationProcedure resolveActionClass(final AuthenticationFlowContext context, final PlatformApiClientImpl platform) {
        String actionClass = getActionClass(platform);
        try {
            return (ApplicationProcedure) Class.forName(actionClass)
                .getDeclaredConstructor()
                .newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getActionClass(final PlatformApiClientImpl platform) {
        return this.userActionPackageName + "." +  extractActionMode(platform) + this.userActionClassNameSuffix;
    }
}
