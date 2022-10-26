package com.github.khakers.modmailviewer.auth;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE, REGULAR, MODERATOR, ADMINISTRATOR, OWNER
}