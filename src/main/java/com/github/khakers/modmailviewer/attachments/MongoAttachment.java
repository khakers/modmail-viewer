package com.github.khakers.modmailviewer.attachments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
@JsonIgnoreProperties(ignoreUnknown = true)
public record MongoAttachment(
      @BsonProperty("_id")
      @JsonProperty("_id")
      long id,
      @BsonProperty("content_type")
      @JsonProperty("content_type")
      String contentType,
      byte[] data,
//      @Nullable String description,
      String filename,
      int size,
      @Nullable
      Integer height,
      @Nullable
      Integer width,
      @BsonProperty("uploaded_at")
      @JsonProperty("uploaded_at")
      Instant uploadTime

) {
}
