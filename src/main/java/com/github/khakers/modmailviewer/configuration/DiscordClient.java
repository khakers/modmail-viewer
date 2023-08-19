package com.github.khakers.modmailviewer.configuration;

import org.github.gestalt.config.annotations.ConfigPrefix;

@ConfigPrefix(prefix = "discord")
public record DiscordClient(
    String clientId,
    String clientSecret,
    long guildId) {
}
