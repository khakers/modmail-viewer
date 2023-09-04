package com.github.khakers.modmailviewer.configuration;

import org.github.gestalt.config.annotations.Config;

public record AuthConfig(
      @Config(path = "secretkey")
      String secretKey,
      @Config(path = "discord")
      DiscordClient discordClient,
      // TODO: Implement
      long accessTokenDuration
) {
}
