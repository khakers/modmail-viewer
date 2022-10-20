package com.github.khakers.data;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

public record Attachment(
        @BsonRepresentation(BsonType.STRING)
        long id,
        String filename,
        String url,
        boolean isImage,
        int size
) {
}
