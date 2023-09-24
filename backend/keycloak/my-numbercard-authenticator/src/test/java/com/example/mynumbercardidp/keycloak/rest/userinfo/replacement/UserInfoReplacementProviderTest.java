package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.ClientConnection;
import org.keycloak.http.HttpRequest;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.protocol.oidc.utils.OIDCRedirectUriBuilder;
import org.keycloak.protocol.oidc.utils.OIDCResponseMode;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.MyNumberCardAuthenticatorFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

public class UserInfoReplacementProviderTest {
    private AutoCloseable closeable;
    private Response expected;
    private static final UUID uuid = UUID.fromString("a05500d4-6764-4c72-9765-109b198189b5");
    private Response responseData;

    @Mock
    KeycloakContext keycloakContext;
    @Mock
    AccessToken accessToken;
    @Mock
    KeycloakSession keycloakSession;
    @Mock
    HttpHeaders httpHeaders;
    @Mock
    RealmModel realmModel;
    @Mock
    ClientModel clientModel;
    @Mock
    AuthenticationSessionProvider authenticationSessionProvider;
    @Mock
    RootAuthenticationSessionModel rootAuthenticationSessionModel;
    @Mock
    AuthenticationSessionModel authenticationSessionModel;
    @Mock
    ClientConnection clientConnection;
    @Mock
    KeycloakUriInfo keycloakUriInfo;
    @Mock
    AuthResult authResult;
    @Mock
    HttpRequest httpRequest;
    @Mock
    UriInfo uriInfo;
    @Mock
    Optional<AuthenticationExecutionModel> authenticationSessionModelOptional;
    @Mock
    AuthenticationExecutionModel authenticationExecutionModel;
    @Mock
    AuthenticatorConfigModel authenticatorConfigModel;
    @Mock
    URI uri;
    @Mock
    Response response;
    @Mock
    SingleUseObjectProvider singleUseObjectProvider;
    @Mock
    OIDCResponseMode oidcResponseMode;
    @Mock
    AuthenticatedClientSessionModel authenticatedClientSessionModel;


    @InjectMocks
    UserInfoReplacementProvider userInfoReplacementProvider;

