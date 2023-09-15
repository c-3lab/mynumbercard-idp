package com.example.mynumbercardidp.keycloak.util.authentication;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.SpiConfigProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

public class CurrentConfigTest {

    @Mock
    AuthenticationFlowContext context;
    @Mock
    AuthenticatorConfigModel authenticatorConfig;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        Map<String, String> config = Map.of(
            "my-num-cd-auth.platform-class", "PlatformApiClientClassFqdn",
            "NAME", "my-num-cd-auth.platform-class",
            "LABEL", "Platform API Client Class FQDN",
            "HELP_TEXT", "Fully qualified class name of the platform API client for authentication using the public personal authentication information from a My Number Card.",
            "TYPE", "String",
            "DEFAULT_VALUE", "com.example.mynumbercardidp.keycloak.network.platform.PlatformApiClient"
        );
        Mockito.when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfig);
        Mockito.when(authenticatorConfig.getConfig()).thenReturn(config);
    }

    @Test
    public void testGetValue() {
        assertEquals("", CurrentConfig.getValue(context, "TestConfig"));
        assertNotNull(CurrentConfig.getValue(context, SpiConfigProperty.PlatformApiClientClassFqdn.CONFIG.getName()));
    }
}
