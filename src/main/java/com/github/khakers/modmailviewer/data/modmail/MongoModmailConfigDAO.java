package com.github.khakers.modmailviewer.data.modmail;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.mongojack.JacksonMongoCollection;

import java.util.Optional;

import static com.mongodb.client.model.Projections.*;

/**
 * A DAO for the ModmailConfig collection in MongoDB
 * Does provide any form of caching
 * <p>
 *     This class is responsible for all interactions with the ModmailConfig collection in MongoDB.
 *     It also provides methods to retrieve and update individual values in the config
 * </p>
 */
public class MongoModmailConfigDAO implements ModmailConfigDao {

    protected MongoCollection<ModmailConfig> modmailConfigMongoCollection;
    protected MongoCollection<Document> untypedMongoCollection;

    private final long botId;

    public MongoModmailConfigDAO(MongoDatabase database, long botId) {
        this(database, botId, "config");
    }

    public MongoModmailConfigDAO(MongoDatabase database, long botId, String collectionName) {
//        JacksonMongoCollection.builder().build(database,collectionName, ModmailConfig.class, UuidRepresentation.STANDARD);
        this.modmailConfigMongoCollection = JacksonMongoCollection.builder().build(database,collectionName, ModmailConfig.class, UuidRepresentation.STANDARD);
        this.untypedMongoCollection = database.getCollection(collectionName);
        this.botId = botId;
    }

    @Override
    public ModmailConfig getConfig() {
        return modmailConfigMongoCollection
              .find(Filters.eq("bot_id", this.botId))
              .first();
    }

    /**
     * Retrieve a single value from the config with the given key
     *
     * @param key The key to get the value of
     * @return The value of the key
     */
    @Override
    public Optional<Object> getSingleConfigValue(String key) {
        var result = untypedMongoCollection
              .find(Filters.eq("bot_id", this.botId))
              .projection(fields(include(key), excludeId()))
              .first();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(result.get(key));
    }

    /**
     * Update a value in the config of the given key
     *
     * @param key   The key to update
     * @param value The value to update the key to
     */
    @Override
    public <V> void updateConfigValue(String key, V value) {
        modmailConfigMongoCollection.updateOne(
              Filters.eq("bot_id", this.botId),
              Updates.set(key, value));
    }
}
