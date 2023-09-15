package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.junit.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.Before;

public class SpiConfigPropertyTest {

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
    public void testGetPropertis() {
        assertNotNull(SpiConfigProperty.getPropertis());
    }

    @Test
    public void testGetFreeMarkerJavaTemplateVariables() {
        assertNotNull(SpiConfigProperty.getFreeMarkerJavaTemplateVariables());
    }

    @Test
    public void testInitFreeMarkerJavaTemplateVariables() {
        assertNotNull(new SpiConfigProperty.DebugMode());
        assertNotNull(new SpiConfigProperty.CertificateValidatorRootUri());
        assertNotNull(new SpiConfigProperty.RunUriOfAndroidApplication());
        assertNotNull(new SpiConfigProperty.RunUriOfiOSApplication());
        assertNotNull(new SpiConfigProperty.InstallationUriOfSmartPhoneApplication());
        assertNotNull(new SpiConfigProperty.PlatformApiClientClassFqdn());
        assertNotNull(new SpiConfigProperty.PlatformApiIdpSender());
        assertNotNull(new SpiConfigProperty.TermsOfUseDirURL());
        assertNotNull(new SpiConfigProperty.PrivacyPolicyDirURL());
        assertNotNull(new SpiConfigProperty.PersonalDataProtectionPolicyDirURL());
        assertDoesNotThrow(() -> {
            SpiConfigProperty.initFreeMarkerJavaTemplateVariables(context);
        });
    }
}
