package com.example.mynumbercardidp.keycloak.util.authentication;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

public class CurrentConfigTest {
    private AutoCloseable closeable;

    @Mock
    AuthenticationFlowContext context;
    @Mock
    AuthenticatorConfigModel authenticatorConfig;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        Map<String, String> config = Map.of(
            "my-num-cd-auth.platform-class", "PlatformApiClientClassFqdn",
            "NAME", "my-num-cd-auth.platform-class",
            "LABEL", "Platform API Client Class FQDN",
            "HELP_TEXT", "Fully qualified class name of the platform API client for authentication using the public personal authentication information from a My Number Card.",
            "TYPE", "String",
            "DEFAULT_VALUE", "com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient"
        );
        doReturn(authenticatorConfig).when(context).getAuthenticatorConfig();
        doReturn(config).when(authenticatorConfig).getConfig();
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetValue() {
        assertEquals("", CurrentConfig.getValue(context, "TestConfig"));
        assertNotNull(CurrentConfig.getValue(context, SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName()));
    }
}
