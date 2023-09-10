package com.example.mynumbercardidp.keycloak.rest.userinfo.replacement;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;

public class AuthenticationUtil {
    private AuthenticationUtil() {
    }

    public static Optional<AuthenticationExecutionModel> findAuthenticationExecutionModel(RealmModel realm,
                    ClientModel client, String authenticaterId) {
            String authenticationFlowId = Optional
                            .ofNullable(Objects.requireNonNull(client).getAuthenticationFlowBindingOverride("browser"))
                            .orElseGet(() -> {
                                    return Objects.requireNonNull(realm).getBrowserFlow().getId();
                            });
            Stream<AuthenticationExecutionModel> authenticationExecutionStream = Objects.requireNonNull(realm)
                .getAuthenticationExecutionsStream(authenticationFlowId);
        return Optional.ofNullable(authenticationExecutionStream
                        .filter(execution -> execution.getAuthenticator().equals(authenticaterId)).findFirst().get());
    }
}
