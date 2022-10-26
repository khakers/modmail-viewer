package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;

public record User(
        String id,
        String name,
        String discriminator,
        @BsonProperty(value = "avatar_url")
        @JsonProperty("avatar_url")
        String avatarUrl,
        boolean mod
) {
}
