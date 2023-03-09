package com.github.khakers.modmailviewer.util;

import com.github.khakers.modmailviewer.auth.Role;
import io.javalin.security.RouteRole;

public class RoleUtils {

    public static RouteRole[] anyone() {
        return new RouteRole[]{Role.ANYONE, Role.REGULAR, Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }

    public static RouteRole[] atLeastRegular() {
        return new RouteRole[]{Role.REGULAR, Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }

    public static RouteRole[] atLeasRouteSupporter() {
        return new RouteRole[]{Role.SUPPORTER, Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }
    public static RouteRole[] atLeastModerator() {
        return new RouteRole[]{Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }

    public static RouteRole[] atLeastAdministrator() {
        return new RouteRole[]{Role.ADMINISTRATOR, Role.OWNER};
    }
}
