package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.jetbrains.annotations.Nullable;

public record Attachment(
      @BsonRepresentation(BsonType.STRING)
      long id,
      String filename,
      String url,
      //isImage does not correctly Identify whether this is an image
      @JsonProperty("is_image")
      boolean isImage,
      int size,

      //Requires modmail enhanced feature support
      @JsonProperty("content_type")
      @Nullable
      String contentType
) {
}
