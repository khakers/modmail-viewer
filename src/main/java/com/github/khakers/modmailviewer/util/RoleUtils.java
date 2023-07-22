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

    public static RouteRole[] atLeastSupporter() {
        return new RouteRole[]{Role.SUPPORTER, Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }
    public static RouteRole[] atLeastModerator() {
        return new RouteRole[]{Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }

    public static RouteRole[] atLeastAdministrator() {
        return new RouteRole[]{Role.ADMINISTRATOR, Role.OWNER};
    }

    /**
     * Checks is a role is at least the role specified hierarchically
     *
     * @param role   The role to check
     * @param atLeast The role to check against
     * @return  True if the role is at least the role specified
     */
    public static boolean isAtLeastRole(Role role, Role atLeast) {
        return role.ordinal() >= atLeast.ordinal();
    }
}
