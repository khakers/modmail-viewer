package com.github.khakers.modmailviewer.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.khakers.modmailviewer.configuration.Config;
import org.apache.logging.log4j.LogManager;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public record Attachment(
      @BsonRepresentation(BsonType.STRING)
      long id,
      String filename,
      String url,
      @JsonProperty("is_image")
      @BsonProperty("is_image")
      @Deprecated
      boolean isImage,
      int size,

      Optional<String> type,

      //Requires modmail enhanced feature support
      @JsonProperty("content_type")
      @BsonProperty("content_type")
      @Nullable
      String contentType,

      @JsonProperty("s3_object")
      @BsonProperty("s3_object")
      @Nullable
      String s3Object,
      @JsonProperty("s3_bucket")
      @BsonProperty("s3_bucket")
      @Nullable
      String s3Bucket

) {

    public Optional<String> getAttachmentURI() {
        if (type.isPresent() && type.get().equals("internal")) {
            return Optional.of("/attachment/" + id);
        }
        if (type.isPresent() && type.get().equals("s3")) {
            if (Config.appConfig.s3Url().isEmpty()) {
                LogManager.getLogger().error("S3 URL not configured, cannot generate attachment URL");
                return Optional.empty();
            }
            if (s3Bucket == null || s3Object == null) {
                LogManager.getLogger().error("The attachment {} is missing s3 bucket ({}) or object ({}) information, cannot generate attachment URL", id, s3Bucket, s3Object);
                return Optional.empty();
            }
            return (Config.appConfig.s3Url().get()+"/"+ s3Bucket+"/"+ s3Object).describeConstable();
        }

        return url.describeConstable();
    }

    public boolean isImageContentType() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isVideoContentType() {
        return contentType != null && contentType.startsWith("video/");
    }
}
