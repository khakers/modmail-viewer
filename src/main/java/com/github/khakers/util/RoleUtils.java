package com.github.khakers.util;

import com.github.khakers.auth.Role;
import io.javalin.security.RouteRole;

public class RoleUtils {

    public static RouteRole[] atLeastRegular() {
        return new RouteRole[]{Role.REGULAR, Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }
    public static RouteRole[] atLeastModerator() {
        return new RouteRole[]{Role.MODERATOR, Role.ADMINISTRATOR, Role.OWNER};
    }

    public static RouteRole[] atLeastAdministrator() {
        return new RouteRole[]{Role.ADMINISTRATOR, Role.OWNER};
    }
}
