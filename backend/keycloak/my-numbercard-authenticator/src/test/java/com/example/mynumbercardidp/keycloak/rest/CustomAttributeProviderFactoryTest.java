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
    public void getId() {
        assertNotNull(customAttributeProviderFactory.getId());
    }

    @Test
    public void close() {
        assertDoesNotThrow(() -> {
            customAttributeProviderFactory.close();
        });
    }

    @Test
    public void create() {
        assertNotNull(customAttributeProviderFactory.create(session));
    }

    @Test
    public void init() {
        assertDoesNotThrow(() -> {
            customAttributeProviderFactory.init(config);
        });
    }

    @Test
    public void postInit() {
        assertDoesNotThrow(() -> {
            customAttributeProviderFactory.postInit(factory);
        });
    }
}