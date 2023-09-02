package com.github.khakers.modmailviewer.configuration;

import org.github.gestalt.config.annotations.Config;

public record DiscordClient(
      @Config(path = "id")
      String clientId,
      @Config(path = "secret")
      String clientSecret,
      long guildId) {
}
