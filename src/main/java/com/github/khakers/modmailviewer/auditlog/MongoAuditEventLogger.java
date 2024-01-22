package com.github.khakers.modmailviewer.auditlog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.MongoJackModuleConfiguration;
import org.mongojack.MongoJackModuleFeature;
import org.mongojack.internal.MongoJackModule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MongoAuditEventLogger implements OutboundAuditEventLogger, AuditEventDAO {

    private static final Logger logger = LogManager.getLogger();

    private final MongoCollection<AuditEvent> auditCollection;

    public MongoAuditEventLogger(MongoDatabase modmailMongoDatabase) {


        var mongojackFeatures = new MongoJackModuleConfiguration().with(MongoJackModuleFeature.WRITE_INSTANT_AS_BSON_DATE);

        var objectMapper = new JsonMapper()
                .findAndRegisterModules()
                //This is needed to get instant deserialization to work
                .registerModules(new MongoJackModule(mongojackFeatures))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                // We're saving these as indexes
                .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);


        this.auditCollection = JacksonMongoCollection
                .builder()
                .withObjectMapper(objectMapper)
                .build(
                      modmailMongoDatabase,
                        "audit_log",
                        AuditEvent.class,
                        UuidRepresentation.STANDARD
                );

        // create TTL index
        auditCollection.createIndex(Indexes.ascending("timestamp"), new IndexOptions().expireAfter(30L, TimeUnit.DAYS));

    }

    @Override
    public List<AuditEvent> getAuditEvents() {
        return this.auditCollection.find().into(new ArrayList<>());
    }

    @Override
    public Optional<AuditEvent> getAuditEvent(String id) {
        logger.debug("Getting audit event with id: {}", id);
        return Optional.ofNullable(this.auditCollection.find(Filters.eq("_id", new ObjectId(id))).first());
    }

    @Override
    public List<AuditEvent> searchAuditEvents(Instant rangeStart, Instant rangeEnd, List<Long> userIds, List<String> actions) {
        var timeFilter = Filters.and(
                Filters.gte("timestamp", rangeStart),
                Filters.lte("timestamp", rangeEnd)
        );

        Bson userFilter;

        if (userIds.isEmpty()) {
            userFilter = Filters.empty();
        } else {
            userFilter = Filters.or(userIds.stream()
                    .map(userId -> Filters.eq("actor.user_id", userId))
                    .collect(Collectors.toSet()));
        }

        Bson actionFilter;
        if (actions.isEmpty()) {
            actionFilter = Filters.empty();
        } else {
            actionFilter = Filters.or(actions.stream()
                    .map(action -> Filters.eq("action", action))
                    .collect(Collectors.toSet()));

        }

        return this.auditCollection.find(Filters.and(timeFilter, userFilter, actionFilter))
                .into(new ArrayList<>());
    }

    @Override
    public void pushEvent(AuditEvent event) {
        logger.debug("Pushing audit event: {}", event);
        this.auditCollection.insertOne(event);
    }

    @Override
    public void pushAuditEventWithContext(Context ctx, String event, String description) throws Exception {
        var user = AuthHandler.getUser(ctx);
        this.pushEvent(new AuditEvent.Builder(event)
              .fromCtx(ctx)
              .withDescription(description)
              .withRole(AuthHandler.getUserRole(ctx))
              .withUser(user)
              .build());
    }

}
