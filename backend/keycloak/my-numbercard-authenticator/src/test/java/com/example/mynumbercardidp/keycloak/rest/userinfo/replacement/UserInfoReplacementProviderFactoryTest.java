package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserInfoReplacementProviderFactoryTest {
    private AutoCloseable closeable;

    @InjectMocks
    UserInfoReplacementProviderFactory userInfoReplacementProviderFactory;

    @Mock
    KeycloakSession session;
    @Mock
    Scope scope;
    @Mock
    KeycloakSessionFactory keycloakSessionFactory;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetId() {
        String expected = "userinfo-replacement";
        String result = userInfoReplacementProviderFactory.getId();
        assertEquals(expected, result);
    }

    @Test
    public void testCreate() {
        var expected = new UserInfoReplacementProvider(session);
        var result = userInfoReplacementProviderFactory.create(session);
        assertEquals(expected instanceof UserInfoReplacementProvider, result instanceof UserInfoReplacementProvider);
    }

    @Test
    public void testInit() {
        assertDoesNotThrow(() -> {
            userInfoReplacementProviderFactory.init(scope);
        });
    }

    @Test
    public void testPostInit() {
        assertDoesNotThrow(() -> {
            userInfoReplacementProviderFactory.postInit(keycloakSessionFactory);
        });
    }

    @Test
    public void testClose() {
        assertDoesNotThrow(() -> {
            userInfoReplacementProviderFactory.close();
        });
    }
}
