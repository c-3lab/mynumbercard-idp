package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.FlowStatus;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.ActionResolver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Field;
import java.net.URI;

import javax.ws.rs.core.Response;

public class MyNumberCardAuthenticatorTest {
    private AutoCloseable closeable;
    AuthenticationExecutionModel authenticationExecutionModel = new AuthenticationExecutionModel();

    @InjectMocks
    MyNumberCardAuthenticator myNumberCardAuthenticator;

    @Mock
    AuthenticationFlowContext context;
    @Mock
    AuthenticatorConfigModel authenticatorConfig;
    @Mock
    AuthenticationSessionModel authenticationSessionModel;
    @Mock
    LoginFormsProvider form;
    @Mock
    Response response;
    @Mock
    URI uri;
    @Mock
    URI refreshUrl;
    @Mock
    FlowStatus flowStatus;
    @Mock
    ActionResolver actionResolver;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        uri = URI.create("https://9e56-118-238-7-66.ngrok-free.app/realms/OIdp/login-actions/authenticate?session_code=b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI&execution=19ca82cd-d3ab-44ab-8732-ac3af3656c6a&client_id=sample-client&tab_id=n2zdEv2oWSQ&auth_session_id=df4fafea-fc51-4037-999e-2546c66ff913");
        authenticationExecutionModel.setId("19ca82cd-d3ab-44ab-8732-ac3af3656c6a");
        refreshUrl = URI.create("https://9e56-118-238-7-66.ngrok-free.app/realms/OIdp/login-actions/authenticate?client_id=sample-client&tab_id=3tSMeWfeVcQ&auth_session_id=204abe79-3551-4fe2-9800-a645615a2bc0");
        response = Response.ok().build();

        doReturn(authenticatorConfig).when(context).getAuthenticatorConfig();
        doReturn(authenticationSessionModel).when(context).getAuthenticationSession();
        doReturn(authenticationExecutionModel).when(context).getExecution();
        doReturn("b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI").when(context).generateAccessCode();
        doReturn(refreshUrl).when(context).getRefreshUrl(anyBoolean());
        doReturn(flowStatus).when(context).getStatus();
        doReturn(uri).when(context).getActionUrl(anyString());
        doReturn(form).when(context).form();
        doReturn(form).when(form).setActionUri(any());
        doReturn(form).when(form).setExecution(anyString());
        doReturn(form).when(form).setResponseHeader(anyString(), anyString());
        doReturn(response).when(form).createLoginUsernamePassword();
        doReturn(form).when(form).setActionUri(any());
        doNothing().when(actionResolver).executeUserAction(any());
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testAction() throws Exception {
        try (
            MockedStatic<SpiConfigProperty> spiConfigPropertyStatic = mockStatic(SpiConfigProperty.class);
        ) {
            Field actionResolverField = myNumberCardAuthenticator.getClass().getDeclaredField("actionResolver");
            actionResolverField.setAccessible(true);
            actionResolverField.set(myNumberCardAuthenticator, actionResolver);
            spiConfigPropertyStatic.when(() -> SpiConfigProperty.initFreeMarkerJavaTemplateVariables(any())).thenAnswer((Answer<Void>) invocation -> null);

            myNumberCardAuthenticator.action(context);

            verify(actionResolver, times(1)).executeUserAction(any());
            verify(context, times(1)).getStatus();
            spiConfigPropertyStatic.verify(() -> SpiConfigProperty.initFreeMarkerJavaTemplateVariables(any()), times(1));
        }
    }

    @Test
    public void authenticate() {
        assertDoesNotThrow(() -> {
            myNumberCardAuthenticator.authenticate(context);
        });
    }
}
