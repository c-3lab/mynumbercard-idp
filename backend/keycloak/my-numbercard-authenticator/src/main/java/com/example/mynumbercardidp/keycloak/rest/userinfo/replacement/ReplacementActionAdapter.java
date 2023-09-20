package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.MyNumberCardAuthenticatorFactory;
import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientResolver;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;

/**
 * 個人番号カードの公的個人認証部分を利用したプラットフォームからの応答を元にユーザー情報を更新する定義です。
 */
public class ReplacementActionAdapter {
    private PlatformApiClientResolveAdapter platformResolver = new PlatformApiClientResolveAdapter();
    private AuthenticationFlowContext authContext;
    private static Logger consoleLogger = Logger.getLogger(ReplacementActionAdapter.class);

    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からKeycloak内のユーザー情報を更新します。
     *
     * @param session
     */
    public Response replace(KeycloakSession session, MultivaluedMap<String, String> formData) {
        this.authContext = createAuthenticationFlowContext(session);
        String platformApiClientClassFqdn = CurrentConfig.getValue(this.authContext,
                SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName());
        ReplacementActionAdapter.consoleLogger.debugf("platformApiClientClassFqdn: %s", platformApiClientClassFqdn);
        PlatformApiClientInterface platform = this.platformResolver.createPlatform(
                platformApiClientClassFqdn,
                formData,
                this.authContext,
                CurrentConfig.getValue(this.authContext,
                        SpiConfigProperty.CertificateValidatorRootUri.CONFIG.getName()),
                CurrentConfig.getValue(this.authContext, SpiConfigProperty.PlatformApiIdpSender.CONFIG.getName()));
        platform.setContextForDataManager(this.authContext);

        ReplacementAction replacementAction = new ReplacementAction();
        return replacementAction.replace(this.authContext, platform);
    }

    private AuthenticationFlowContext createAuthenticationFlowContext(KeycloakSession session) {
        AuthenticationProcessor authenticationProcessor = new AuthenticationProcessor().setSession(session);
        authenticationProcessor.setRealm(session.getContext().getRealm());
        authenticationProcessor.setClient(session.getContext().getClient());
        authenticationProcessor.setBrowserFlow(true);
        authenticationProcessor.setRequest(session.getContext().getHttpRequest());
        authenticationProcessor.setAuthenticationSession(session.getContext().getAuthenticationSession());
        authenticationProcessor.setUriInfo(session.getContext().getUri());
        authenticationProcessor.setFlowPath(
                "/realms/" + session.getContext().getRealm().getName() + "/" + UserInfoReplacementProviderFactory.ID
                        + "/replace");

        AuthenticatorFactory authenticatorFactory = new MyNumberCardAuthenticatorFactory();
        Authenticator authenticator = authenticatorFactory.create(session);
        AuthenticationExecutionModel authenticationExecution = AuthenticationUtil
                        .findAuthenticationExecutionModel(session.getContext().getRealm(),
                                        session.getContext().getClient(), authenticatorFactory.getId())
                        .orElseThrow();
        authenticationProcessor.setFlowId(authenticationExecution.getFlowId());
        authenticationProcessor.getAuthenticationSession().setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM,
                session.getContext().getHttpRequest().getDecodedFormParameters().getFirst("response_type"));
        authenticationProcessor.getAuthenticationSession().setClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM,
                "query");
        return authenticationProcessor.createAuthenticatorContext(authenticationExecution, authenticator, null);
    }

    private static class PlatformApiClientResolveAdapter extends PlatformApiClientResolver {
        private PlatformApiClientInterface createPlatform(final String platformClassFqdn,
                final MultivaluedMap<String, String> formData, final AuthenticationFlowContext context,
                final String apiRootUri, String idpSender) {
            try {
                PlatformApiClientInterface platform = (PlatformApiClientInterface) Class.forName(platformClassFqdn)
                        .getDeclaredConstructor()
                        .newInstance();
                platform.init(apiRootUri, formData, idpSender);
                return platform;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
