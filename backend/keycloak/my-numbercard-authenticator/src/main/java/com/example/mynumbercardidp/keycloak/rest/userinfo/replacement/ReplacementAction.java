package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import java.util.Objects;

import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.util.DefaultClientSessionContext;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.AbstractUserAction;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ResponseCreater;
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.ActionType;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformAuthenticationResponse;

public class ReplacementAction extends AbstractUserAction {
    public Response replace(AuthenticationFlowContext context, PlatformApiClientInterface platform) {
        try {
            if (!super.validateSignature(context, platform)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            platform.sendRequest();
            PlatformAuthenticationResponse response = (PlatformAuthenticationResponse) platform.getPlatformResponse();
            ReplacementAction.FlowTransition
                    .validatePlatformStatusCode(Response.Status.fromStatusCode(response.getHttpStatusCode()));

            String uniqueId = super.tryExtractUniqueId(response);
            UserModel user = super.findUser(context, uniqueId);
            if (Objects.isNull(user)) {
                ResponseCreater.sendChallengeResponse(context, ActionType.REGISTRATION.getName(),
                        Response.Status.NOT_FOUND);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            context.setUser(user);
            response.toUserModelAttributes(context.getUser());
            return createAuthorizationCodeResponse(context);
        } catch (ReplacementAction.PlatformResponseException e) {
            return e.getHttpResponse();
        }
    }

    private Response createAuthorizationCodeResponse(AuthenticationFlowContext authContext) {
        KeycloakSession session = authContext.getSession();
        UserSessionModel userSession = session.sessions().getUserSession(authContext.getRealm(),
                authContext.getAuthenticationSession().getAuthNote("userSessionId"));

        ClientModel client = Objects.requireNonNull(authContext.getAuthenticationSession().getClient());
        AuthenticatedClientSessionModel clientSession = userSession
                .getAuthenticatedClientSessionByClient(client.getId());
        ClientSessionContext clientSessionCtx = DefaultClientSessionContext
                .fromClientSessionScopeParameter(clientSession, session);

        ClientConnection connection = session.getContext().getConnection();
        EventBuilder event = new EventBuilder(session.getContext().getRealm(),
                session, connection);
        AccessToken clientAccessToken = new TokenManager().createClientAccessToken(session,
                authContext.getRealm(), client,
                authContext.getUser(), userSession, clientSessionCtx);
        authContext.getAuthenticationSession().setClientNote(OIDCLoginProtocol.STATE_PARAM, clientAccessToken.getId());
        authContext.getAuthenticationSession().setClientNote(OAuth2Constants.SCOPE,
                authContext.getAuthenticationSession().getAuthNote(OIDCLoginProtocol.SCOPE_PARAM));
        authContext.getAuthenticationSession().setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM,
                authContext.getAuthenticationSession().getRedirectUri());
        authContext.getAuthenticationSession().setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM,
                authContext.getAuthenticationSession().getAuthNote(
                        OIDCLoginProtocol.RESPONSE_TYPE_PARAM));

        OIDCLoginProtocol oidcLoginProtocol = new OIDCLoginProtocol(session, authContext.getRealm(),
                session.getContext().getUri().getDelegate(), session.getContext().getHttpRequest().getHttpHeaders(),
                event);
        return oidcLoginProtocol.authenticated(authContext.getAuthenticationSession(), userSession, clientSessionCtx);
    }

    private static class FlowTransition {
        private static Logger consoleLogger = Logger.getLogger(FlowTransition.class);

        private static void validatePlatformStatusCode(Response.Status status)
                throws PlatformResponseException {
            switch (status) {
                case OK:
                    return;
                case BAD_REQUEST:
                    throw new PlatformResponseException(Response.status(status).build());
                case INTERNAL_SERVER_ERROR:
                    // フォールスルー
                case SERVICE_UNAVAILABLE:
                    FlowTransition.consoleLogger.errorf("Platform response status: %s", status.getStatusCode());
                    FlowTransition.consoleLogger.error("Make sure the platform API server status is running.");
                    throw new PlatformResponseException(Response.status(status).build());
                case UNAUTHORIZED:
                    // フォールスルー
                case GONE:
                    throw new PlatformResponseException(Response.status(status).build());
                default:
                    throw new IllegalStateException(
                            "Received an invalid status code. Status " + status.getStatusCode() + " is the undefined "
                                    + ActionType.REPLACEMENT.getName() + " flow.");
            }
        }
    }

    private static class PlatformResponseException extends Exception {
        private Response httpResponse;

        private PlatformResponseException(Response response) {
            this.httpResponse = response;
        }

        private Response getHttpResponse() {
            return this.httpResponse;
        }
    }
}
