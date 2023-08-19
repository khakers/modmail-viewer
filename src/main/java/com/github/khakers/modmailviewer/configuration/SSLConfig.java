package com.github.khakers.modmailviewer.configuration;

import org.github.gestalt.config.annotations.Config;

import java.util.Optional;

public record SSLConfig(
      @Config(defaultVal = "false")
      boolean enabled,
      Optional<String> cert,
      Optional<String> key,
      @Config(defaultVal = "false")
      boolean httpsOnly,
      @Config(path = "sni")
      boolean isSNIEnabled,
      @Config(path = "sts")
      boolean isSTSEnabled) {
}
