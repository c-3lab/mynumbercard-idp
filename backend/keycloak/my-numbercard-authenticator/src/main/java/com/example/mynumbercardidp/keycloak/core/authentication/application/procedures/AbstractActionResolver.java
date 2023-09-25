package com.example.mynumbercardidp.keycloak.core.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientResolver;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;
import com.example.mynumbercardidp.keycloak.util.StringUtil;
import org.keycloak.authentication.AuthenticationFlowContext;

/**
 * ユーザーからKeycloakへのHTTPリクエストを元に実行する処理を呼び出す抽象クラスです。
 */
public abstract class AbstractActionResolver implements ApplicationResolverInterface {

    /** プラットフォームAPIクライアント検索クラスのインスタンス */
    private PlatformApiClientResolver platformResolver = new PlatformApiClientResolver();

    protected AbstractActionResolver() {
    }

    /**
     * ユーザーからKeycloakへ送られたHTTPリクエストを元に、ユーザーが希望する処理を呼び出します。
     *
     * @param context 認証フローのコンテキスト
     */
    @Override
    public abstract void executeUserAction(final AuthenticationFlowContext context);

    /**
     * プラットフォームAPIクライアントのインスタンスを作成します。
     *
     * @param context 認証フローのコンテキスト
     * @exception IllegalStateException プラットフォームAPIのURLが空値の場合
     */
    protected PlatformApiClientInterface createPlatform(final AuthenticationFlowContext context) {
        String platformApiClassFqdn = CurrentConfig.getValue(context,
                SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName());
        if (StringUtil.isEmpty(platformApiClassFqdn)) {
            throw new IllegalStateException(SpiConfigProperty.PlatformApiClientClassFqdn.LABEL + " is empty.");
        }
        String platformRootApiUri = CurrentConfig.getValue(context,
                SpiConfigProperty.CertificateValidatorRootUri.CONFIG.getName());
        String idpSender = CurrentConfig.getValue(context, SpiConfigProperty.PlatformApiIdpSender.CONFIG.getName());
        return this.platformResolver.createPlatform(platformApiClassFqdn, context, platformRootApiUri, idpSender);
    }
}
