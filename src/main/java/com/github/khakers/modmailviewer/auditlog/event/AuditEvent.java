package com.github.khakers.modmailviewer.auditlog.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.khakers.modmailviewer.auth.Role;
import io.javalin.http.Context;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.mongojack.ObjectId;

import java.time.Instant;
import java.util.Objects;

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
    public static final class Builder {
        private String action;
        private Instant timestamp = Instant.now();
        private String description;
        private AuditEventSource actor = null;

        // also add parameters for AuditEventSource

        private long userId;
        private String username;
        private String ip;
        private String country;
        private String userAgent;
        private Role role = Role.ANYONE;
        private String source = "modmail-viewer";


        public Builder(String action) {
            this.action = action;
        }

        public Builder withAction(String action) {
            this.action = action;
            return this;
        }

        public Builder withTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Override the actor of the event
         * When set, the userId, username, ip, country, userAgent, and role parameters are ignored
         *
         * @param actor The actor of the event
         * @return The builder
         */
        public Builder withActor(AuditEventSource actor) {
            this.actor = actor;
            return this;
        }

        public Builder fromCtx(Context ctx) {
            this.ip = ctx.ip();
            this.userAgent = ctx.userAgent();

            return this;
        }

        public Builder withUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder withRole(Role role) {
            this.role = role;
            return this;
        }

        public Builder withSource(String source) {
            this.source = source;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(null, action, timestamp, description, Objects.isNull(actor) ? new AuditEventSource(userId, username, ip, country, userAgent, role, source) : actor);
        }
    }
}
