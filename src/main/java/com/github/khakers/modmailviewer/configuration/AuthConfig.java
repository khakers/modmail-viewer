package com.github.khakers.modmailviewer.configuration;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigPrefix;

@ConfigPrefix(prefix = "auth")
public record AuthConfig(
      boolean enabled,
      @Config(path = "secretkey")
      String secretKey,
      DiscordClient discordClient,
      // TODO: Implement
      long accessTokenDuration
) {
}
