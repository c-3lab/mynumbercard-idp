package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import java.util.Objects;

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
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;

public class ReplacementActionAdapter {
    private final Logger CONSOLE_LOGGER = Logger.getLogger(ReplacementActionAdapter.class);
    private AuthenticationFlowContext authContext;

    public ReplacementActionAdapter(KeycloakSession session) {
        this.authContext = createAuthenticationFlowContext(Objects.requireNonNull(session));
    }

    public Response replace(MultivaluedMap<String, String> formData) throws Exception {
        String platformApiClientClassFqdn = CurrentConfig.getValue(this.authContext,
                SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName());
        this.CONSOLE_LOGGER.debugf("platformApiClientClassFqdn: %s", platformApiClientClassFqdn);
        PlatformApiClientInterface platform = createPlatform(
                platformApiClientClassFqdn,
                formData,
                this.authContext,
                CurrentConfig.getValue(this.authContext,
                        SpiConfigProperty.CertificateValidatorRootUri.CONFIG.getName()),
                CurrentConfig.getValue(this.authContext, SpiConfigProperty.PlatformApiIdpSender.CONFIG.getName()));
        platform.setContextForDataManager(this.authContext);
        return new ReplacementAction().replace(this.authContext, platform);
    }

    private PlatformApiClientInterface createPlatform(final String platformClassFqdn,
            final MultivaluedMap<String, String> formData, final AuthenticationFlowContext context,
            final String apiRootUri, String idpSender) throws Exception {
        PlatformApiClientInterface platform = (PlatformApiClientInterface) Class.forName(platformClassFqdn)
                .getDeclaredConstructor()
                .newInstance();
        platform.init(apiRootUri, formData, idpSender);
        return platform;
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
}
