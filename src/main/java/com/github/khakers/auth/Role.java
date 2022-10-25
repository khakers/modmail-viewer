package com.github.khakers.auth;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE, REGULAR, MODERATOR, ADMINISTRATOR, OWNER
}