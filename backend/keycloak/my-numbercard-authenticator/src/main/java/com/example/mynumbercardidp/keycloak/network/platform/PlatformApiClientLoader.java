package com.example.mynumbercardidp.keycloak.network.platform;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.MultivaluedMap;

/**
 * 個人番号カードの公的個人認証部分を受け付けるプラットフォームと通信するためのクラスインスタンスを作成します。
 */
public class PlatformApiClientLoader implements PlatformApiClientLoaderImpl {
     private static Logger consoleLogger = Logger.getLogger(PlatformApiClientLoader.class);
     private PlatformApiClientImpl platformClass;

     public PlatformApiClientLoader() {}

     @Override
     public PlatformApiClientImpl load(String platformClassFqdn, AuthenticationFlowContext context, String apiRootUri, String idpSender) {
         try {
             AbstractPlatformApiClient platform = (AbstractPlatformApiClient) Class.forName(platformClassFqdn)
                 .getDeclaredConstructor()
                 .newInstance();

             MultivaluedMap formData = extractFormData(context);
             formData.forEach((k, v) -> consoleLogger.debug("Key " + k + " -> " + v));
             platform.init(apiRootUri, formData, idpSender); 
             return platform;
         } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException |
                  InstantiationException | IllegalAccessException | InvocationTargetException e) {
             throw new IllegalArgumentException(e);
         }
     }

    /**
     * 認証フローのコンテキストからHTMLフォームデータを抽出します。
     *
     * @param context 認証フローのコンテキスト
     * @return ユーザーが送ったHTMLフォームデータのマップ
     */
    private static MultivaluedMap<String, String> extractFormData(AuthenticationFlowContext context) {
         return context.getHttpRequest().getDecodedFormParameters();
    }
}
