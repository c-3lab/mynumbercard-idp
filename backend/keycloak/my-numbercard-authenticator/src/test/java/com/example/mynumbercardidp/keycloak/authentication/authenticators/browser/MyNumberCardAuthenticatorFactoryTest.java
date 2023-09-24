package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.AfterEach;

public class MyNumberCardAuthenticatorFactoryTest {
    private AutoCloseable closeable;

    @InjectMocks
    MyNumberCardAuthenticatorFactory myNumberCardAuthenticatorFactory;

    @Mock
    KeycloakSession session;
    @Mock
    Scope config;
    @Mock
    KeycloakSessionFactory factory;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> {
            myNumberCardAuthenticatorFactory.close();
        });
    }

    @Test
    public void testCreate() {
        assertNotNull(myNumberCardAuthenticatorFactory.create(session));
    }

    @Test
    public void testGetId() {
        assertNotNull(myNumberCardAuthenticatorFactory.getId());
    }

    @Test
    public void testInit() {
        assertDoesNotThrow(() -> {
            myNumberCardAuthenticatorFactory.init(config);
        });
    }

    @Test
    public void testPostInit() {
        assertDoesNotThrow(() -> {
            myNumberCardAuthenticatorFactory.postInit(factory);
        });
    }

    @Test
    public void testGetDisplayType() {
        assertNotNull(myNumberCardAuthenticatorFactory.getDisplayType());
    }

    @Test
    public void testGetReferenceCategory() {
        assertNotNull(myNumberCardAuthenticatorFactory.getReferenceCategory());
    }

    @Test
    public void testGetRequirementChoices() {
        assertNotNull(myNumberCardAuthenticatorFactory.getRequirementChoices());
    }

    @Test
    public void testIsConfigurable() {
        assertTrue(myNumberCardAuthenticatorFactory.isConfigurable());
    }

    @Test
    public void testIsUserSetupAllowed() {
        assertFalse(myNumberCardAuthenticatorFactory.isUserSetupAllowed());
    }

    @Test
    public void testGetConfigProperties() {
        assertNotNull(myNumberCardAuthenticatorFactory.getConfigProperties());
    }

    @Test
    public void testGetHelpText() {
        assertNotNull(myNumberCardAuthenticatorFactory.getHelpText());
    }
}
