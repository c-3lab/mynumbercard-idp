package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import java.util.Objects;
import java.util.Optional;

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
import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.CommonFlowTransition;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.network.platform.PlatformAuthenticationResponse;

/**
 * 個人番号カードの公的個人認証部分を利用したプラットフォームからの応答を元にユーザー情報を更新する定義です。
 */
public class ReplacementAction extends AbstractUserAction {
    private static Logger consoleLogger = Logger.getLogger(ReplacementAction.class);
    private FlowTransition flowTransition = new FlowTransition();

    /**
     * 公的個人認証部分をプラットフォームへ送信し、その応答からKeycloak内のユーザー情報を更新します。
     *
     * @param context  認証フローのコンテキスト
     * @param platform プラットフォーム APIクライアントのインスタンス
     */
    public Response replace(AuthenticationFlowContext context, PlatformApiClientInterface platform) {
        if (!super.validateSignature(context, platform)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        platform.sendRequest();
        PlatformAuthenticationResponse response = (PlatformAuthenticationResponse) platform.getPlatformResponse();
        if (!this.flowTransition.canExecuteReplacement(context,
                Response.Status.fromStatusCode(response.getHttpStatusCode()))) {
            return this.flowTransition.getHttpResponse().orElseThrow(() -> {
                return new IllegalStateException();
            });
        }

        String uniqueId = super.tryExtractUniqueId(response);
        UserModel user = super.findUser(context, uniqueId);
        if (Objects.isNull(user)) {
            ResponseCreater.sendChallengeResponse(context, ActionType.REGISTRATION.getName(),
                    Response.Status.NOT_FOUND);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return createAuthorizationCodeResponse(context);
    }

    private Response createAuthorizationCodeResponse(AuthenticationFlowContext authContext) {
        KeycloakSession session = authContext.getSession();
        UserSessionModel userSession = session.sessions().getUserSession(authContext.getRealm(),
                authContext.getAuthenticationSession().getAuthNote("userSessionId"));

        ClientModel client = Objects.requireNonNull(authContext.getAuthenticationSession().getClient());
        consoleLogger.debugf("Client id: %s", client.getId());
        userSession.getAuthenticatedClientSessions().forEach((key, value) -> {
            consoleLogger.debugf("key %s -> AuthenticatedClientSessionModel ID: %s", key, value.getId());
        });
        AuthenticatedClientSessionModel clientSession = userSession
                .getAuthenticatedClientSessionByClient(client.getId());
        ClientSessionContext clientSessionCtx = DefaultClientSessionContext
                .fromClientSessionScopeParameter(clientSession, session);

        ClientConnection connection = session.getContext().getConnection();
        EventBuilder event = new EventBuilder(session.getContext().getRealm(),
                session, connection);
        TokenManager tokenManager = new TokenManager();
        AccessToken clientAccessToken = tokenManager.createClientAccessToken(session,
                authContext.getRealm(), client,
                authContext.getUser(), userSession, clientSessionCtx);
        authContext.getAuthenticationSession().setClientNote(OIDCLoginProtocol.STATE_PARAM, clientAccessToken.getId());
        authContext.getAuthenticationSession().setClientNote(OAuth2Constants.SCOPE,
                authContext.getAuthenticationSession().getAuthNote(OIDCLoginProtocol.SCOPE_PARAM));
        authContext.getAuthenticationSession().setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM,
                authContext.getAuthenticationSession().getRedirectUri());
        consoleLogger.debugf("Redirect URI: %s", authContext.getAuthenticationSession().getRedirectUri());
        authContext.getAuthenticationSession().setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM,
                authContext.getAuthenticationSession().getAuthNote(
                        OIDCLoginProtocol.RESPONSE_TYPE_PARAM));

        OIDCLoginProtocol oidcLoginProtocol = new OIDCLoginProtocol(session, authContext.getRealm(),
                session.getContext().getUri().getDelegate(), session.getContext().getHttpRequest().getHttpHeaders(),
                event);
        return oidcLoginProtocol.authenticated(authContext.getAuthenticationSession(), userSession, clientSessionCtx);
    }

    private static class FlowTransition {
        private static Logger consoleLogger = Logger.getLogger(CommonFlowTransition.class);
        private Response httpResponse;

        private Optional<Response> getHttpResponse() {
            return Optional.ofNullable(this.httpResponse);
        }

        private boolean canExecuteReplacement(final AuthenticationFlowContext context, final Response.Status status) {
            switch (status) {
                case OK:
                    return true;
                case BAD_REQUEST:
                    this.httpResponse = Response.status(Response.Status.BAD_REQUEST).build();
                    return false;
                case INTERNAL_SERVER_ERROR:
                    // フォールスルー
                case SERVICE_UNAVAILABLE:
                    FlowTransition.consoleLogger.error("Platform response status: " + status.getStatusCode());
                    FlowTransition.consoleLogger.error("Make sure the platform API server status is running.");
                    this.httpResponse = Response.status(status).build();
                    return false;
                case UNAUTHORIZED:
                    // フォールスルー
                case GONE:
                    this.httpResponse = Response.status(status).build();
                    return false;
                default:
                    throw new IllegalStateException(
                            "Received an invalid status code. Status " + status.getStatusCode() + " is the undefined "
                                    + ActionType.REPLACEMENT.getName() + " flow.");
            }
        }
    }
}
