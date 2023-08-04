package com.example.mynumbercardidp.keycloak.authentication.forms;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.events.EventType;
import org.keycloak.events.Details;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.sessions.CommonClientSessionModel.ExecutionStatus;
import org.keycloak.theme.Theme;
import org.keycloak.models.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;


public class ConsentScreen implements FormAction, FormActionFactory {
    public static final String PROVIDER_ID = "consent-screen";

    private static final Logger consoleLogger = Logger.getLogger(ConsentScreen.class);
    private static List<String> screenLayouts;
    private static final String SCREEN_LAYOUT_LINK = "link";
    private static final String SCREEN_LAYOUT_EMBEDDED = "embedded";
    private static final String HTTP_PARAMETER_AGREE_TERMS_OF_USE = "agree-tos";
    private static final String HTTP_PARAMETER_AGREE_PRIVATE_POLICY = "agree-pp";
    private static String defaultThemaName;

    @Override
    public String getHelpText() {
        return "View terms of use and privacy policy information.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

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
    public void validate(org.keycloak.authentication.ValidationContext context) {
        consoleLogger.info("Called: validate");
        MultivaluedMap<String, String> formData =
            context.getHttpRequest().getDecodedFormParameters();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        consoleLogger.info("TOU: " + formData.getFirst(HTTP_PARAMETER_AGREE_TERMS_OF_USE));
        consoleLogger.info("PP: " + formData.getFirst(HTTP_PARAMETER_AGREE_PRIVATE_POLICY));
        if ((!"on".equals(formData.getFirst(HTTP_PARAMETER_AGREE_TERMS_OF_USE)) &&
                !"true".equals(formData.getFirst(HTTP_PARAMETER_AGREE_TERMS_OF_USE))) ||
            (!"on".equals(formData.getFirst(HTTP_PARAMETER_AGREE_PRIVATE_POLICY)) &&
                !"true".equals(formData.getFirst(HTTP_PARAMETER_AGREE_PRIVATE_POLICY)))) {
            consoleLogger.warn("process: error (Agree parameter is empty.)");
            
            List<FormMessage> errors = new ArrayList();
            errors.add(new FormMessage("Not agree"));

            context.validationError(formData, errors);
            context.getEvent().error("Not agree");
            context.getEvent().event(EventType.REGISTER_ERROR);

            String currentAuthenticator = context.getExecution().getAuthenticator();
            consoleLogger.info("currentAuthenticator: " + currentAuthenticator);
            context.getAuthenticationSession()
                .setExecutionStatus(currentAuthenticator, ExecutionStatus.ATTEMPTED);

        } else {
            context.success();
        }
    }

    @Override
    public void success(FormContext context) {
        consoleLogger.info("Called: success");
        context.getEvent().success();
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        consoleLogger.info("Called: buildPage");

        String screenLayout      = context.getAuthenticatorConfig().getConfig()
                                   .get("consent-screen.screen-layout-mode")
                                   .toLowerCase();
        String termsOfUseUrl     = context.getAuthenticatorConfig().getConfig()
                                   .get("consent-screen.terms-of-use-url");
        String privacyPolicyUrl  = context.getAuthenticatorConfig().getConfig()
                                   .get("consent-screen.privacy-policy-url");
        String termsOfUseText    = context.getAuthenticatorConfig().getConfig()
                                   .get("consent-screen.terms-of-use-text");
        String privacyPolicyText = context.getAuthenticatorConfig().getConfig()
                                   .get("consent-screen.privacy-policy-text");

        try {
            form.setAttribute("screenLayout", screenLayout);
        } catch (java.lang.NullPointerException e) {
            form.setAttribute("screenLayout", SCREEN_LAYOUT_LINK);
        }
        try {
            form.setAttribute("termsOfUseUrl", termsOfUseUrl);
        } catch (java.lang.NullPointerException e) {
            form.setAttribute("termsOfUseUrl", "#");
        }
        try {
            form.setAttribute("privacyPolicyUrl", privacyPolicyUrl);
        } catch (java.lang.NullPointerException e) {
            form.setAttribute("privacyPolicyUrl", "#");
        }
        try {
            form.setAttribute("termsOfUseText", termsOfUseText);
        } catch (java.lang.NullPointerException e) {
            form.setAttribute("termsOfUseText", "");
        }
        try {
            form.setAttribute("privacyPolicyText", privacyPolicyText);
        } catch (java.lang.NullPointerException e) {
            form.setAttribute("privacyPolicyText", "");
        }

        form.setAttribute("paramAgreeTos", HTTP_PARAMETER_AGREE_TERMS_OF_USE);
        form.setAttribute("paramAgreePp", HTTP_PARAMETER_AGREE_PRIVATE_POLICY);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public void close() {

    }

    @Override
    public String getDisplayType() {
        return "Consent screen";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
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
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
        defaultThemaName = config.get("theme");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
