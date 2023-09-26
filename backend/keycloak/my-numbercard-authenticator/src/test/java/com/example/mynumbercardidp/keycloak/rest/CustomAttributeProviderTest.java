package com.example.mynumbercardidp.keycloak.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.TokenManager.AccessTokenResponseBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.util.DefaultClientSessionContext;
import org.keycloak.services.managers.AppAuthManager.BearerTokenAuthenticator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomAttributeProviderTest {
    private AutoCloseable closeable;

    @InjectMocks
    CustomAttributeProvider customAttributeProvider;

    @Mock
    KeycloakSession session;
    @Mock
    KeycloakContext context;
    @Mock
    RealmModel realm;
    @Mock
    RealmManager realmManager;
    @Mock
    UserModel user;
    @Mock
    HttpHeaders httpHeaders;
    @Mock
    HttpHeaders headers;
    @Mock
    URI uri;
    @Mock
    AppAuthManager appAuthManager;
    @Mock
    ClientConnection connection;
    @Mock
    KeycloakUriInfo keycloakUriInfo;
    @Mock
    AccessToken token;
    @Mock
    ClientModel client;
    @Mock
    AuthResult authResult;
    @Mock
    UserSessionModel userSession;
    @Mock
    UserSessionProvider userSessionProvider;
    @Mock
    AuthenticatedClientSessionModel AuthenticatedClientSessionModel;
    @Mock
    DefaultClientSessionContext defaultClientSessionContext;
    @Mock
    AccessTokenResponse accessTokenResponse;
    @Mock
    AccessTokenResponseBuilder accessTokenResponseBuilder;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    JsonNode jsonNode;
    @Mock
    SingleUseObjectProvider singleUseObjectProvider;
    @Mock
    JsonNode requestData;
    private String accessTokenString = "{\"access_token\":\"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4amRuSVo1UG0wVS10b1RNYlJyVlJNSkt3OG81RDVkVHJqVURXb3kzNlNzIn0.eyJleHAiOjE2OTU0NTMzNzcsImlhdCI6MTY5NTQ1MzA3NywiYXV0aF90aW1lIjoxNjk1NDUzMDcyLCJqdGkiOiJmMmU1ODg4NS1lZThkLTQ1YTYtOGVmMi0yNDdiNzI0ZDFmNGEiLCJpc3MiOiJodHRwczovLzllNTYtMTE4LTIzOC03LTY2Lm5ncm9rLWZyZWUuYXBwL3JlYWxtcy9PSWRwIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImQ5YWRlNzcwLWExYWUtNDdkOC05ODc3LTkzMjc1YmQ1YTQzMSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNhbXBsZS1jbGllbnQwMSIsInNlc3Npb25fc3RhdGUiOiJmYTZhODRlNS04ODY5LTQwZTYtOTkyZC1lZjBlYTc3MTg1NzYiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLW9pZHAiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIGFkZHJlc3MiLCJzaWQiOiJmYTZhODRlNS04ODY5LTQwZTYtOTkyZC1lZjBlYTc3MTg1NzYiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImFkZHJlc3MiOnt9LCJiaXJ0aF9kYXRlIjoiMTk3MC0wMS0zMSIsInVzZXJfYWRkcmVzcyI6IuadseS6rOmDveWNg-S7o-eUsOWMuuWNg-S7o-eUsDEtMSIsIm5hbWUiOiLkvZDol6Qg5aSq6YOOIiwidXNlcl9hdHRyaWJ1dGVzIjp7InNlcnZpY2VfaWQiOiJleGFtcGxlQGV4YW1wbGUuY29tIiwibm90ZXMiOiJSUDEifSwiZ2VuZGVyX2NvZGUiOiIwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiNzkxMGFlNWYtYTZjMS00MTE3LWI4OTAtZmMyZGYyZGI2M2YxIn0.BBN0Mrwe-roBKs4slNb46veTzfK2qzDkYTBnqi7ugb4wCgRoLX_KiXseq485rol8wX86vWZs3r_hTPP6L8A_B80r0gqSlOz21X92z0hgqzv9y1xk29JKNprjRosoz074v3f2JD-qoAxHmDxhz7saN862KKxbat7AjzkNTldFWYovHazAmAgC2o30Jqy1hIZgAvNKYS7sCNE5Z3ZwuOu0uEaaXOaQdHmuo7DDveuqVLVjtj_p53gBJEWCWA4y6bD6JqEfZu_A-wWzRTc5nQmTNn9dMcCDqBs2LeXfkYVL1OIhpEAUI5VN97-aotI-L3gheQ5BuRbmRbhy-brH2nA3JA\",\"expires_in\":300,\"refresh_expires_in\":1800,\"refresh_token\":\"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkYTA2NmFlMS1jZmY2LTQ5OWQtYjBhMS04NjZkMGQyNTg2NzYifQ.eyJleHAiOjE2OTU0NTQ4NzcsImlhdCI6MTY5NTQ1MzA3NywianRpIjoiMDZlYmU0YzItODJlYS00ODBhLWE0YWUtM2I4NWJjZmQ1YmQ2IiwiaXNzIjoiaHR0cHM6Ly85ZTU2LTExOC0yMzgtNy02Ni5uZ3Jvay1mcmVlLmFwcC9yZWFsbXMvT0lkcCIsImF1ZCI6Imh0dHBzOi8vOWU1Ni0xMTgtMjM4LTctNjYubmdyb2stZnJlZS5hcHAvcmVhbG1zL09JZHAiLCJzdWIiOiJkOWFkZTc3MC1hMWFlLTQ3ZDgtOTg3Ny05MzI3NWJkNWE0MzEiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoic2FtcGxlLWNsaWVudDAxIiwic2Vzc2lvbl9zdGF0ZSI6ImZhNmE4NGU1LTg4NjktNDBlNi05OTJkLWVmMGVhNzcxODU3NiIsInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwgYWRkcmVzcyIsInNpZCI6ImZhNmE4NGU1LTg4NjktNDBlNi05OTJkLWVmMGVhNzcxODU3NiJ9.v4pwOyRb_D2h1z_HXgjebk7-iupXQs2MviPgEUcridE\",\"token_type\":\"Bearer\",\"id_token\":\"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4amRuSVo1UG0wVS10b1RNYlJyVlJNSkt3OG81RDVkVHJqVURXb3kzNlNzIn0.eyJleHAiOjE2OTU0NTMzNzcsImlhdCI6MTY5NTQ1MzA3NywiYXV0aF90aW1lIjoxNjk1NDUzMDcyLCJqdGkiOiJkYzA1OTY0NS03ZDBhLTQ0ZWMtOTdlNi0wNjEwOTFkNGRmNjAiLCJpc3MiOiJodHRwczovLzllNTYtMTE4LTIzOC03LTY2Lm5ncm9rLWZyZWUuYXBwL3JlYWxtcy9PSWRwIiwiYXVkIjoic2FtcGxlLWNsaWVudDAxIiwic3ViIjoiZDlhZGU3NzAtYTFhZS00N2Q4LTk4NzctOTMyNzViZDVhNDMxIiwidHlwIjoiSUQiLCJhenAiOiJzYW1wbGUtY2xpZW50MDEiLCJzZXNzaW9uX3N0YXRlIjoiZmE2YTg0ZTUtODg2OS00MGU2LTk5MmQtZWYwZWE3NzE4NTc2IiwiYWNyIjoiMSIsInNpZCI6ImZhNmE4NGU1LTg4NjktNDBlNi05OTJkLWVmMGVhNzcxODU3NiIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiYWRkcmVzcyI6e30sImJpcnRoX2RhdGUiOiIxOTcwLTAxLTMxIiwidXNlcl9hZGRyZXNzIjoi5p2x5Lqs6YO95Y2D5Luj55Sw5Yy65Y2D5Luj55SwMS0xIiwibmFtZSI6IuS9kOiXpCDlpKrpg44iLCJ1c2VyX2F0dHJpYnV0ZXMiOnsic2VydmljZV9pZCI6ImV4YW1wbGVAZXhhbXBsZS5jb20iLCJub3RlcyI6IlJQMSJ9LCJnZW5kZXJfY29kZSI6IjAiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiI3OTEwYWU1Zi1hNmMxLTQxMTctYjg5MC1mYzJkZjJkYjYzZjEifQ.ODQ4fGf8lU1nl5kiAt3bci2hFfo6FgQn3aV7IOmN_CF9YTV2rT4_qA1AjvbiR7FhIOH9NOU4TUzzqxgcJvMeWiJoXhCIE9iJrg-1NlJ2cQ0cbYWx4GbEeuJrAMdWPSgroComGfgInWeq81nYNIvazQ7wNITEaKnuvmF3yOfATgAarRbx6A6qDNne-Loe19qV4WkMo_xrDmxjwxjsdbxLrnAdqFlZCNMLdD-_A-2X7aFcGeGmySNKxCDMxo614-O430SMLkHXhsjIbIuZ3BSb1aoH9Vq_uQMfQXfmdFFLHiVCxGSMxxIeOTnEU_JF7NucLXh2foKF14B4JKgHRRcowg\",\"not-before-policy\":0,\"session_state\":\"fa6a84e5-8869-40e6-992d-ef0ea7718576\",\"scope\":\"openid profile email address\",\"error\":null,\"error_description\":null,\"error_uri\":null}";

    @BeforeEach
    public void setup() throws URISyntaxException, JsonProcessingException {
        closeable = MockitoAnnotations.openMocks(this);
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.putSingle("Authorization", "True");
        uri = new URI("https://9e56-118-238-7-66.ngrok-free.app/realms/OIdp/custom-attribute/assign");

        doReturn(context).when(session).getContext();
        doReturn(keycloakUriInfo).when(context).getUri();
        doReturn(singleUseObjectProvider).when(session).getProvider(SingleUseObjectProvider.class);
        doReturn(userSessionProvider).when(session).sessions();
        doReturn(userSession).when(userSessionProvider).getUserSession(any(), any());
        doReturn(httpHeaders).when(context).getRequestHeaders();
        doReturn(realm).when(context).getRealm();
        doReturn(headerMap).when(headers).getRequestHeaders();
        doReturn(client).when(realm).getClientByClientId(any());
        doReturn(AuthenticatedClientSessionModel).when(userSession).getAuthenticatedClientSessionByClient(any());
        doReturn(user).when(userSession).getUser();
        doReturn(accessTokenString).when(objectMapper).writeValueAsString(accessTokenResponseBuilder);
        doReturn(false).when(jsonNode).isObject();
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetResource() {
        assertNotNull(customAttributeProvider.getResource());
    }

    @Test
    public void testSetAttributes() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, JsonProcessingException {
        try (
            MockedStatic<AppAuthManager> appAuthManagerStatic = mockStatic(AppAuthManager.class);
            MockedStatic<AuthenticationManager> authenticationManagerStatic = mockStatic(AuthenticationManager.class);
            MockedStatic<DefaultClientSessionContext> defaultClientSessionContextStatic = mockStatic(DefaultClientSessionContext.class);
            MockedConstruction<RealmManager> realmManagerConstruction = mockConstruction(RealmManager.class,
                                                    (mock, ctx) -> {
                                                        doReturn(realm).when(mock).getRealmByName(any());
                                                    });
            MockedConstruction<BearerTokenAuthenticator> bearerTokenAuthenticator = mockConstruction(BearerTokenAuthenticator.class,
                                                    (mock, ctx) -> {
                                                        doReturn(mock).when(mock).setRealm(any());
                                                        doReturn(mock).when(mock).setUriInfo(any());
                                                        doReturn(mock).when(mock).setTokenString(any());
                                                        doReturn(mock).when(mock).setConnection(any());
                                                        doReturn(mock).when(mock).setHeaders(any());
                                                        doReturn(authResult).when(mock).authenticate();
                                                    });
            MockedConstruction<TokenManager> tokenManagerConstruction = mockConstruction(TokenManager.class,
                                                    (mock, ctx) -> {
                                                        doReturn(accessTokenResponseBuilder).when(mock).responseBuilder(any(), any(), any(), any(), any(), any());
                                                        doReturn(accessTokenResponseBuilder).when(accessTokenResponseBuilder).generateAccessToken();
                                                        doReturn(accessTokenResponseBuilder).when(accessTokenResponseBuilder).generateIDToken();
                                                        doReturn(accessTokenResponseBuilder).when(accessTokenResponseBuilder).generateRefreshToken();
                                                        doReturn(accessTokenResponse).when(accessTokenResponseBuilder).build();
                                                        doReturn(token).when(mock).createClientAccessToken(any(), any(), any(), any(), any(), any());
                                                    });
        ) {
            doReturn(connection).when(context).getConnection();
            doReturn(keycloakUriInfo).when(context).getUri();
            doReturn(user).when(authResult).getUser();
            doReturn("sample-client").when(client).getClientId();
            appAuthManagerStatic.when(() -> AppAuthManager.extractAuthorizationHeaderToken(any())).thenReturn("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrMTVBeF8tc0pCVWNlZzRnbDZXajRnVVd3aHFjcjhJUkNsUFFCeFhFTEFnIn0.eyJleHAiOjE2OTUwNDQ4NTIsImlhdCI6MTY5NTA0NDU1MiwiYXV0aF90aW1lIjoxNjk1MDQ0NTUxLCJqdGkiOiI0Zjk0ZWEzNC1jYmE0LTQ5MDEtOGE0ZS1iN2FmYzNmOTI4YzYiLCJpc3MiOiJodHRwczovLzhmZWUtMTE4LTIzOC03LTY5Lm5ncm9rLWZyZWUuYXBwL3JlYWxtcy9PSWRwIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImM5ZTgxNWM4LTRhYzYtNDIwOS1iZTYzLTVlMmY1YzY0NzNkOSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNhbXBsZS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1vaWRwIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgYWRkcmVzcyBlbWFpbCBwcm9maWxlIiwic2lkIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwidW5pcXVlX2lkIjoiNzkxMGFlNWYtYTZjMS00MTE3LWI4OTAtZmMyZGYyZGI2M2YxIiwiYWRkcmVzcyI6e30sImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9hZGRyZXNzIjoi5p2x5Lqs6YO95Y2D5Luj55Sw5Yy65Y2D5Luj55SwMS0xIiwiYmlydGhfZGF0ZSI6IjE5NzAtMDEtMzEiLCJuYW1lIjoi5L2Q6JekIOWkqumDjiIsImdlbmRlcl9jb2RlIjoiMCIsInByZWZlcnJlZF91c2VybmFtZSI6Ijc5MTBhZTVmLWE2YzEtNDExNy1iODkwLWZjMmRmMmRiNjNmMSJ9.LD6Pg76hBFePAP69tzW3Sg1asP89sVZop7NnW9BlH1smHmPn-UifCq2NNmjA7KrEGSuf9rqWp_6lQ_L6osdBxBvu1S1bearjzeME8h6rWSqcyvzrdaVaGZd5Xzchl5ULZfmeNP_X1yQEIHCWvko-03X3yE5gJ3H33JHrAsf7isFo-B_qhZDv77zAr4uNJYt9wzlC7jsq39G0sKrxjGquqG1dJpoOxARVAO0pKzemr2SzzvDJfn2sMch1F-Lz8ZI60qxZ5wXRPPJMjM1WP65oEGuJLRFStDdbM_s9Avue2ErEuPztqyaDFdOhpgm8YKScrYi_mcT5LBY0evOsU-rPKw");
            defaultClientSessionContextStatic.when(() -> DefaultClientSessionContext.fromClientSessionScopeParameter(any(), any())).thenReturn(defaultClientSessionContext);
            assertNotNull(customAttributeProvider.setAttributes("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}"));
            appAuthManagerStatic.verify(() -> AppAuthManager.extractAuthorizationHeaderToken(any()), times(1));
            defaultClientSessionContextStatic.verify(() -> DefaultClientSessionContext.fromClientSessionScopeParameter(any(), any()), times(1));
        }
    }

    @Test
    public void testSetAttributesWithoutRealm() {
        try (
            MockedStatic<AppAuthManager> appAuthManagerStatic = mockStatic(AppAuthManager.class);
            MockedStatic<AuthenticationManager> authenticationManagerStatic = mockStatic(AuthenticationManager.class);
            MockedConstruction<RealmManager> realmManagerConstruction = mockConstruction(RealmManager.class,
                                                    (mock, ctx) -> {
                                                        doReturn(null).when(mock).getRealmByName(any());
                                                    });
            MockedConstruction<BearerTokenAuthenticator> bearerTokenAuthenticator = mockConstruction(BearerTokenAuthenticator.class,
                                                    (mock, ctx) -> {
                                                        doReturn(mock).when(mock).setRealm(any());
                                                        doReturn(mock).when(mock).setUriInfo(any());
                                                        doReturn(mock).when(mock).setTokenString(any());
                                                        doReturn(mock).when(mock).setConnection(any());
                                                        doReturn(mock).when(mock).setHeaders(any());
                                                        doReturn(authResult).when(mock).authenticate();
                                                    });
        ) {
            doReturn(connection).when(context).getConnection();
            doReturn(keycloakUriInfo).when(context).getUri();
            appAuthManagerStatic.when(() -> AppAuthManager.extractAuthorizationHeaderToken(any())).thenReturn("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrMTVBeF8tc0pCVWNlZzRnbDZXajRnVVd3aHFjcjhJUkNsUFFCeFhFTEFnIn0.eyJleHAiOjE2OTUwNDQ4NTIsImlhdCI6MTY5NTA0NDU1MiwiYXV0aF90aW1lIjoxNjk1MDQ0NTUxLCJqdGkiOiI0Zjk0ZWEzNC1jYmE0LTQ5MDEtOGE0ZS1iN2FmYzNmOTI4YzYiLCJpc3MiOiJodHRwczovLzhmZWUtMTE4LTIzOC03LTY5Lm5ncm9rLWZyZWUuYXBwL3JlYWxtcy9PSWRwIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImM5ZTgxNWM4LTRhYzYtNDIwOS1iZTYzLTVlMmY1YzY0NzNkOSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNhbXBsZS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1vaWRwIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgYWRkcmVzcyBlbWFpbCBwcm9maWxlIiwic2lkIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwidW5pcXVlX2lkIjoiNzkxMGFlNWYtYTZjMS00MTE3LWI4OTAtZmMyZGYyZGI2M2YxIiwiYWRkcmVzcyI6e30sImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9hZGRyZXNzIjoi5p2x5Lqs6YO95Y2D5Luj55Sw5Yy65Y2D5Luj55SwMS0xIiwiYmlydGhfZGF0ZSI6IjE5NzAtMDEtMzEiLCJuYW1lIjoi5L2Q6JekIOWkqumDjiIsImdlbmRlcl9jb2RlIjoiMCIsInByZWZlcnJlZF91c2VybmFtZSI6Ijc5MTBhZTVmLWE2YzEtNDExNy1iODkwLWZjMmRmMmRiNjNmMSJ9.LD6Pg76hBFePAP69tzW3Sg1asP89sVZop7NnW9BlH1smHmPn-UifCq2NNmjA7KrEGSuf9rqWp_6lQ_L6osdBxBvu1S1bearjzeME8h6rWSqcyvzrdaVaGZd5Xzchl5ULZfmeNP_X1yQEIHCWvko-03X3yE5gJ3H33JHrAsf7isFo-B_qhZDv77zAr4uNJYt9wzlC7jsq39G0sKrxjGquqG1dJpoOxARVAO0pKzemr2SzzvDJfn2sMch1F-Lz8ZI60qxZ5wXRPPJMjM1WP65oEGuJLRFStDdbM_s9Avue2ErEuPztqyaDFdOhpgm8YKScrYi_mcT5LBY0evOsU-rPKw");
            assertNotNull(customAttributeProvider.setAttributes("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}"));
            appAuthManagerStatic.verify(() -> AppAuthManager.extractAuthorizationHeaderToken(any()), times(1));
        }
    }

    @Test
    public void testSetAttributesWithoutUser() {
        try (
            MockedStatic<AppAuthManager> appAuthManagerStatic = mockStatic(AppAuthManager.class);
            MockedStatic<AuthenticationManager> authenticationManagerStatic = mockStatic(AuthenticationManager.class);
            MockedConstruction<RealmManager> realmManagerConstruction = mockConstruction(RealmManager.class,
                                                    (mock, ctx) -> {
                                                        doReturn(realm).when(mock).getRealmByName(any());
                                                    });
            MockedConstruction<BearerTokenAuthenticator> bearerTokenAuthenticator = mockConstruction(BearerTokenAuthenticator.class,
                                                    (mock, ctx) -> {
                                                        doReturn(mock).when(mock).setRealm(any());
                                                        doReturn(mock).when(mock).setUriInfo(any());
                                                        doReturn(mock).when(mock).setTokenString(any());
                                                        doReturn(mock).when(mock).setConnection(any());
                                                        doReturn(mock).when(mock).setHeaders(any());
                                                        doReturn(authResult).when(mock).authenticate();
                                                    });
        ) {
            doReturn(connection).when(context).getConnection();
            doReturn(keycloakUriInfo).when(context).getUri();
            appAuthManagerStatic.when(() -> AppAuthManager.extractAuthorizationHeaderToken(any())).thenReturn("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrMTVBeF8tc0pCVWNlZzRnbDZXajRnVVd3aHFjcjhJUkNsUFFCeFhFTEFnIn0.eyJleHAiOjE2OTUwNDQ4NTIsImlhdCI6MTY5NTA0NDU1MiwiYXV0aF90aW1lIjoxNjk1MDQ0NTUxLCJqdGkiOiI0Zjk0ZWEzNC1jYmE0LTQ5MDEtOGE0ZS1iN2FmYzNmOTI4YzYiLCJpc3MiOiJodHRwczovLzhmZWUtMTE4LTIzOC03LTY5Lm5ncm9rLWZyZWUuYXBwL3JlYWxtcy9PSWRwIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImM5ZTgxNWM4LTRhYzYtNDIwOS1iZTYzLTVlMmY1YzY0NzNkOSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNhbXBsZS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1vaWRwIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgYWRkcmVzcyBlbWFpbCBwcm9maWxlIiwic2lkIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwidW5pcXVlX2lkIjoiNzkxMGFlNWYtYTZjMS00MTE3LWI4OTAtZmMyZGYyZGI2M2YxIiwiYWRkcmVzcyI6e30sImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9hZGRyZXNzIjoi5p2x5Lqs6YO95Y2D5Luj55Sw5Yy65Y2D5Luj55SwMS0xIiwiYmlydGhfZGF0ZSI6IjE5NzAtMDEtMzEiLCJuYW1lIjoi5L2Q6JekIOWkqumDjiIsImdlbmRlcl9jb2RlIjoiMCIsInByZWZlcnJlZF91c2VybmFtZSI6Ijc5MTBhZTVmLWE2YzEtNDExNy1iODkwLWZjMmRmMmRiNjNmMSJ9.LD6Pg76hBFePAP69tzW3Sg1asP89sVZop7NnW9BlH1smHmPn-UifCq2NNmjA7KrEGSuf9rqWp_6lQ_L6osdBxBvu1S1bearjzeME8h6rWSqcyvzrdaVaGZd5Xzchl5ULZfmeNP_X1yQEIHCWvko-03X3yE5gJ3H33JHrAsf7isFo-B_qhZDv77zAr4uNJYt9wzlC7jsq39G0sKrxjGquqG1dJpoOxARVAO0pKzemr2SzzvDJfn2sMch1F-Lz8ZI60qxZ5wXRPPJMjM1WP65oEGuJLRFStDdbM_s9Avue2ErEuPztqyaDFdOhpgm8YKScrYi_mcT5LBY0evOsU-rPKw");
            assertNotNull(customAttributeProvider.setAttributes("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}"));
            appAuthManagerStatic.verify(() -> AppAuthManager.extractAuthorizationHeaderToken(any()), times(1));
        }
    }

    @Test
    public void testSetAttributesWithInvalidJSON() {
        try (
            MockedStatic<AppAuthManager> appAuthManagerStatic = mockStatic(AppAuthManager.class);
        ) {
            appAuthManagerStatic.when(() -> AppAuthManager.extractAuthorizationHeaderToken(any())).thenReturn("e");
            assertNotNull(customAttributeProvider.setAttributes("{\"user_attributes\"\"service_id\"\"example@example.com\",\"notes\":\"RP1\"}"));
            appAuthManagerStatic.verify(() -> AppAuthManager.extractAuthorizationHeaderToken(any()), times(1));
        }
    }

    @Test
    public void testSetAttributesWithInvalidRequest() throws JsonMappingException, JsonProcessingException {
        try (
            MockedStatic<AppAuthManager> appAuthManagerStatic = mockStatic(AppAuthManager.class);
            MockedConstruction<RealmManager> realmManagerConstruction = mockConstruction(RealmManager.class,
                                                    (mock, ctx) -> {
                                                        doReturn(realm).when(mock).getRealmByName(any());
                                                    });
            MockedConstruction<BearerTokenAuthenticator> bearerTokenAuthenticator = mockConstruction(BearerTokenAuthenticator.class,
                                                    (mock, ctx) -> {
                                                        doReturn(mock).when(mock).setRealm(any());
                                                        doReturn(mock).when(mock).setUriInfo(any());
                                                        doReturn(mock).when(mock).setTokenString(any());
                                                        doReturn(mock).when(mock).setConnection(any());
                                                        doReturn(mock).when(mock).setHeaders(any());
                                                        doReturn(authResult).when(mock).authenticate();
                                                    });
        ) {
            doReturn(user).when(authResult).getUser();
            appAuthManagerStatic.when(() -> AppAuthManager.extractAuthorizationHeaderToken(any())).thenReturn("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrMTVBeF8tc0pCVWNlZzRnbDZXajRnVVd3aHFjcjhJUkNsUFFCeFhFTEFnIn0.eyJleHAiOjE2OTUwNDQ4NTIsImlhdCI6MTY5NTA0NDU1MiwiYXV0aF90aW1lIjoxNjk1MDQ0NTUxLCJqdGkiOiI0Zjk0ZWEzNC1jYmE0LTQ5MDEtOGE0ZS1iN2FmYzNmOTI4YzYiLCJpc3MiOiJodHRwczovLzhmZWUtMTE4LTIzOC03LTY5Lm5ncm9rLWZyZWUuYXBwL3JlYWxtcy9PSWRwIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImM5ZTgxNWM4LTRhYzYtNDIwOS1iZTYzLTVlMmY1YzY0NzNkOSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNhbXBsZS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1vaWRwIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgYWRkcmVzcyBlbWFpbCBwcm9maWxlIiwic2lkIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwidW5pcXVlX2lkIjoiNzkxMGFlNWYtYTZjMS00MTE3LWI4OTAtZmMyZGYyZGI2M2YxIiwiYWRkcmVzcyI6e30sImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9hZGRyZXNzIjoi5p2x5Lqs6YO95Y2D5Luj55Sw5Yy65Y2D5Luj55SwMS0xIiwiYmlydGhfZGF0ZSI6IjE5NzAtMDEtMzEiLCJuYW1lIjoi5L2Q6JekIOWkqumDjiIsImdlbmRlcl9jb2RlIjoiMCIsInByZWZlcnJlZF91c2VybmFtZSI6Ijc5MTBhZTVmLWE2YzEtNDExNy1iODkwLWZjMmRmMmRiNjNmMSJ9.LD6Pg76hBFePAP69tzW3Sg1asP89sVZop7NnW9BlH1smHmPn-UifCq2NNmjA7KrEGSuf9rqWp_6lQ_L6osdBxBvu1S1bearjzeME8h6rWSqcyvzrdaVaGZd5Xzchl5ULZfmeNP_X1yQEIHCWvko-03X3yE5gJ3H33JHrAsf7isFo-B_qhZDv77zAr4uNJYt9wzlC7jsq39G0sKrxjGquqG1dJpoOxARVAO0pKzemr2SzzvDJfn2sMch1F-Lz8ZI60qxZ5wXRPPJMjM1WP65oEGuJLRFStDdbM_s9Avue2ErEuPztqyaDFdOhpgm8YKScrYi_mcT5LBY0evOsU-rPKw");
            assertNotNull(customAttributeProvider.setAttributes("{\"user_attributes\":{\"service_id\"\"example@example.com\",\"notes\"\"RP1\"}}"));
            appAuthManagerStatic.verify(() -> AppAuthManager.extractAuthorizationHeaderToken(any()), times(1));
        }
    }

    @Test
    public void testSetAttributesWithoutAuthResult() {
        try (
            MockedStatic<AppAuthManager> appAuthManagerStatic = mockStatic(AppAuthManager.class);
            MockedConstruction<RealmManager> realmManagerConstruction = mockConstruction(RealmManager.class,
                                                    (mock, ctx) -> {
                                                        doReturn(realm).when(mock).getRealmByName(any());
                                                    });
            MockedConstruction<BearerTokenAuthenticator> bearerTokenAuthenticator = mockConstruction(BearerTokenAuthenticator.class,
                                                    (mock, ctx) -> {
                                                        doReturn(mock).when(mock).setRealm(any());
                                                        doReturn(mock).when(mock).setUriInfo(any());
                                                        doReturn(mock).when(mock).setTokenString(any());
                                                        doReturn(mock).when(mock).setConnection(any());
                                                        doReturn(mock).when(mock).setHeaders(any());
                                                        doReturn(null).when(mock).authenticate();
                                                    });
        ) {
            appAuthManagerStatic.when(() -> AppAuthManager.extractAuthorizationHeaderToken(any())).thenReturn("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJrMTVBeF8tc0pCVWNlZzRnbDZXajRnVVd3aHFjcjhJUkNsUFFCeFhFTEFnIn0.eyJleHAiOjE2OTUwNDQ4NTIsImlhdCI6MTY5NTA0NDU1MiwiYXV0aF90aW1lIjoxNjk1MDQ0NTUxLCJqdGkiOiI0Zjk0ZWEzNC1jYmE0LTQ5MDEtOGE0ZS1iN2FmYzNmOTI4YzYiLCJpc3MiOiJodHRwczovLzhmZWUtMTE4LTIzOC03LTY5Lm5ncm9rLWZyZWUuYXBwL3JlYWxtcy9PSWRwIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImM5ZTgxNWM4LTRhYzYtNDIwOS1iZTYzLTVlMmY1YzY0NzNkOSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNhbXBsZS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1vaWRwIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgYWRkcmVzcyBlbWFpbCBwcm9maWxlIiwic2lkIjoiMGE0ZDU3NjYtZmZjMy00MWVjLTliOWYtNGMxZTYzYzQ5MDVhIiwidW5pcXVlX2lkIjoiNzkxMGFlNWYtYTZjMS00MTE3LWI4OTAtZmMyZGYyZGI2M2YxIiwiYWRkcmVzcyI6e30sImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwidXNlcl9hZGRyZXNzIjoi5p2x5Lqs6YO95Y2D5Luj55Sw5Yy65Y2D5Luj55SwMS0xIiwiYmlydGhfZGF0ZSI6IjE5NzAtMDEtMzEiLCJuYW1lIjoi5L2Q6JekIOWkqumDjiIsImdlbmRlcl9jb2RlIjoiMCIsInByZWZlcnJlZF91c2VybmFtZSI6Ijc5MTBhZTVmLWE2YzEtNDExNy1iODkwLWZjMmRmMmRiNjNmMSJ9.LD6Pg76hBFePAP69tzW3Sg1asP89sVZop7NnW9BlH1smHmPn-UifCq2NNmjA7KrEGSuf9rqWp_6lQ_L6osdBxBvu1S1bearjzeME8h6rWSqcyvzrdaVaGZd5Xzchl5ULZfmeNP_X1yQEIHCWvko-03X3yE5gJ3H33JHrAsf7isFo-B_qhZDv77zAr4uNJYt9wzlC7jsq39G0sKrxjGquqG1dJpoOxARVAO0pKzemr2SzzvDJfn2sMch1F-Lz8ZI60qxZ5wXRPPJMjM1WP65oEGuJLRFStDdbM_s9Avue2ErEuPztqyaDFdOhpgm8YKScrYi_mcT5LBY0evOsU-rPKw");
            assertNotNull(customAttributeProvider.setAttributes("{\"user_attributes\":{\"service_id\":\"example@example.com\",\"notes\":\"RP1\"}}"));
            appAuthManagerStatic.verify(() -> AppAuthManager.extractAuthorizationHeaderToken(any()), times(1));
        }
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> {
            customAttributeProvider.close();
        });
    }
}
