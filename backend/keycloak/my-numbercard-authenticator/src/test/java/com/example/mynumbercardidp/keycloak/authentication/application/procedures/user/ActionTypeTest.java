package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ActionTypeTest {

    @Test
    public void getName() {
        assertNotNull(ActionType.LOGIN.getName());
        assertNotNull(ActionType.REGISTRATION.getName());
        assertNotNull(ActionType.REPLACEMENT.getName());
    }
}
