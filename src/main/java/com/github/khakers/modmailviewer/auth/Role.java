package com.github.khakers.modmailviewer.auth;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE(0), REGULAR(1), SUPPORTER(2), MODERATOR(3), ADMINISTRATOR(4), OWNER(5);
    public final int value;

    private Role(int value) {
        this.value = value;
    }

}