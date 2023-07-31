package com.example.mynumbercardidp.keycloak.authentication.forms;

import com.example.mynumbercardidp.keycloak.authentication.authenticators.browser.RelayResponse;
import com.example.mynumbercardidp.keycloak.authentication.utils.X509AuthenticatorUtil;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;


public class X509RegistrationUserCreation implements FormAction, FormActionFactory {
    public static final String PROVIDER_ID = "registration-x509-user-creation";

    private static Logger consoleLogger = Logger.getLogger(X509RegistrationUserCreation.class);
    private MultivaluedMap<String, String> userData;

    @Override
    public String getHelpText() {
        return "This action must always be first! In validate phase, " +
               "Validates x509 validate server responce attributes " +
               "and stores them in user data. " +
               "In success phase, this will create the user in the database.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    private static final List<ProviderConfigProperty> configProperties =
        new ArrayList<ProviderConfigProperty>();

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
        androidAppUriProperty.setLabel("Run URL of Android application");
        androidAppUriProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(androidAppUriProperty);

        ProviderConfigProperty iosAppUriProperty = new ProviderConfigProperty();
        iosAppUriProperty.setName("x509-relay-auth.ios-app-uri");
        iosAppUriProperty.setLabel("Run URL of iOS application");
        iosAppUriProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(iosAppUriProperty);

        ProviderConfigProperty appUriProperty = new ProviderConfigProperty();
        appUriProperty.setName("x509-relay-auth.app-uri");
        appUriProperty.setLabel("Installation URI of Android/iOS application");
        appUriProperty.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(appUriProperty);
    }

    @Override
    public void validate(org.keycloak.authentication.ValidationContext context) {
        consoleLogger.info("Called: validate");
        MultivaluedMap<String, String> formData =
            context.getHttpRequest().getDecodedFormParameters();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        try {
            if (formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME).isEmpty() ||
                formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME) == null ) {
                consoleLogger.warn("process: attempted (Post empty.)");
                return;
            }
            if (!X509AuthenticatorUtil.checkCertificateFormat(
                     formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME))) {
                consoleLogger.warn("process: attempted (File format error.)");
                return;
            } else {
                if(!X509AuthenticatorUtil.verifySignature(
                       formData.getFirst(X509AuthenticatorUtil.SIGNATURE_ATTR_NAME),
                       formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME),
                       context.getAuthenticationSession().getAuthNote("nonce"))) {
                    consoleLogger.warn("process: attempted (Signature verifying error.)");
                    return;
                }
            }

            RelayResponse relayResponse = X509AuthenticatorUtil.convertRelayResponse(
                                              X509AuthenticatorUtil.verifyCertificate(
                                                  context.getAuthenticatorConfig()
                                                      .getConfig()
                                                      .get("x509-relay-auth.certificate-validator-uri"),
                                                  X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME,
                                                  formData.getFirst(
                                                      X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME)));
            userData = relayResponse.toMultivaluedMap();
            UserProfileProvider profileProvider = context.getSession()
                                                      .getProvider(UserProfileProvider.class);
            UserProfile profile = profileProvider.create(
                                      UserProfileContext.REGISTRATION_PROFILE,
                                      userData);

            try {
                profile.validate();
            } catch (ValidationException pve) {
                List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());
                context.validationError(formData, errors);
                pve.printStackTrace();
                return;
            }
            consoleLogger.info("process: success");
            context.success();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    @Override
    public void success(FormContext context) {
        consoleLogger.info("Called: success");
        KeycloakSession session = context.getSession();
        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(
                                  UserProfileContext.REGISTRATION_USER_CREATION,
                                  userData);
        UserModel user = profile.create();
        // User attribute redefinition.
        String[] attributeName = {
            "uniqueId",
            "gender",
            "region",
            "locality",
            "birthdate"
        };
        for (String key : attributeName) {
            user.setSingleAttribute(key, userData.getFirst(key)); 
        }
        user.setFirstName(userData.getFirst("firstName"));
        user.setLastName(userData.getFirst("givenName"));

        user.setEnabled(true);
        context.setUser(user);
        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        consoleLogger.info("Called: buildPage");

        String nonce = UUID.randomUUID().toString();
        context.getAuthenticationSession().setAuthNote("nonce", nonce);
        form.setAttribute("nonce", nonce);
        consoleLogger.debug("getConfig():");
        consoleLogger.debug(context.getAuthenticatorConfig().getConfig());

        try {
            form.setAttribute(
             "debug",
             context.getAuthenticatorConfig().getConfig()
                 .get("x509-relay-auth.debug-mode")
                 .toString()
                 .toLowerCase()
             );
        } catch (java.lang.NullPointerException e) {
             form.setAttribute("debug", "false");
        }

        form.setAttribute(
            "androidAppUri",
             context.getAuthenticatorConfig()
                 .getConfig()
                 .get("x509-relay-auth.android-app-uri")
        );
        form.setAttribute(
            "iosAppUri",
             context.getAuthenticatorConfig()
                 .getConfig()
                 .get("x509-relay-auth.ios-app-uri")
        );
        form.setAttribute(
            "otherAppUri",
             context.getAuthenticatorConfig()
                 .getConfig()
                 .get("x509-relay-auth.app-uri")
        );
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
        return "User creation by server response";
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

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
