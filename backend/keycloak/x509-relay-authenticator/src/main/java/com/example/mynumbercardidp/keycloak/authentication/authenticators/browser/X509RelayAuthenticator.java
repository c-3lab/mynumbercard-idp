package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import com.example.mynumbercardidp.keycloak.authentication.utils.X509AuthenticatorUtil;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

import java.lang.Exception;
import java.util.UUID;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;


public class X509RelayAuthenticator extends UsernamePasswordForm {
    protected static final String X509_FILE_UPLOAD_ENABLE = "x509Upload";

    private static Logger consoleLogger = Logger.getLogger(X509RelayAuthenticator.class);
    protected static ServicesLogger logger = ServicesLogger.LOGGER;

    @Override
    /*
     *  HTTP POST method --> action()
     *      This method recive application/x-www-form-urlencoded (x509 DER file).
     *      If signed nonce string is equal created nonce string,
     *      Send x509 file or text to Dummy platform.
     *      Dummy platform return JSON (unique id, certificate validation) result.
     *          [Note] Unique id could be empty when certificate validation fault.
     *      It find users with matching unique ID and a user attribute value.
     *      That result is login user.
     */
    public void action(AuthenticationFlowContext context) {
        consoleLogger.info("Called: action");
        consoleLogger.debug(
            "Query parameters: \"" +
            context.getHttpRequest().getUri().getQueryParameters(false) +
            "\""
            );

        MultivaluedMap<String, String> formData =
            context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("cancel")) {
            consoleLogger.debug("Process: cancel");
            context.cancelLogin();
            return;
        }

    consoleLogger.debug("Process: Defined UserModel");
        UserModel user;
        try {
            consoleLogger.debug("Process: verifyCertificateFile");
            consoleLogger.debug(
                "formData file (Certificate): " +
                formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME)
            );
            consoleLogger.debug(
                "formData signature: " +
                formData.getFirst(X509AuthenticatorUtil.SIGNATURE_ATTR_NAME)
            );
            if (formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME).isEmpty() ||
                formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME) == null ) {
                consoleLogger.warn("process: attempted (Post empty.)");
                context.attempted();
                return;
            }
            if (!X509AuthenticatorUtil.checkCertificateFormat(
                    formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME)
                )) {
                consoleLogger.warn("process: attempted (File format error.)");
                context.attempted();
                return;
            } else {
                if(!X509AuthenticatorUtil.verifySignature(
                      formData.getFirst(X509AuthenticatorUtil.SIGNATURE_ATTR_NAME),
                      formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME),
                      context.getAuthenticationSession().getAuthNote("nonce")
                   )) {
                    consoleLogger.warn("process: attempted (Signature verifying error.)");
                    context.attempted();
                    return;
                }
            }

            RelayResponse relayResponse = X509AuthenticatorUtil.convertRelayResponse(
                                              X509AuthenticatorUtil.verifyCertificate(
                                                  context.getAuthenticatorConfig()
                                                      .getConfig()
                                                      .get("x509-relay-auth.certificate-validator-uri"),
                                                  X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME,
                                                  formData.getFirst(X509AuthenticatorUtil.RELAY_DEST_FILE_ATTR_NAME)
                                              )
                                          );
        consoleLogger.debug("Process: userFind");
            user = getUserIdentityToModelMapper(X509AuthenticatorUtil.USER_IDENTITY_ATTR_NAME)
                       .find(context, relayResponse.getUniqueId());
            context.setUser(user);

            if (context.getUser() != null) {
            consoleLogger.info("process: success");
                context.success();
                return;
            }

        consoleLogger.warn("Process: attempted (Not found user.)");
            context.attempted();
            return;
        }
        catch (ModelDuplicateException e) {
        consoleLogger.warn("Process: ModelDuplicateException (Found more than one users.)");
            logger.modelDuplicateException(e);
            context.attempted();
            return;
        }
        catch (Exception e) {
        consoleLogger.error("Process: Exception (Unexpected exception.)");
            e.printStackTrace();
            context.attempted();
            return;
        }
    }

    protected boolean validateResponse(RelayResponse responce) {
        if (responce.getVerifyResultCode() != RelayResponse.VERIFY_RESULT_SUCCESS) {
            return false;
        }
        return true;
    }

    /*
     *  HTTP GET method  --> authenticate()
     *      Ownership of a public key is determined by being able to verify a string signed by the private key with that public key.
     *      Generate a nonce as a string to sign.
     *      Return the redirect native application page include nonce string.
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();

        consoleLogger.info("Called: authenticate");
        consoleLogger.debug("HTTP Method: " + context.getHttpRequest().getHttpMethod().toString());

        if (HttpMethod.GET.equalsIgnoreCase(context.getHttpRequest().getHttpMethod()) ||
            HttpMethod.GET.equalsIgnoreCase(context.getHttpRequest().getHttpMethod().toString())) {

            String loginHint = context.getAuthenticationSession()
                                   .getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

            LoginFormsProvider form = context.form();
            form.setAttribute(X509_FILE_UPLOAD_ENABLE, true);
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

            if (context.getUser() != null) {
                form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
                form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true);
                context.getAuthenticationSession().setAuthNote(
                    USER_SET_BEFORE_USERNAME_PASSWORD_AUTH,
                    "true"
                );
            } else {
                String nonce = UUID.randomUUID().toString();
                context.getAuthenticationSession().setAuthNote("nonce", nonce);
                form.setAttribute("nonce", nonce);
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
                context.getAuthenticationSession()
                    .removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
            }

            Response challengeResponse = challenge(context, formData);
            context.challenge(challengeResponse);
            return;
        }

        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (formData.size() > 0) forms.setFormData(formData);

        return forms.createLoginUsernamePassword();
    }

    public X509RelayUserIdentityToModelMapper getUserIdentityToModelMapper(String attributeName) {
        consoleLogger.debug("Process: getUserIdentityToModelMapper");
        return UserIdentityToModelMapperBuilder.fromString(attributeName);
    }

    protected static class UserIdentityToModelMapperBuilder {

        static X509RelayUserIdentityToModelMapper fromString(String attributeName) {

            X509RelayUserIdentityToModelMapper mapper = X509RelayUserIdentityToModelMapper.getUserIdentityToCustomAttributeMapper(attributeName);
            return mapper;
        }
    }

}
