package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.Authenticator;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.utils.OIDCRedirectUriBuilder;
import org.keycloak.protocol.oidc.utils.OIDCResponseMode;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.MyNumberCardAuthenticatorFactory;
import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;
import com.example.mynumbercardidp.keycloak.core.network.platform.PlatformApiClientInterface;
import com.example.mynumbercardidp.keycloak.util.authentication.CurrentConfig;

public class ReplacementActionAdapterTest {

    private MultivaluedMap<String, String> formData;
    private Response responseData;
    private ReplacementActionAdapter replacementActionAdapter;
    private AutoCloseable closeable;

    private static final MockedStatic<AuthenticationUtil> authenticationUtilStatic = mockStatic(AuthenticationUtil.class);

    @Mock
    KeycloakSession keycloakSession;
    @Mock
    KeycloakContext keycloakContext;
    @Mock
    RealmModel realmModel;
    @Mock
    ClientModel clientModel;
    @Mock
    HttpRequest httpRequest;
    @Mock
    KeycloakUriInfo keycloakUriInfo;
    @Mock
    Authenticator authenticator;
    @Mock
    AuthenticationExecutionModel authenticationExecutionModel;
    @Mock
    Optional<AuthenticationExecutionModel> authenticationSessionModelOptional;
    @Mock
    AuthenticationSessionModel authenticationSessionModel;
    @Mock
    AuthenticationProcessor.Result authenticationProcessorResult;
    @Mock
    PlatformApiClientInterface platformApiClientInterface;
    @Mock
    AuthenticationFlowContext authenticationFlowContext;
    @Mock
    Response response;
    @Mock
    OIDCResponseMode oidcResponseMode;
    @Mock
    AuthenticatedClientSessionModel authenticatedClientSessionModel;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        formData = new MultivaluedHashMap<>() {
            {
                putSingle("response_type", "testResponse");
            }
        };

        responseData = OIDCRedirectUriBuilder.fromUri("testUri", oidcResponseMode, keycloakSession, authenticatedClientSessionModel)
        .addParam(OAuth2Constants.STATE, "state")
        .addParam(OAuth2Constants.SESSION_STATE, "session_state")
        .addParam(Constants.KC_ACTION_STATUS, "kc_action_status")
        .addParam(OAuth2Constants.CODE, "code").build();

        doReturn(keycloakContext).when(keycloakSession).getContext();
        doReturn(realmModel).when(keycloakContext).getRealm();
        doReturn(clientModel).when(keycloakContext).getClient();
        doReturn(httpRequest).when(keycloakContext).getHttpRequest();
        doReturn(authenticationSessionModel).when(keycloakContext).getAuthenticationSession();
        doReturn(keycloakUriInfo).when(keycloakContext).getUri();
        doReturn("test_name").when(realmModel).getName();
        doReturn(authenticationExecutionModel).when(authenticationSessionModelOptional).orElseThrow();
        doReturn("test_frow_id").when(authenticationExecutionModel).getFlowId();
        doReturn(formData).when(httpRequest).getDecodedFormParameters();
        doNothing().when(authenticationSessionModel).setClientNote(any(), any());
        doNothing().when(platformApiClientInterface).setContextForDataManager(authenticationFlowContext);
        authenticationUtilStatic.when(() -> AuthenticationUtil.findAuthenticationExecutionModel(any(), any(), any())).thenReturn(authenticationSessionModelOptional);

        replacementActionAdapter = new ReplacementActionAdapter(keycloakSession);

    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"normal", "runtime", "exception"})  // 正常系、RuntimeException発生、Exception発生
    public void testReplace(String pattern) throws Exception {

        try(
            MockedConstruction<AuthenticationProcessor> authenticationProcessor = mockConstruction(AuthenticationProcessor.class,
                                                            (mock, ctx) -> {
                                                                doReturn(mock).when(mock).setSession(any());
                                                                doReturn(mock).when(mock).setRealm(any());
                                                                doReturn(mock).when(mock).setBrowserFlow(anyBoolean());
                                                                doReturn(mock).when(mock).setRequest(any());
                                                                doReturn(mock).when(mock).setAuthenticationSession(any());
                                                                doReturn(mock).when(mock).setUriInfo(any());
                                                                doReturn(mock).when(mock).setFlowPath(any());
                                                                doReturn(mock).when(mock).setFlowId(any());
                                                                doReturn(authenticationSessionModel).when(mock).getAuthenticationSession();
                                                                doReturn(authenticationProcessorResult).when(mock).createAuthenticatorContext(any(), any(), any());
                                                                doNothing().when(mock).setClient(any());
                                                            });
            MockedConstruction<MyNumberCardAuthenticatorFactory> myNumberCardAuthenticatorFactory = mockConstruction(MyNumberCardAuthenticatorFactory.class,
                                                            (mock, ctx) -> {
                                                                doReturn(authenticator).when(mock).create(any());
                                                                doReturn("test_id").when(mock).getId();
                                                            });
            MockedConstruction<ReplacementAction> replacementAction = mockConstruction(ReplacementAction.class,
                                                            (mock, ctx) -> {
                                                                doReturn(responseData).when(mock).replace(any(), any());
                                                            });
            MockedStatic<CurrentConfig> currentConfig = mockStatic(CurrentConfig.class);
        ) {

            currentConfig.when(() -> CurrentConfig.getValue(any(), eq(SpiConfigProperty.CertificateValidatorRootUri.CONFIG.getName()))).thenReturn("CertificateValidatorRootUri");
            currentConfig.when(() -> CurrentConfig.getValue(any(), eq(SpiConfigProperty.PlatformApiIdpSender.CONFIG.getName()))).thenReturn("PlatformApiIdpSender");

            if (pattern.equals("runtime")) {
                currentConfig.when(() -> CurrentConfig.getValue(any(), eq(SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName()))).thenReturn(null);
                assertThrows(RuntimeException.class, () -> {
                    replacementActionAdapter.replace(formData);
                });
            } else if (pattern.equals("exception")) {
                currentConfig.when(() -> CurrentConfig.getValue(any(), eq(SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName()))).thenReturn("exceptionTest");
                assertThrows(Exception.class, () -> {
                    replacementActionAdapter.replace(formData);
                });
            } else {
                currentConfig.when(() -> CurrentConfig.getValue(any(), eq(SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName()))).thenReturn("com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient");

                verify(authenticationSessionModelOptional, times(1)).orElseThrow();
                Response expected = responseData;
                Response result = replacementActionAdapter.replace(formData);
                assertEquals(expected, result);

            }

        }
    }

    @AfterAll
    static void tearDownAll() {
        authenticationUtilStatic.close();
    }
}
