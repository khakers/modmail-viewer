package com.github.khakers.modmailviewer.configuration;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigPrefix;

@ConfigPrefix(prefix = "audit")
public record AuditLogConfig(
      @Config(path="enabled")
      boolean isAuditLoggingEnabled,
      @Config(path = "detailed")
      boolean isDetailedAuditingEnabled,
      @Config(path = "logApiUsage")
      boolean isApiAuditingEnabled,
      //TODO plug in retentionPeriod
      long retentionPeriod
) {
}
