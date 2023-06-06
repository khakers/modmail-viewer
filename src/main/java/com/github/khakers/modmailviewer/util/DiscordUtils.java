package com.github.khakers.modmailviewer.util;

import com.github.khakers.modmailviewer.auth.UserToken;
import com.github.khakers.modmailviewer.data.User;

public class DiscordUtils {
    /**
     * Returns true if the user is a migrated user (i.e. has no discriminator)
     *
     * @param user The user to check
     * @return True if the user is a migrated user
     */
    public static boolean isMigratedUserName(User user) {
        return user.discriminator().equals("0") || user.discriminator().isBlank();
    }

    public static boolean isMigratedUserName(UserToken user) {
        return user.getDiscriminator().equals("0") || user.getDiscriminator().isBlank();
    }

    /**
     * Returns the avatar ID for the user
     *
     * @param user The user to get the avatar ID for
     * @return The avatar ID for the user
     */
    public static int getAvatarId(User user) {
        if (isMigratedUserName(user))
            return (int) ((Long.parseLong(user.id()) >> 22) % 5);
        else
            return Integer.parseInt(user.discriminator()) % 5;
    }

    /**
     * Returns the avatar URL for the user
     *
     * @param user The user to get the avatar URL for
     * @return The avatar URL for the user
     */
    public static String getAvatarUrl(User user) {
        return "https://cdn.discordapp.com/embed/avatars/" + getAvatarId(user) + ".png";
    }
}
