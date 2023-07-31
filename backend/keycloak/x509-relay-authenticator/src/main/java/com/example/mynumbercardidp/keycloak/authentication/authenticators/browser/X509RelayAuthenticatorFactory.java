package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class X509RelayAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "x509-relay-authenticator";
    private static final String DISPLAY_TYPE = "X509 Relay Authenticator";
    private static final String HELP_TEXT = "Relay validates a X509 file from login form.";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
      AuthenticationExecutionModel.Requirement.REQUIRED
    };

    public static final X509RelayAuthenticator SINGLETON = new X509RelayAuthenticator();

    @Override
    public void close() {
      // Nothing to do
    }

    @Override
    public Authenticator create(KeycloakSession session) {
      return SINGLETON;
    }

    @Override
    public String getId() {
      return PROVIDER_ID;
    }

    @Override
    public void init(Scope scope) {
      // Nothing to do
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
      // Nothing to do
    }

    @Override
    public String getDisplayType() {
      return DISPLAY_TYPE;
    }

    @Override
    public String getReferenceCategory() {
      return PasswordCredentialModel.TYPE;
    }

    @Override
    public Requirement[] getRequirementChoices() {
      return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isConfigurable() {
      return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
      return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty debugModeProperty = new ProviderConfigProperty();
        debugModeProperty.setName("x509-relay-auth.debug-mode");
        debugModeProperty.setLabel("Enable debug mode");
        debugModeProperty.setHelpText("Print javascript debug log to browser console, and login form screen.");
        debugModeProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        debugModeProperty.setDefaultValue(false);
        configProperties.add(debugModeProperty);

        ProviderConfigProperty CertVaildatorUriProperty = new ProviderConfigProperty();
        CertVaildatorUriProperty.setName("x509-relay-auth.certificate-validator-uri");
        CertVaildatorUriProperty.setLabel("Certificate Vaildator URI");
        CertVaildatorUriProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(CertVaildatorUriProperty);

        ProviderConfigProperty androidAppUriProperty = new ProviderConfigProperty();
        androidAppUriProperty.setName("x509-relay-auth.android-app-uri");
        androidAppUriProperty.setLabel("Run URI of Android application");
        androidAppUriProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(androidAppUriProperty);

        ProviderConfigProperty iosAppUriProperty = new ProviderConfigProperty();
        iosAppUriProperty.setName("x509-relay-auth.ios-app-uri");
        iosAppUriProperty.setLabel("Run URI of iOS application");
        iosAppUriProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(iosAppUriProperty);

        ProviderConfigProperty appUriProperty = new ProviderConfigProperty();
        appUriProperty.setName("x509-relay-auth.app-uri");
        appUriProperty.setLabel("Installation URI of Android/iOS application");
        appUriProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(appUriProperty);
    }

    @Override
    public String getHelpText() {
      return HELP_TEXT;
    }
}
