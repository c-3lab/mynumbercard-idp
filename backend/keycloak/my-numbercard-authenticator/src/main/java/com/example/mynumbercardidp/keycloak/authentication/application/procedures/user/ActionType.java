package com.example.mynumbercardidp.keycloak.authentication.application.procedures.user;

public enum ActionType {
    LOGIN("login"),
    REGISTRATION("registration"),
    REPLACEMENT("replacement");

    private String name;

    private ActionType(String actionName) {
        this.name = actionName;
    }

    public String getName() {
        return this.name;
    }
}
