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
        /*
         * This is true if the channel the message was sent in was not a DM
         * It implies nothing about actual status as a modw
         */
        boolean mod
) {
}
