package com.example.mynumbercardidp.keycloak.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CustomAttributeProviderFactoryTest {

    CustomAttributeProviderFactory customAttributeProviderFactory = new CustomAttributeProviderFactory();
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
    public void testGetId() {
        assertNotNull(customAttributeProviderFactory.getId());
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> {
            customAttributeProviderFactory.close();
        });
    }

    @Test
    public void testCreate() {
        assertNotNull(customAttributeProviderFactory.create(session));
    }

    @Test
    public void testInit() {
        assertDoesNotThrow(() -> {
            customAttributeProviderFactory.init(config);
        });
    }

    @Test
    public void testPostInit() {
        assertDoesNotThrow(() -> {
            customAttributeProviderFactory.postInit(factory);
        });
    }
}