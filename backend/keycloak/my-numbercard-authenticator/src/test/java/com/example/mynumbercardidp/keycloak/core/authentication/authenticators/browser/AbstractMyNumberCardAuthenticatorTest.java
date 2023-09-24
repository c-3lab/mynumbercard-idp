package com.example.mynumbercardidp.keycloak.core.authentication.authenticators.browser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.AfterEach;

public class AbstractMyNumberCardAuthenticatorTest {
    private AutoCloseable closeable;

    // 抽象クラステストの為、ダミーの実装クラスを作成
    public class ConcreteImpl extends AbstractMyNumberCardAuthenticator {}

    @InjectMocks
    ConcreteImpl concreteImpl = new ConcreteImpl();

    @Mock
    AuthenticationFlowContext context;
    @Mock
    KeycloakSession session;
    @Mock
    RealmModel realm;
    @Mock
    UserModel user;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testAuthenticate() {
        assertDoesNotThrow(() -> {
            concreteImpl.authenticate(context);
        });
    }

    @Test
    public void testAction() {
        assertDoesNotThrow(() -> {
            concreteImpl.action(context);
        });
    }

    @Test
    public void testRequiresUser() {
        assertFalse(concreteImpl.requiresUser());
    }

    @Test
    public void testConfiguredFor() {
        assertTrue(concreteImpl.configuredFor(session, realm, user));
    }

    @Test
    public void testSetRequiredActions() {
        assertDoesNotThrow(() -> {
            concreteImpl.setRequiredActions(session, realm, user);
        });
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> {
            concreteImpl.close();
        });
    }
}
