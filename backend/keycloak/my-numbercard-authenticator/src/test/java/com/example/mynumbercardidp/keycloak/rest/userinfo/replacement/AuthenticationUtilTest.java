package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AuthenticationUtilTest {
    private AutoCloseable closeable;

    @InjectMocks
    AuthenticationUtil authenticationUtil;

    @Mock
    RealmModel realmModel;

    @Mock
    ClientModel clientModel;
    @Mock
    AuthenticationFlowModel authenticationFlowModel;
    @Mock
    Stream<AuthenticationExecutionModel> authenticationExecutionStream;
    @Mock
    Optional<AuthenticationExecutionModel> authenticationExecutionOptional;
    @Mock
    AuthenticationExecutionModel authenticationExecutionModel;

    @BeforeEach
    public void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testFindAuthenticationExecutionModel() {

        doReturn(authenticationFlowModel).when(realmModel).getBrowserFlow();
        doReturn(authenticationExecutionStream).when(realmModel).getAuthenticationExecutionsStream(any());
        doReturn("xxxxxxxxxx").when(authenticationFlowModel).getId();
        doReturn(authenticationExecutionStream).when(authenticationExecutionStream).filter(any());
        doReturn(authenticationExecutionOptional).when(authenticationExecutionStream).findFirst();

        AuthenticationUtil.findAuthenticationExecutionModel(realmModel, clientModel, "aaaaaaaaa");

    }
}
