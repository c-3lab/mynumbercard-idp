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

public class MyNumberCardAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "my-mumber-card-authenticator";
    private static final String DISPLAY_TYPE = "My number card Authenticator";
    private static final String HELP_TEXT = "Send public personal authentication part in my number card at platform, validates.";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
      AuthenticationExecutionModel.Requirement.REQUIRED
    };
    private static final List<ProviderConfigProperty> configProperties = SpiConfigProperty.getPropertis();
    public static final MyNumberCardAuthenticator SINGLETON = new MyNumberCardAuthenticator();

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

    @Override
    public String getHelpText() {
      return HELP_TEXT;
    }
}
