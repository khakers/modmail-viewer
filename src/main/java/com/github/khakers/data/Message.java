package com.github.khakers.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;
import java.util.List;

public record Message(
        @JsonProperty("message_id")
        String id,
        @JsonProperty("timestamp")
        String creationTime,
        String content,
        List<Attachment> attachments,
        User author,
        MessageType type,
        @JsonProperty("edited")
        boolean isEdited
) {
}
