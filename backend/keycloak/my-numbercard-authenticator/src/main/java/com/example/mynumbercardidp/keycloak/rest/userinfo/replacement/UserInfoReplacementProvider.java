package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.common.ClientConnection;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.ActionType;
import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.MyNumberCardAuthenticatorFactory;
import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;

public class UserInfoReplacementProvider implements RealmResourceProvider {
    private KeycloakSession session;
    private static Logger consoleLogger = Logger.getLogger(UserInfoReplacementProvider.class);

    public UserInfoReplacementProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
    }

    // HTTPステータスコードは302
    // Run URI od xxx application と 認証SPIのaction urlをLocationヘッダーで返す
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/login")
    @NoCache
    public Response login(String requestBody) {
        try {
            String authenticationSessionId = authenticate(this.session.getContext().getRequestHeaders())
                    .orElseThrow(() -> {
                        return new IllegalArgumentException("Invalid token.");
                    });
            AuthenticatorConfigModel authenticatorConfigModel = getAuthenticatorConfig(this.session.getContext());
            String runUriApplication = getConfigRunUriOfApplication(authenticatorConfigModel,
                    this.session.getContext().getRequestHeaders());

            MultivaluedMap<String, String> queryParameters = this.session.getContext().getHttpRequest().getUri()
                    .getQueryParameters();
            String clientRedirectURI = Objects
                    .requireNonNull(URLDecoder.decode(queryParameters.getFirst("redirect_uri"), "UTF-8"));
            boolean allowedClientRedirectURIFlag = false;
            for (String pattern : this.session.getContext().getClient().getRedirectUris()) {
                if (!allowedClientRedirectURIFlag) {
                    allowedClientRedirectURIFlag = clientRedirectURI.matches(pattern.replace("*", ".*?"));
                }
            }
            if (!allowedClientRedirectURIFlag) {
                this.session.getContext().getClient().getRedirectUris().forEach(value -> {
                    UserInfoReplacementProvider.consoleLogger.debugf("Allowed client redirect URI: %s", value);
                });
                throw new IllegalArgumentException(
                        "Redirect URI is unauthorized value. {redirect_uri=" + clientRedirectURI + "}");
            }
            this.session.getContext().getAuthenticationSession().setRedirectUri(clientRedirectURI);
            UserInfoReplacementProvider.consoleLogger.debugf("Client Redirect URI: %s", clientRedirectURI);
            this.session.getContext().getAuthenticationSession().setAuthNote(
                    OIDCLoginProtocol.RESPONSE_TYPE_PARAM,
                    queryParameters.getFirst(OIDCLoginProtocol.RESPONSE_TYPE_PARAM));
            this.session.getContext().getAuthenticationSession().setAuthNote(OIDCLoginProtocol.SCOPE_PARAM,
                    queryParameters.getFirst(OIDCLoginProtocol.SCOPE_PARAM));
            URI replacementPostUrl = new URI(runUriApplication
                    + createQueryParameters(authenticatorConfigModel, authenticationSessionId,
                            this.session.getContext()));
            return Response.status(Response.Status.FOUND).location(replacementPostUrl).build();
        } catch (IllegalArgumentException | NullPointerException e) {
            // クライアントから必須の値を渡されなかった場合にBad requestの応答を返すため、NullPointerExceptionも補足する。
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (InternalError | UnsupportedEncodingException | URISyntaxException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/replace")
    @NoCache
    public Response replace(String requestBody) {
        MultivaluedMap<String, String> queryParameters = this.session.getContext().getHttpRequest().getUri()
                .getQueryParameters();
        try {
            String sessionId = Optional.ofNullable(queryParameters.getFirst("session_code")).orElseThrow(() -> {
                return new IllegalArgumentException("Query parameter session_code is null.");
            });
            String clientName = Optional.ofNullable(queryParameters.getFirst("client_id")).orElseThrow(() -> {
                return new IllegalArgumentException("Query parameter client_id is null.");
            });
            RealmModel realm = this.session.getContext().getRealm();
            this.session.getContext().setRealm(realm);
            ClientModel client = realm.getClientByClientId(clientName);
            this.session.getContext().setClient(client);
            String tabId = Optional.ofNullable(queryParameters.getFirst("tab_id")).orElseThrow(() -> {
                return new IllegalArgumentException("Query parameter tab_id is null.");
            });
            RootAuthenticationSessionModel rootAuthenticationSession = this.session.authenticationSessions()
                    .getRootAuthenticationSession(realm, sessionId);
            this.session.getContext()
                    .setAuthenticationSession(
                            Optional.ofNullable(rootAuthenticationSession.getAuthenticationSession(client, tabId))
                                    .orElseThrow(() -> {
                                        return new IllegalArgumentException(
                                                "Not found authentication session. {Clinet name: " + clientName
                                                        + ", Tab ID: " + tabId + "}");
                                    }));
            UserInfoReplacementProvider.consoleLogger.debugf("Session code: %s", sessionId);
            UserInfoReplacementProvider.consoleLogger.debugf("Client name: %s", client.getClientId());
            UserInfoReplacementProvider.consoleLogger.debugf("Realm name: %s", realm.getName());
            UserInfoReplacementProvider.consoleLogger.debugf("Tab id: %s", tabId);

            String clientRedirectURI = this.session.getContext().getAuthenticationSession().getRedirectUri();
            this.session.getContext().getAuthenticationSession().setRedirectUri(clientRedirectURI);
            UserInfoReplacementProvider.consoleLogger.debugf("Client Redirect URI: %s", clientRedirectURI);

            MultivaluedMap<String, String> formData = decodeFormURLEncodedParameters(requestBody);
            ReplacementActionAdapter replacementAction = new ReplacementActionAdapter();
            return replacementAction.replace(session, formData);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private Optional<String> authenticate(HttpHeaders headers) {
        String tokenString = AppAuthManager.extractAuthorizationHeaderToken(headers);
        AccessToken token = null;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new IllegalArgumentException(e);
        }

        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(this.session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        this.session.getContext().setRealm(realm);

        if (Objects.isNull(realm)) {
            return Optional.ofNullable((String) null);
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        this.session.getContext().setClient(client);

        RootAuthenticationSessionModel rootAuthSession = this.session.authenticationSessions()
                .createRootAuthenticationSession(realm);
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);
        this.session.getContext().setAuthenticationSession(authSession);
        this.session.getContext().getAuthenticationSession().setAuthNote("accessToken", tokenString);
        this.session.getContext().getAuthenticationSession().setAuthNote("userSessionId", token.getSessionId());

        ClientConnection clientConnection = session.getContext().getConnection();
        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .setRealm(realm)
                .setUriInfo(session.getContext().getUri())
                .setTokenString(tokenString)
                .setConnection(clientConnection)
                .setHeaders(headers)
                .authenticate();
        if (Objects.isNull(authResult)) {
            return Optional.ofNullable((String) null);
        }
        authSession.setAuthenticatedUser(authResult.getUser());
        return Optional.ofNullable(rootAuthSession.getId());
    }

    private AuthenticatorConfigModel getAuthenticatorConfig(KeycloakContext context) {
        String authenticaterId = new MyNumberCardAuthenticatorFactory().getId();
        AuthenticationExecutionModel authenticationExecution = AuthenticationUtil
                .findAuthenticationExecutionModel(context.getRealm(), context.getClient(), authenticaterId)
                .orElseThrow(() -> {
                    return new IllegalStateException(
                            "Not found authentication execution. Make sure you have an authentication flow configured.");
                });
        return context.getRealm().getAuthenticatorConfigById(authenticationExecution.getAuthenticatorConfig());
    }

    private String getConfigRunUriOfApplication(AuthenticatorConfigModel authConfig, HttpHeaders headers) {
        String userAgent = headers.getRequestHeaders().getFirst(HttpHeaders.USER_AGENT.toString());
        if (userAgent.contains("Android")) {
            return authConfig.getConfig().get(SpiConfigProperty.RunUriOfAndroidApplication.CONFIG.getName());
        } else if (userAgent.contains("iPad") || userAgent.contains("iPhone") || userAgent.contains("iPod")) {
            return authConfig.getConfig().get(SpiConfigProperty.RunUriOfiOSApplication.CONFIG.getName());
        } else {
            return authConfig.getConfig()
                    .get(SpiConfigProperty.InstallationUriOfSmartPhoneApplication.CONFIG.getName());
        }
    }

    private String createQueryParameters(AuthenticatorConfigModel authenticatorConfigModel,
            String authenticationSessionId, KeycloakContext context) {
        String actionUrl = createActionUrl(authenticatorConfigModel, authenticationSessionId,
                this.session.getContext());
        UserInfoReplacementProvider.consoleLogger.debugf("ActionUrl: %s", actionUrl);
        try {
            StringBuffer queryParameters = new StringBuffer("?action_url=" + URLEncoder.encode(actionUrl, "UTF-8"));
            String nonce = UUID.randomUUID().toString();
            context.getAuthenticationSession().setAuthNote("nonce", nonce);
            UserInfoReplacementProvider.consoleLogger.debugf("nonce: %s",
                    context.getAuthenticationSession().getAuthNote("nonce"));
            queryParameters.append("&nonce=" + nonce);
            queryParameters.append("&mode=" + ActionType.REPLACEMENT.toString().toLowerCase());
            return queryParameters.toString();
        } catch (UnsupportedEncodingException e) {
            // ハードコーディングしたエンコード文字セットの指定に誤りがあるため、このエラーが発生した場合はコードに誤りがある。
            throw new InternalError(e);
        }
    }

    private String createActionUrl(AuthenticatorConfigModel authenticatorConfigModel,
            String authenticationSessionId, KeycloakContext context) {
        StringBuffer actionUrl = new StringBuffer(context.getAuthServerUrl().toString() + "realms/"
                + context.getRealm().getName() + "/" + UserInfoReplacementProviderFactory.ID + "/replace");
        actionUrl.append("?session_code=" + authenticationSessionId);
        actionUrl.append("&client_id=" + context.getAuthenticationSession().getClient().getClientId());
        actionUrl.append("&tab_id=" + context.getAuthenticationSession().getTabId());
        return actionUrl.toString();
    }

    private MultivaluedMap<String, String> decodeFormURLEncodedParameters(String requestBody) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        Arrays.asList(requestBody.split("&")).forEach(line -> {
            try {
                String[] encodedParameter = line.split("=", 2);
                if (encodedParameter.length == 0) {
                    return;
                }
                String key = "";
                if (encodedParameter[0].startsWith("amp;")) {
                    key = encodedParameter[0].replaceFirst("amp;", "");
                } else {
                    key = encodedParameter[0];
                }
                String value = "";
                if (encodedParameter.length > 1) {
                    value = URLDecoder.decode(encodedParameter[1], "UTF-8");
                }
                formData.add(key, value);
            } catch (UnsupportedEncodingException e) {
                // ハードコーディングしたエンコード文字セットの指定に誤りがあるため、このエラーが発生した場合はコードに誤りがある。
                throw new InternalError(e);
            }
        });
        return formData;
    }
}
