package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class ActionTypeTest {

    @Test
    public void testGetName() {
        assertEquals("login", ActionType.LOGIN.getName());
        assertEquals("registration", ActionType.REGISTRATION.getName());
        assertEquals("replacement", ActionType.REPLACEMENT.getName());
    }
}