    @BeforeEach
    public void setup() throws URISyntaxException, JsonProcessingException {
        closeable = MockitoAnnotations.openMocks(this);
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.putSingle("Authorization", "True");

        MultivaluedMap<String, String> queryParameterMap = new MultivaluedHashMap<>();
        queryParameterMap.putSingle("redirect_uri", "test_redirect_uri");
        queryParameterMap.putSingle("response_type", "test_response_type");
        queryParameterMap.putSingle("scope", "test_scope");
        queryParameterMap.putSingle("session_code", "test_session_code");
        queryParameterMap.putSingle("client_id", "test_client_id");
        queryParameterMap.putSingle("tab_id", "test_tab_id");

        Set<String> redirectUriSet = new HashSet<String>();
        redirectUriSet.add("test_redirect_uri");

        MultivaluedMap<String, String> requestHeaderMap = new MultivaluedHashMap<>();
        requestHeaderMap.putSingle("User-Agent", "Android");

        Map<String, String> config = Map.of(
            "my-num-cd-auth.android-app-uri", "android-app-uri",
            "my-num-cd-auth.ios-app-uri", "ios-app-uri",
            "my-num-cd-auth.app-uri", "app-uri"
        );

        Long expTime = ((long) (System.currentTimeMillis() / 1000) + 10);

        responseData = OIDCRedirectUriBuilder.fromUri("testUri", oidcResponseMode, keycloakSession, authenticatedClientSessionModel)
        .addParam(OAuth2Constants.STATE, "state")
        .addParam(OAuth2Constants.SESSION_STATE, "session_state")
        .addParam(Constants.KC_ACTION_STATUS, "kc_action_status")
        .addParam(OAuth2Constants.CODE, "code").build();

        doReturn("https://8fee-118-238-7-69.ngrok-free.app/realms/OIdp").when(accessToken).getIssuer();
        doReturn("test_issued_for").when(accessToken).getIssuedFor();
        doReturn("test_session_id").when(accessToken).getSessionId();
        doReturn(expTime).when(accessToken).getExp();
        doReturn("test_token_id").when(accessToken).getId();
        doReturn(keycloakContext).when(keycloakSession).getContext();
        doReturn(authenticationSessionProvider).when(keycloakSession).authenticationSessions();
        doReturn(singleUseObjectProvider).when(keycloakSession).getProvider(any());
        doReturn(httpHeaders).when(keycloakContext).getRequestHeaders();
        doReturn(authenticationSessionModel).when(keycloakContext).getAuthenticationSession();
        doReturn(clientConnection).when(keycloakContext).getConnection();
        doReturn(keycloakUriInfo).when(keycloakContext).getUri();
        doReturn(httpRequest).when(keycloakContext).getHttpRequest();
        doReturn(clientModel).when(keycloakContext).getClient();
        doReturn(realmModel).when(keycloakContext).getRealm();
        doReturn(uri).when(keycloakContext).getAuthServerUrl();
        doReturn(redirectUriSet).when(clientModel).getRedirectUris();
        doReturn("test_client_id").when(clientModel).getClientId();
        doReturn(uriInfo).when(httpRequest).getUri();
        doReturn(requestHeaderMap).when(httpHeaders).getRequestHeaders();
        doReturn(queryParameterMap).when(uriInfo).getQueryParameters();
        doReturn(clientModel).when(realmModel).getClientByClientId(anyString());
        doReturn(authenticatorConfigModel).when(realmModel).getAuthenticatorConfigById(any());
        doReturn("test_realm_name").when(realmModel).getName();
        doReturn(rootAuthenticationSessionModel).when(authenticationSessionProvider).createRootAuthenticationSession(any());
        doReturn(rootAuthenticationSessionModel).when(authenticationSessionProvider).getRootAuthenticationSession(any(), any());
        doReturn(authenticationSessionModel).when(rootAuthenticationSessionModel).createAuthenticationSession(any());
        doReturn("test_id").when(rootAuthenticationSessionModel).getId();
        doReturn(authenticationSessionModel).when(rootAuthenticationSessionModel).getAuthenticationSession(any(), any());
        doReturn(clientModel).when(authenticationSessionModel).getClient();
        doReturn("test_tab_id").when(authenticationSessionModel).getTabId();
        doReturn("test_redirect_uri").when(authenticationSessionModel).getRedirectUri();
        doReturn(authenticationExecutionModel).when(authenticationSessionModelOptional).orElseThrow(any());
        doReturn("test_authenticator_config").when(authenticationExecutionModel).getAuthenticatorConfig();
        doReturn(config).when(authenticatorConfigModel).getConfig();

        doNothing().when(keycloakContext).setRealm(any());
        doNothing().when(keycloakContext).setClient(any());
        doNothing().when(keycloakContext).setAuthenticationSession(any());
        doNothing().when(authenticationSessionModel).setAuthNote(any(), any());
        doNothing().when(authenticationSessionModel).setAuthenticatedUser(any());
        doNothing().when(authenticationSessionModel).setRedirectUri(any());
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetResouce() {
        Object expected = new UserInfoReplacementProvider(keycloakSession);
        Object result = userInfoReplacementProvider.getResource();
        assertSame(expected instanceof UserInfoReplacementProvider, result instanceof UserInfoReplacementProvider);
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> {
            userInfoReplacementProvider.close();
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testAuthenticate(Boolean isRealmError) throws Exception {
        try (
            MockedConstruction<JWSInput> JWSInput = mockConstruction(JWSInput.class,
                                                    (mock, ctx) -> {
                                                        doReturn(accessToken).when(mock).readJsonContent(any());
                                                    });
            MockedConstruction<RealmManager> realmManager = mockConstruction(RealmManager.class,
                                                    (mock, ctx) -> {
                                                        if (isRealmError) {
                                                            doReturn(null).when(mock).getRealmByName(any());
                                                        } else {
                                                            doReturn(realmModel).when(mock).getRealmByName(any());
                                                        }

                                                    });
            MockedConstruction<AppAuthManager> appAuthManager = mockConstruction(AppAuthManager.class,
                                                    (mock, ctx) -> {});
            MockedConstruction<AppAuthManager.BearerTokenAuthenticator> bearerTokenAuthenticator = mockConstruction(AppAuthManager.BearerTokenAuthenticator.class,
                                                    (mock, ctx) -> {
                                                        doReturn(mock).when(mock).setRealm(any());
                                                        doReturn(mock).when(mock).setUriInfo(any());
                                                        doReturn(mock).when(mock).setTokenString(any());
                                                        doReturn(mock).when(mock).setConnection(any());
                                                        doReturn(mock).when(mock).setHeaders(any());
                                                        doReturn(authResult).when(mock).authenticate();
                                                    });
            MockedConstruction<MyNumberCardAuthenticatorFactory> myNumberCardAuthenticatorFactory = mockConstruction(MyNumberCardAuthenticatorFactory.class,
                                                    (mock, ctx) -> {
                                                        doReturn("authenticater_id").when(mock).getId();
                                                    });

            MockedStatic<AppAuthManager> appAuthManagerStatic = mockStatic(AppAuthManager.class);
            MockedStatic<AuthenticationUtil> authenticationUtilStatic = mockStatic(AuthenticationUtil.class);
            MockedStatic<UUID> uuidStatic = mockStatic(UUID.class);
        ) {

            authenticationUtilStatic.when(() -> AuthenticationUtil.findAuthenticationExecutionModel(any(), any(), any())).thenReturn(authenticationSessionModelOptional);
            uuidStatic.when(() -> UUID.randomUUID()).thenReturn(uuid);

            if (isRealmError) {
                expected = Response.status(Response.Status.BAD_REQUEST).build();
                Response result = userInfoReplacementProvider.authenticate("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}");
                assertEquals(expected.getStatus(), result.getStatus());
                assertEquals(expected instanceof Response, result instanceof Response);
            } else {
                URI location = new URI("android-app-uri?action_url=uri%2Frealms%2Ftest_realm_name%2Fuserinfo-replacement%2Freplace%3Fsession_code%3Dtest_id%26client_id%3Dtest_client_id%26tab_id%3Dtest_tab_id&nonce=a05500d4-6764-4c72-9765-109b198189b5&mode=replacement");
                expected = Response.status(Response.Status.FOUND).location(location).build();
                Response result = userInfoReplacementProvider.authenticate("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}");
                assertEquals(expected.getStatus(), result.getStatus());
                assertEquals(expected.getLocation(), result.getLocation());
            }

        }
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testReplace(Boolean isError) {
        try (
            MockedConstruction<ReplacementActionAdapter> replacementActionAdapter = mockConstruction(ReplacementActionAdapter.class,
                                                    (mock, ctx) -> {
                                                        doReturn(responseData).when(mock).replace(any());
                                                    });
            MockedConstruction<JWSInput> JWSInput = mockConstruction(JWSInput.class,
                                                    (mock, ctx) -> {
                                                        if (isError) {
                                                            doReturn(null).when(mock).readJsonContent(any());
                                                        } else {
                                                            doReturn(accessToken).when(mock).readJsonContent(any());
                                                        }
                                                    });
        ) {
            if (isError) {
                expected = Response.status(Response.Status.BAD_REQUEST).build();
                Response result = userInfoReplacementProvider.replace("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}");
                assertEquals(expected.getStatus(), result.getStatus());
                assertEquals(expected instanceof Response, result instanceof Response);
            } else {
                Response expected = responseData;
                Response result = userInfoReplacementProvider.replace("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}");
                assertEquals(expected, result);
            }
        }
    }

}
