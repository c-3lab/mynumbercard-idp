package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class MyNumberCardAuthenticatorFactoryTest {

    MyNumberCardAuthenticatorFactory myNumberCardAuthenticatorFactory = new MyNumberCardAuthenticatorFactory();
    @Mock
    KeycloakSession session;
    @Mock
    Scope config;
    @Mock
    KeycloakSessionFactory factory;
    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void close() {
        assertDoesNotThrow(() -> {
            myNumberCardAuthenticatorFactory.close();
        });
    }

    @Test
    public void create() {
        assertNotNull(myNumberCardAuthenticatorFactory.create(session));
    }

    @Test
    public void getId() {
        assertNotNull(myNumberCardAuthenticatorFactory.getId());
    }

    @Test
    public void init() {
        assertDoesNotThrow(() -> {
            myNumberCardAuthenticatorFactory.init(config);
        });
    }

    @Test
    public void postInit() {
        assertDoesNotThrow(() -> {
            myNumberCardAuthenticatorFactory.postInit(factory);
        });
    }

    @Test
    public void getDisplayType() {
       assertNotNull(myNumberCardAuthenticatorFactory.getDisplayType());
    }

    @Test
    public void getReferenceCategory() {
        assertNotNull(myNumberCardAuthenticatorFactory.getReferenceCategory());
    }

    @Test
    public void getRequirementChoices() {
        assertNotNull(myNumberCardAuthenticatorFactory.getRequirementChoices());
    }

    @Test
    public void isConfigurable() {
        assertTrue(myNumberCardAuthenticatorFactory.isConfigurable());
    }

    @Test
    public void isUserSetupAllowed() {
        assertFalse(myNumberCardAuthenticatorFactory.isUserSetupAllowed());
    }

    @Test
    public void getConfigProperties() {
        assertNotNull(myNumberCardAuthenticatorFactory.getConfigProperties());
    }

    @Test
    public void getHelpText() {
        assertNotNull(myNumberCardAuthenticatorFactory.getHelpText());
    }
}
