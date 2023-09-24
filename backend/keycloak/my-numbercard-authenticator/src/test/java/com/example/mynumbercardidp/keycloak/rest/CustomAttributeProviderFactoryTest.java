package com.example.mynumbercardidp.keycloak.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CustomAttributeProviderFactoryTest {
    private AutoCloseable closeable;

    @InjectMocks
    CustomAttributeProviderFactory customAttributeProviderFactory;
    
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