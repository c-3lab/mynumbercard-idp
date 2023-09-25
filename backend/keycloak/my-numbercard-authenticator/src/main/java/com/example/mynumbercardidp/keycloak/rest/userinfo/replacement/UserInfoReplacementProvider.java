package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
import org.keycloak.common.util.Time;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
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
    private final Logger CONSOLE_LOGGER = Logger.getLogger(UserInfoReplacementProvider.class);
    private KeycloakSession session;

    public UserInfoReplacementProvider(KeycloakSession session) {
        this.session = Objects.requireNonNull(session);
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/login")
    @NoCache
    public Response authenticate(String requestBody) {
        try {
            String authenticationSessionId = createAuthSession(this.session);

            MultivaluedMap<String, String> queryParameters = this.session.getContext().getHttpRequest().getUri()
                    .getQueryParameters();
            String clientRedirectURI = getRedirectURIFromQueryParameters(queryParameters);
            this.session.getContext().getAuthenticationSession().setRedirectUri(clientRedirectURI);
            this.CONSOLE_LOGGER.debugf("Client Redirect URI: %s", clientRedirectURI);
            this.session.getContext().getAuthenticationSession().setAuthNote(
                    OIDCLoginProtocol.RESPONSE_TYPE_PARAM,
                    Objects.requireNonNull(queryParameters.getFirst(OIDCLoginProtocol.RESPONSE_TYPE_PARAM),
                            "Query parameter " + OIDCLoginProtocol.RESPONSE_TYPE_PARAM + " is null"));
            this.session.getContext().getAuthenticationSession().setAuthNote(OIDCLoginProtocol.SCOPE_PARAM,
                    Objects.requireNonNull(queryParameters.getFirst(OIDCLoginProtocol.SCOPE_PARAM),
                            "Query parameter " + OIDCLoginProtocol.SCOPE_PARAM + " is null"));

            AuthenticatorConfigModel authenticatorConfigModel = getAuthenticatorConfig(this.session.getContext());
            String runUriApplication = getConfigRunUriOfApplication(authenticatorConfigModel,
                    this.session.getContext().getRequestHeaders());
            URI replacementPostUrl = new URI(runUriApplication
                    + createQueryParameters(authenticatorConfigModel, authenticationSessionId,
                            this.session.getContext()));
            return Response.status(Response.Status.FOUND).location(replacementPostUrl).build();
        } catch (IllegalArgumentException | JWSInputException | NullPointerException e) {
            // クライアントから必須の値を渡されなかった場合にBad requestの応答を返すため、NullPointerExceptionも補足する。
            this.CONSOLE_LOGGER.warnf("Bad request: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            // 想定外の非検査例外も含め、他の全ての例外を補足し、Internal Server Errorの応答を返す。
            this.CONSOLE_LOGGER.error("Internal server error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/replace")
    @NoCache
    public Response replace(String requestBody) {
        try {
            setKeycloakContextFromQueryParameters(this.session,
                    this.session.getContext().getHttpRequest().getUri().getQueryParameters());
            Response response = new ReplacementActionAdapter(session)
                    .replace(decodeFormURLEncodedParameters(requestBody));
            JWSInput accessTokeninput = new JWSInput(
                    session.getContext().getAuthenticationSession().getAuthNote("accessToken"));
            revokeAccessToken(accessTokeninput.readJsonContent(AccessToken.class));
            return response;
        } catch (IllegalArgumentException | NullPointerException e) {
            // クライアントから必須の値を渡されなかった場合にBad requestの応答を返すため、NullPointerExceptionも補足する。
            this.CONSOLE_LOGGER.warnf("Bad request: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            // 想定外の非検査例外も含め、他の全ての例外を補足し、Internal Server Errorの応答を返す。
            this.CONSOLE_LOGGER.error("Internal server error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String createAuthSession(KeycloakSession session) throws JWSInputException {
        String tokenString = AppAuthManager.extractAuthorizationHeaderToken(session.getContext().getRequestHeaders());
        JWSInput input = new JWSInput(tokenString);
        AccessToken token = input.readJsonContent(AccessToken.class);
        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        session.getContext().setRealm(realm);
        if (Objects.isNull(realm)) {
            throw new IllegalArgumentException("Invalid token.");
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (Objects.isNull(client)) {
            throw new IllegalArgumentException("Invalid token.");
        }
        session.getContext().setClient(client);

        RootAuthenticationSessionModel rootAuthSession = this.session.authenticationSessions()
                .createRootAuthenticationSession(realm);
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);
        session.getContext().setAuthenticationSession(authSession);
        session.getContext().getAuthenticationSession().setAuthNote("accessToken", tokenString);
        session.getContext().getAuthenticationSession().setAuthNote("userSessionId", token.getSessionId());

        ClientConnection clientConnection = session.getContext().getConnection();
        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .setRealm(realm)
                .setUriInfo(session.getContext().getUri())
                .setTokenString(tokenString)
                .setConnection(clientConnection)
                .setHeaders(session.getContext().getRequestHeaders())
                .authenticate();
        if (Objects.isNull(authResult)) {
            throw new IllegalArgumentException("Invalid token.");
        }
        authSession.setAuthenticatedUser(authResult.getUser());
        return rootAuthSession.getId();
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
            String authenticationSessionId, KeycloakContext context) throws UnsupportedEncodingException {
        String actionUrl = createActionUrl(authenticatorConfigModel, authenticationSessionId,
                this.session.getContext());
        this.CONSOLE_LOGGER.debugf("ActionUrl: %s", actionUrl);
        StringBuffer queryParameters = new StringBuffer("?action_url=" + URLEncoder.encode(actionUrl, "UTF-8"));
        String nonce = UUID.randomUUID().toString();
        context.getAuthenticationSession().setAuthNote("nonce", nonce);
        this.CONSOLE_LOGGER.debugf("nonce: %s",
                context.getAuthenticationSession().getAuthNote("nonce"));
        queryParameters.append("&nonce=" + nonce);
        queryParameters.append("&mode=" + ActionType.REPLACEMENT.toString().toLowerCase());
        return queryParameters.toString();
    }

    private String createActionUrl(AuthenticatorConfigModel authenticatorConfigModel,
            String authenticationSessionId, KeycloakContext context) {
        String authServerURL = context.getAuthServerUrl().toString().endsWith("/")
                ? context.getAuthServerUrl().toString()
                : context.getAuthServerUrl().toString() + "/";
        StringBuffer actionUrl = new StringBuffer(authServerURL + "realms/"
                + context.getRealm().getName() + "/" + UserInfoReplacementProviderFactory.ID + "/replace");
        actionUrl.append("?session_code=" + authenticationSessionId);
        actionUrl.append("&client_id=" + context.getAuthenticationSession().getClient().getClientId());
        actionUrl.append("&tab_id=" + context.getAuthenticationSession().getTabId());
        return actionUrl.toString();
    }

    private MultivaluedMap<String, String> decodeFormURLEncodedParameters(String requestBody)
            throws UnsupportedEncodingException {
        try {
            MultivaluedMap<String, String> decordedFormData = new MultivaluedHashMap<>();
            Arrays.asList(requestBody.split("&")).forEach(formDataOnePair -> {
                try {
                    Map<String, String> decodedParameter = decodeFromURLEncodedParameter(formDataOnePair);
                    if (decodedParameter.size() == 0) {
                        return;
                    }
                    decordedFormData.add(decodedParameter.get("name"), decodedParameter.get("value"));
                } catch (UnsupportedEncodingException e) {
                    // ラムダ式の中では検査例外を補足する必要があるため、非検査例外として送出する。
                    throw new InternalError(e);
                }
            });
            return decordedFormData;
        } catch (InternalError e) {
            // 呼び出し元へ本来の検査例外を送出するため、ラムダ式で送出した例外から検査例外を取り出す。
            throw (UnsupportedEncodingException) e.getCause();
        }
    }

    private Map<String, String> decodeFromURLEncodedParameter(String formDataOnePair)
            throws UnsupportedEncodingException {
        String[] encodedParameterArray = formDataOnePair.split("=", 2);
        if (encodedParameterArray.length == 0) {
            return new HashMap<>(0);
        }

        Map<String, String> encodedParameter = new HashMap<>(2);
        encodedParameter.put("name", encodedParameterArray[0]);
        if (encodedParameterArray.length == 1) {
            encodedParameter.put("value", "");
        } else {
            encodedParameter.put("value", encodedParameterArray[1]);
        }

        Map<String, String> decordedParameter = new HashMap<>(2);
        if (encodedParameter.get("name").startsWith("amp;")) {
            decordedParameter.put("name", encodedParameter.get("name").replaceFirst("amp;", ""));
        } else {
            decordedParameter.put("name", encodedParameter.get("name"));
        }
        decordedParameter.put("value", URLDecoder.decode(encodedParameter.get("value"), "UTF-8"));
        return decordedParameter;
    }

    private String getRedirectURIFromQueryParameters(MultivaluedMap<String, String> queryParameters)
            throws UnsupportedEncodingException {
        if (Objects.isNull(queryParameters.getFirst("redirect_uri"))) {
            throw new IllegalArgumentException("Query parameter redirect_uri is null");
        } else if (queryParameters.getFirst("redirect_uri").length() == 0) {
            throw new IllegalArgumentException("Query parameter redirect_uri is empty");
        }
        String clientRedirectURI = URLDecoder.decode(queryParameters.getFirst("redirect_uri"), "UTF-8");
        boolean allowedClientRedirectURIFlag = false;
        for (String redirectUri : this.session.getContext().getClient().getRedirectUris()) {
            if (!allowedClientRedirectURIFlag) {
                allowedClientRedirectURIFlag = clientRedirectURI.matches(redirectUri.replace("*", ".*?"));
            }
        }
        if (!allowedClientRedirectURIFlag) {
            this.session.getContext().getClient().getRedirectUris().forEach(value -> {
                this.CONSOLE_LOGGER.debugf("Allowed client redirect URI: %s", value);
            });
            throw new IllegalArgumentException(
                    "Redirect URI is unauthorized value. {redirect_uri=" + clientRedirectURI + "}");
        }
        return clientRedirectURI;
    }

    private void setKeycloakContextFromQueryParameters(KeycloakSession session,
            MultivaluedMap<String, String> queryParameters) {
        String sessionId = Objects.requireNonNull(queryParameters.getFirst("session_code"),
                "Query parameter session_code is null.");
        String clientName = Objects.requireNonNull(queryParameters.getFirst("client_id"),
                "Query parameter client_id is null.");
        RealmModel realm = session.getContext().getRealm();
        session.getContext().setRealm(realm);
        ClientModel client = realm.getClientByClientId(clientName);
        session.getContext().setClient(client);
        String tabId = Objects.requireNonNull(queryParameters.getFirst("tab_id"), "Query parameter tab_id is null.");
        RootAuthenticationSessionModel rootAuthenticationSession = session.authenticationSessions()
                .getRootAuthenticationSession(realm, sessionId);
        AuthenticationSessionModel authenticationSession = Objects.requireNonNull(
                rootAuthenticationSession.getAuthenticationSession(client, tabId),
                "Not found authentication session. {Clinet name: " + clientName + ", Tab ID: " + tabId + "}");
        session.getContext().setAuthenticationSession(authenticationSession);
        this.CONSOLE_LOGGER.debugf("Session code: %s", sessionId);
        this.CONSOLE_LOGGER.debugf("Client name: %s", client.getClientId());
        this.CONSOLE_LOGGER.debugf("Realm name: %s", realm.getName());
        this.CONSOLE_LOGGER.debugf("Tab id: %s", tabId);

        String clientRedirectURI = session.getContext().getAuthenticationSession().getRedirectUri();
        session.getContext().getAuthenticationSession().setRedirectUri(clientRedirectURI);
        this.CONSOLE_LOGGER.debugf("Client Redirect URI: %s", clientRedirectURI);
    }

    private void revokeAccessToken(AccessToken token) {
        SingleUseObjectProvider singleUseStore = session.getProvider(SingleUseObjectProvider.class);
        int maxSeconds = 10;
        long lifespanInSecs = Math.max(token.getExp() - Time.currentTime(), maxSeconds);
        singleUseStore.put(token.getId() + SingleUseObjectProvider.REVOKED_KEY, lifespanInSecs, Collections.emptyMap());
    }
}
