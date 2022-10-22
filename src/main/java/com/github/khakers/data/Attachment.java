package com.github.khakers.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

public record Attachment(
        @BsonRepresentation(BsonType.STRING)
        long id,
        String filename,
        String url,
        @JsonProperty("is_image")
        boolean isImage,
        int size
) {
}
