package com.github.khakers.modmailviewer.auditlog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import com.github.khakers.modmailviewer.auditlog.event.AuditEventSource;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.util.DiscordUtils;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.MongoJackModuleConfiguration;
import org.mongojack.MongoJackModuleFeature;
import org.mongojack.internal.MongoJackModule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MongoAuditLogger implements AuditLogger {

    private static final Logger logger = LogManager.getLogger();

    private final MongoCollection<AuditEvent> auditCollection;

    public MongoAuditLogger(MongoClient mongoClient, String connectionString, String defaultDatabase, String defaultCollection) {

        var connectionString1 = new ConnectionString(connectionString);

        var mongojackFeatures = new MongoJackModuleConfiguration().with(MongoJackModuleFeature.WRITE_INSTANT_AS_BSON_DATE);

        var objectMapper = new JsonMapper()
                .findAndRegisterModules()
                //This is needed to get instant deserialization to work
                .registerModules(new MongoJackModule(mongojackFeatures))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                // We're saving these as indexes
                .configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);


        var mongoDatabase = mongoClient.getDatabase(connectionString1.getDatabase() == null ? defaultDatabase : connectionString1.getDatabase());


        this.auditCollection = JacksonMongoCollection
                .builder()
                .withObjectMapper(objectMapper)
                .build(
                        mongoDatabase,
                        connectionString1.getCollection() == null ? "audit_log" : connectionString1.getCollection(),
                        AuditEvent.class,
                        UuidRepresentation.STANDARD
                );


    }

    public List<AuditEvent> getAuditEvents() {
        return this.auditCollection.find().into(new ArrayList<>());
    }

    @Override
    public void pushEvent(AuditEvent event) {
        logger.debug("Pushing audit event: {}", event);
        this.auditCollection.insertOne(event);
    }

    @Override
    public void pushAuditEventWithContext(Context ctx, String event, String description) throws Exception {
        var user = AuthHandler.getUser(ctx);
        ObjectId.get();
        var auditEvent = new AuditEvent(
                new ObjectId(),
                event,
                Instant.now(),
                description,
                new AuditEventSource(
                        user.getId(),
                        user.getUsername() + (DiscordUtils.isMigratedUserName(user) ? "" : "#" + user.getDiscriminator()),
                        ctx.ip(),
                        null,
                        ctx.userAgent(),
                        AuthHandler.getUserRole(ctx),
                        "modmail-viewer"
                )

        );

        this.pushEvent(auditEvent);
    }
}
