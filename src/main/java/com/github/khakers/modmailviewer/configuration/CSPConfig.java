package com.github.khakers.modmailviewer.configuration;

import org.github.gestalt.config.annotations.ConfigPrefix;

import java.util.Optional;
@ConfigPrefix(prefix = "csp")
public record CSPConfig(
      boolean enabled,
      Optional<String> extraScriptSources,
      Optional<String> override
) {
}
