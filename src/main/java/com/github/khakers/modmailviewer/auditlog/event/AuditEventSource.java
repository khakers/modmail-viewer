package com.github.khakers.modmailviewer.auditlog.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.khakers.modmailviewer.auth.Role;

/**
 * @param userId The Discord ID of the user who performed the action
 * @param username The discord username of the user who performed the action
 * @param ip The IP address of the user who performed the action
 * @param country The country of the user who performed the action (based on IP) (ISO 3166-1 alpha-2)
 * @param userAgent The user agent of the device that performed the action
 * @param role The role of the user who performed the action
 * @param source The source of the action (e.g. web, discord, etc.)
 */
public record AuditEventSource(
        @JsonProperty("user_id")
        long userId,
        String username,
        String ip,
        String country,
        @JsonProperty("user_agent")
        String userAgent,
        Role role,
        String source
) {
}
