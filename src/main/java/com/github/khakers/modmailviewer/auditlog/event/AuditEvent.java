package com.github.khakers.modmailviewer.auditlog.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.mongojack.ObjectId;

import java.time.Instant;

public record AuditEvent(
        @JsonProperty("_id")
        @BsonProperty("_id")
        @ObjectId
        org.bson.types.ObjectId id,
        String action,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        Instant timestamp,
        String description,
        AuditEventSource actor
) {
}
