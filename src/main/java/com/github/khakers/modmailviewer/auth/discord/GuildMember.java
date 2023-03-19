package com.github.khakers.modmailviewer.auth.discord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.khakers.modmailviewer.auth.UserToken;

import java.time.Instant;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GuildMember(
        UserToken user,
        Optional<String> nick,
        Optional<String> avatar,
        long[] roles,
        Instant joined_at,
        Optional<Instant> premiumSince,
        boolean deaf,
        boolean mute,
        int flags,
        Optional<Boolean> pending,
        Optional<String> permissions,
        Optional<Instant> communicationDisabledUntil
        ) {
}
