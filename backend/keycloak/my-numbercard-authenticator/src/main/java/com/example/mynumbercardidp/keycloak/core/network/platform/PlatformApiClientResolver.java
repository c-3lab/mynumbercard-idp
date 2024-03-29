package com.example.mynumbercardidp.keycloak.core.network.platform;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.MultivaluedMap;

/**
 * 個人番号カードの公的個人認証部分を受け付けるプラットフォームと通信するためのクラスインスタンスを作成します。
 */
public class PlatformApiClientResolver implements PlatformApiClientResolveClassLoader {
    private static Logger consoleLogger = Logger.getLogger(PlatformApiClientResolver.class);

    @Override
    public PlatformApiClientInterface createPlatform(final String platformClassFqdn,
            final AuthenticationFlowContext context, final String apiRootUri, String idpSender) {
        try {
            PlatformApiClientInterface platform = (PlatformApiClientInterface) Class.forName(platformClassFqdn)
                    .getDeclaredConstructor()
                    .newInstance();

            MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
            formData.forEach((k, v) -> PlatformApiClientResolver.consoleLogger.debug("Key " + k + " -> " + v));
            platform.init(apiRootUri, formData, idpSender);
            return platform;
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
