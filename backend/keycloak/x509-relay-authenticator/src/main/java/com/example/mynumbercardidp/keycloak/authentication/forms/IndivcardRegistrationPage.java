package com.example.mynumbercardidp.keycloak.authentication.forms;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAuthenticator;
import org.keycloak.authentication.FormAuthenticatorFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class IndivcardRegistrationPage implements FormAuthenticator, FormAuthenticatorFactory {

    public static final String PROVIDER_ID = "registration-page-form";

    private static final Logger consoleLogger = Logger.getLogger(IndivcardRegistrationPage.class);
    private static List<String> screenLayouts;
    private static final String SCREEN_LAYOUT_LINK = "link";
    private static final String SCREEN_LAYOUT_EMBEDDED = "embedded";
    private static final String HTTP_PARAMETER_AGREE_TERMS_OF_USE = "agree-tos";
    private static final String HTTP_PARAMETER_AGREE_PRIVATE_POLICY = "agree-pp";

    private static final List<ProviderConfigProperty> configProperties =
        new ArrayList<ProviderConfigProperty>();

    static {
        screenLayouts = new ArrayList();
        screenLayouts.add(SCREEN_LAYOUT_LINK);
        screenLayouts.add(SCREEN_LAYOUT_EMBEDDED);

        ProviderConfigProperty selectLayoutProperty = new ProviderConfigProperty();
        selectLayoutProperty.setName("consent-screen.screen-layout-mode");
        selectLayoutProperty.setLabel("Screen layout");
        selectLayoutProperty.setHelpText(
            "This setting can to change consent screen layout. \n" +
            "\"Link\" is hyperlink to consent page and privacy policy. \n" +
            "\"Embedded\" embeds the consent page and privacy policy content in the page."
        );
        selectLayoutProperty.setType(ProviderConfigProperty.LIST_TYPE);
        selectLayoutProperty.setOptions(screenLayouts);
        selectLayoutProperty.setDefaultValue(SCREEN_LAYOUT_LINK);
        configProperties.add(selectLayoutProperty);

        ProviderConfigProperty TermsOfUseUrlProperty = new ProviderConfigProperty();
        TermsOfUseUrlProperty.setName("consent-screen.terms-of-use-url");
        TermsOfUseUrlProperty.setLabel("Terms of use URL");
        TermsOfUseUrlProperty.setHelpText("\"Link\" mode only. Terms of use URL.");
        TermsOfUseUrlProperty.setType(ProviderConfigProperty.STRING_TYPE);
        TermsOfUseUrlProperty.setDefaultValue("#");
        configProperties.add(TermsOfUseUrlProperty);

        ProviderConfigProperty privacyPolicyUrlProperty = new ProviderConfigProperty();
        privacyPolicyUrlProperty.setName("consent-screen.privacy-policy-url");
        privacyPolicyUrlProperty.setLabel("Privacy policy URL");
        privacyPolicyUrlProperty.setHelpText("\"Link\" mode only. Privacy policy URL.");
        privacyPolicyUrlProperty.setType(ProviderConfigProperty.STRING_TYPE);
        privacyPolicyUrlProperty.setDefaultValue("#");
        configProperties.add(privacyPolicyUrlProperty);

        ProviderConfigProperty TermsOfUseTextProperty = new ProviderConfigProperty();
        TermsOfUseTextProperty.setName("consent-screen.terms-of-use-text");
        TermsOfUseTextProperty.setLabel("Terms of use contents");
        TermsOfUseTextProperty.setHelpText("\"Embedded\" mode only. Terms of use contents.");
        TermsOfUseTextProperty.setType(ProviderConfigProperty.TEXT_TYPE);
        configProperties.add(TermsOfUseTextProperty);

        ProviderConfigProperty privacyPolicyTextProperty = new ProviderConfigProperty();
        privacyPolicyTextProperty.setName("consent-screen.privacy-policy-text");
        privacyPolicyTextProperty.setLabel("Terms of use contents");
        privacyPolicyTextProperty.setHelpText("\"Embedded\" mode only. Privacy policy contents.");
        privacyPolicyTextProperty.setType(ProviderConfigProperty.TEXT_TYPE);
        configProperties.add(privacyPolicyTextProperty);
    }

    @Override
    public Response render(FormContext context, LoginFormsProvider form) {
        consoleLogger.info("called: render");
        consoleLogger.info("context.getAuthenticator(): " + context.getExecution().getAuthenticator());
        consoleLogger.info("context.getExecution(): " + context.getExecution().getFlowId());
        consoleLogger.info("context.getId(): " + context.getExecution().getId());
        return form.createRegistration();
    }

    @Override
    public void close() {

    }

    @Override
    public String getDisplayType() {
        return "Indivcard Registration Page";
    }

    @Override
    public String getHelpText() {
        return "This is the controller for the indivcard registration page";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public FormAuthenticator create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
