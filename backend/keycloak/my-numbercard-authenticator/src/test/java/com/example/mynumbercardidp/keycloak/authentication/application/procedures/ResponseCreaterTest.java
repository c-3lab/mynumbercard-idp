package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.ActionType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public final class ResponseCreaterTest {
    private AutoCloseable closeable;

    @InjectMocks
    AuthenticationExecutionModel authenticationExecutionModel;

    @Mock
    AuthenticationFlowContext context;
    @Mock
    AuthenticationSessionModel authenticationSessionModel;
    @Mock
    LoginFormsProvider form;
    @Mock
    Response.Status status;
    @Mock
    Response response;
    @Mock
    URI actionURLValue;
    @Mock
    ResponseBuilder responseBuilder;
    @Mock
    URI uri;
    @Mock
    URI refreshUrl;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        uri = URI.create("https://9e56-118-238-7-66.ngrok-free.app/realms/OIdp/login-actions/authenticate?session_code=b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI&execution=19ca82cd-d3ab-44ab-8732-ac3af3656c6a&client_id=sample-client&tab_id=n2zdEv2oWSQ&auth_session_id=df4fafea-fc51-4037-999e-2546c66ff913");
        authenticationExecutionModel.setId("19ca82cd-d3ab-44ab-8732-ac3af3656c6a");
        actionURLValue = URI.create("https://9e56-118-238-7-66.ngrok-free.app/realms/OIdp/login-actions/authenticate?session_code=b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI&execution=19ca82cd-d3ab-44ab-8732-ac3af3656c6a&client_id=sample-client&tab_id=n2zdEv2oWSQ&auth_session_id=df4fafea-fc51-4037-999e-2546c66ff913");
        status = Response.Status.ACCEPTED;
        response = Response.ok().build();
        doReturn(authenticationSessionModel).when(context).getAuthenticationSession();
        doReturn("true").when(authenticationSessionModel).getAuthNote("reuseNonceFlag");
        doReturn(uri).when(context).getActionUrl(anyString());
        doReturn(form).when(context).form();
        doReturn(form).when(form).setActionUri(any());
        doReturn(form).when(form).setExecution(anyString());
        doReturn(form).when(form).setResponseHeader(anyString(), anyString());
        doReturn(form).when(form).setAttribute(anyString(), anyString());
        doReturn(response).when(form).createLoginUsernamePassword();
        doReturn(authenticationExecutionModel).when(context).getExecution();
        doReturn("b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI").when(context).generateAccessCode();
        doReturn(refreshUrl).when(context).getRefreshUrl(anyBoolean());
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testCreateChallengePageWithActionName() {
        assertDoesNotThrow(() -> {
            ResponseCreater.createChallengePage(context, ActionType.LOGIN.getName());
        });
    }

    @Test
    public void testCreateChallengePageWithStatus() {
        assertDoesNotThrow(() -> {
            ResponseCreater.createChallengePage(context, status);
        });
    }

    @Test
    public void testCreateChallengePage() {
        assertDoesNotThrow(() -> {
            ResponseCreater.createChallengePage(context, null, null, status);
        });
    }

    @Test
    public void testCreateChallengePageWithError() {
        assertDoesNotThrow(() -> {
            ResponseCreater.createChallengePage(context, "Error Message", null, status);
        });
    }

    @Test
    public void testCreateChallengePageWithErrorAndField() {
        assertDoesNotThrow(() -> {
            ResponseCreater.createChallengePage(context, "Error Message", "Test Field", status);
        });
    }

    @Test
    public void testSendChallengeResponse() {
        assertDoesNotThrow(() -> {
            ResponseCreater.sendChallengeResponse(context, "login", status);
        });
    }

    @Test
    public void testSendChallengeResponseWithoutReuseNonceFlag() {
        Mockito.when(context.getAuthenticationSession().getAuthNote("reuseNonceFlag")).thenReturn("false");
        assertDoesNotThrow(() -> {
            ResponseCreater.sendChallengeResponse(context, "login", status);
        });
    }

    @Test
    public void testSendInvalidRequestResponse() {
        assertDoesNotThrow(() -> {
            ResponseCreater.sendInvalidRequestResponse(context, status);
        });
    }

    @Test
    public void testSetLoginFormAttributes() {
        assertDoesNotThrow(() -> {
            ResponseCreater.setLoginFormAttributes(context);
        });
    }
}
