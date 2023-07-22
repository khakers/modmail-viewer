package com.github.khakers.modmailviewer.util;

import com.github.khakers.modmailviewer.auth.UserToken;
import com.github.khakers.modmailviewer.data.Message;
import com.github.khakers.modmailviewer.data.MessageType;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.data.User;

import java.util.Optional;

public class DiscordUtils {
    private static final String DISCORD_MESSAGE_URI_FORMAT = "https://discord.com/channels/%s/%s/%s";

    /**
     * Returns true if the user is a migrated user (i.e. has no discriminator)
     *
     * @return True if the user is a migrated user
     */
    public static boolean isMigratedUserName(UserToken user) {
        return user.getDiscriminator().equals("0") || user.getDiscriminator().isBlank();
    }

    public static boolean isLegacyUsername(UserToken user) {
        return !(user.getDiscriminator().equals("0") || user.getDiscriminator().isBlank());

    }

    /**
     * Returns true if the user is a migrated user (i.e. has no discriminator)
     *
     * @param user The user to check
     * @return True if the user is a migrated user
     */
    public static boolean isMigratedUserName(User user) {
        return user.discriminator().equals("0") || user.discriminator().isBlank();
    }

    public static boolean isLegacyUsername(User user) {
        return !(user.discriminator().equals("0") || user.discriminator().isBlank());
    }

    /**
     * @return The discriminator string for the user starting with a # or an empty string if the user is a migrated user
     */
    public static String getDiscriminatorString(UserToken user) {
        if (isMigratedUserName(user))
            return "";
        else
            return "#" + user.getDiscriminator();
    }

    /**
     * @param user The user to get the Discord discriminator string for
     * @return The discriminator string for the user starting with a # or an empty string if the user is a migrated user
     */
    public static String getDiscriminatorString(User user) {
        if (isMigratedUserName(user))
            return "";
        else
            return "#" + user.discriminator();
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

    /**
     * Gets the Discord URI for a message
     * if the message is a thread message, and the log entry does not have a DM channel ID, it will return empty
     *
     * @param message         a message object
     * @param modMailLogEntry the log entry the message is contained within
     * @return The URI for the message, empty if the message is a thread message and the log entry does not have a DM channel ID
     */
    public static Optional<String> getMessageURI(Message message, ModMailLogEntry modMailLogEntry) {

        if (!message.getType().equals(MessageType.thread)) {
            return Optional.of(String.format(DISCORD_MESSAGE_URI_FORMAT, modMailLogEntry.getGuildId(), modMailLogEntry.getChannelId(), message.getId()));
        } else {
            if (modMailLogEntry.getDmChannelId().isPresent())
                return Optional.ofNullable(String.format(DISCORD_MESSAGE_URI_FORMAT, "@me", modMailLogEntry.getDmChannelId().get(), message.getId()));
            else
                return Optional.empty();
        }
    }
}
