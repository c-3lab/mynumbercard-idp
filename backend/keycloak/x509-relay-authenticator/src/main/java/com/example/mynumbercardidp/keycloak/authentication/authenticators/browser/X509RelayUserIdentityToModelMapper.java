package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.x509.UserIdentityToModelMapper;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class X509RelayUserIdentityToModelMapper {

    public abstract UserModel find(AuthenticationFlowContext context, Object userIdentity)
        throws Exception;

    static class UserIdentityToCustomAttributeMapper extends X509RelayUserIdentityToModelMapper {
        private static Logger consoleLogger = Logger.getLogger(
                                                  UserIdentityToCustomAttributeMapper.class
                                              );

        private List<String> _customAttributes;
        UserIdentityToCustomAttributeMapper(String customAttributes) {
            consoleLogger.debug("customAttributes: " + customAttributes);
            _customAttributes = Arrays.asList(
                                    Constants.CFG_DELIMITER_PATTERN.split(customAttributes)
                                );
        }

        @Override
        public UserModel find(AuthenticationFlowContext context, Object userIdentity)
            throws Exception {

            consoleLogger.debug("Process: find");
            KeycloakSession session = context.getSession();
            List<String> userIdentityValues =
                Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(userIdentity.toString()));

            consoleLogger.trace("userIdentity: " + userIdentity.toString());
            if (_customAttributes.isEmpty() ||
                userIdentityValues.isEmpty() ||
                (_customAttributes.size() != userIdentityValues.size())) {
                consoleLogger.info("getUserIdentityToModelMapper is Empty.");
                return null;
            }
            Stream<UserModel> usersStream =
                session.users().searchForUserByUserAttributeStream(
                    context.getRealm(),
                    _customAttributes.get(0),
                    userIdentityValues.get(0)
                );

            consoleLogger.trace("_customAttributes.get(0): " + _customAttributes.get(0));
            consoleLogger.trace("userIdentityValues.get(0): " + userIdentityValues.get(0));
            consoleLogger.trace("_customAttributes.size(): " + _customAttributes.size());
            for (int i = 0; i <_customAttributes.size(); ++i) {
                String customAttribute = _customAttributes.get(i);
                consoleLogger.trace("customAttribute: " + customAttribute);
                String userIdentityValue = userIdentityValues.get(i);
                consoleLogger.trace("userIdentityValue: " + userIdentityValue);
                usersStream = usersStream.filter(
                                  user -> Objects.equals(
                                              user.getFirstAttribute(customAttribute),
                                              userIdentityValue));
            }
            List<UserModel> users = usersStream.collect(Collectors.toList());
            if (users.size() > 1) {
                throw new ModelDuplicateException();
            }
            consoleLogger.debug("users.size(): " + users.size());
            return users.size() == 1 ? users.get(0) : null;
        }
    }

    public static X509RelayUserIdentityToModelMapper getUserIdentityToCustomAttributeMapper(String attributeName) {
        return new UserIdentityToCustomAttributeMapper(attributeName);
    }
}
