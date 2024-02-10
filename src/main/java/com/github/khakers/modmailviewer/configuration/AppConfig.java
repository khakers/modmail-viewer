package com.github.khakers.modmailviewer.configuration;


import org.github.gestalt.config.annotations.Config;

import java.util.Optional;

public record AppConfig(
      // Not sure why, but per class ConfigPrefix is ignored
      // So we're setting the path for each config here
      @Config(path = "port.http")
      int httpPort,
      @Config(path = "port.https")
      int httpsPort,
      String mongodbUri,
      String url,
      @Config(path = "ssl")
      Optional<SSLConfig> sslOptions,
      boolean secureCookies,

      // Make AuthConfig optional again, read enabled option below
      @Config(defaultVal = "true", path = "auth.enabled")
      boolean isAuthEnabled,
      @Config(path = "auth")
      Optional<AuthConfig> auth,
      boolean dev,
      @Config(path = "audit")
      AuditLogConfig auditLogConfig,
      @Config(path = "csp")
      CSPConfig cspConfig,
      long botId,
      String branding,
      Optional<String> analytics,

      Optional<String> s3Url
) {
}
