package com.example.mynumbercardidp.keycloak.network.platform;

import com.example.mynumbercardidp.keycloak.network.RequestBuilderImpl;
import org.keycloak.authentication.AuthenticationFlowContext;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 個人番号カードの公的個人認証部分を受け付けるプラットフォームと通信するためのクラスインスタンスを作成します。
 */
public class PlatformApiClientLoader implements PlatformApiClientLoaderImpl {
     private PlatformApiClient platformClass;

     public PlatformApiClientLoader() {}

     @Override
     public PlatformApiClient load(String platformClassFqdn, AuthenticationFlowContext context, String apiRootUri, String idpSender) throws ClassNotFoundException, ClassCastException, NoSuchMethodException, URISyntaxException, InstantiationException, IllegalAccessException, InvocationTargetException {
         
         AbstractPlatformApiClient platform = (AbstractPlatformApiClient) Class.forName(platformClassFqdn)
             .getDeclaredConstructor()
             .newInstance();
         platform.setApiRootUri(new URI(apiRootUri));
         platform.init(RequestBuilderImpl.extractFormData(context)); 
         return platform;
     }
}
