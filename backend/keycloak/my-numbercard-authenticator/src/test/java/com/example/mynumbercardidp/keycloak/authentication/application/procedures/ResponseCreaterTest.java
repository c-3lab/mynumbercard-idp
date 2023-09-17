package com.example.mynumbercardidp.keycloak.authentication.application.procedures;

import com.example.mynumbercardidp.keycloak.authentication.application.procedures.user.ActionType;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public final class ResponseCreaterTest {

    AuthenticationExecutionModel authenticationExecutionModel = new AuthenticationExecutionModel();

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

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        uri = URI.create("https://8fee-118-238-7-69.ngrok-free.app/realms/OIdp/login-actions/authenticate?session_code=b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI&execution=19ca82cd-d3ab-44ab-8732-ac3af3656c6a&client_id=sample-client&tab_id=n2zdEv2oWSQ&auth_session_id=df4fafea-fc51-4037-999e-2546c66ff913");
        authenticationExecutionModel.setId("19ca82cd-d3ab-44ab-8732-ac3af3656c6a");
        actionURLValue = URI.create("https://8fee-118-238-7-69.ngrok-free.app/realms/OIdp/login-actions/authenticate?session_code=b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI&execution=19ca82cd-d3ab-44ab-8732-ac3af3656c6a&client_id=sample-client&tab_id=n2zdEv2oWSQ&auth_session_id=df4fafea-fc51-4037-999e-2546c66ff913");
        status = Response.Status.ACCEPTED;
        response = Response.ok().build();
        Mockito.when(context.getAuthenticationSession()).thenReturn(authenticationSessionModel);
        Mockito.when(context.getAuthenticationSession().getAuthNote("reuseNonceFlag")).thenReturn("true");
        Mockito.when(context.getActionUrl("b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI")).thenReturn(uri);
        Mockito.when(context.form()).thenReturn(form);
        Mockito.when(context.form().setActionUri(uri)).thenReturn(form);
        Mockito.when(context.getExecution()).thenReturn(authenticationExecutionModel);
        Mockito.when(context.form().setActionUri(uri).setExecution("19ca82cd-d3ab-44ab-8732-ac3af3656c6a")).thenReturn(form);
        Mockito.when(context.form().setActionUri(uri).setExecution("19ca82cd-d3ab-44ab-8732-ac3af3656c6a").setResponseHeader("X-Action-URL", actionURLValue.toString())).thenReturn(form);
        Mockito.when(context.form().setActionUri(actionURLValue).setExecution("19ca82cd-d3ab-44ab-8732-ac3af3656c6a").setResponseHeader("X-Action-URL", actionURLValue.toString())).thenReturn(form);
        Mockito.when(context.generateAccessCode()).thenReturn("b3APQO3L41gM9yX8p8ovcA2SqwWJ8prkVi9ZIdKnlhI");
        Mockito.when(context.getRefreshUrl(true)).thenReturn(refreshUrl);
        Mockito.when(form.createLoginUsernamePassword()).thenReturn(response);
        Mockito.when(form.setAttribute("refreshUrl", context.getRefreshUrl(true).toString())).thenReturn(form);
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
