package com.github.khakers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.khakers.data.ModMailLogEntry;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.*;

public class ModMailLogDB {

    private static final Logger logger = LogManager.getLogger();

    private MongoDatabase database;
    private final MongoCollection<Document> logs;

    private final ObjectMapper objectMapper;

    public ModMailLogDB(String connectionString) {

        this.objectMapper = new JsonMapper().findAndRegisterModules();

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);

        var settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .codecRegistry(codecRegistry)
                .build();

        MongoClient mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("modmail_bot");
        database.listCollectionNames().forEach(System.out::println);
        this.logs = database.getCollection("logs");

    }

    public Optional<ModMailLogEntry> getModMailLogEntry(String id) {
        try {
            var result = logs.find(Filters.eq("_id", id)).limit(1).first();
            if (result == null) {
                return Optional.empty();
            }
            var json = result.toJson();
            logger.debug("Got JSON value of {}", json);
            return Optional.of(objectMapper.readValue(json, ModMailLogEntry.class));
        } catch (Exception e) {
            logger.error(e);
            return Optional.empty();
        }
    }

    public List<ModMailLogEntry> getMostRecentEntries() {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        var foundLogs = logs.find().sort(Sorts.descending("created_at")).limit(8);
        foundLogs.forEach(document -> {
            try {
                entries.add(objectMapper.readValue(document.toJson(), ModMailLogEntry.class));
            } catch (JsonProcessingException e) {
                logger.error(e);
            }
        });
        return entries;
    }

    public List<ModMailLogEntry> getPaginatedMostRecentEntries(int page) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        var foundLogs = logs.find().sort(Sorts.descending("created_at")).skip((page - 1) * 8).limit(8);
        foundLogs.forEach(document -> {
            try {
                entries.add(objectMapper.readValue(document.toJson(), ModMailLogEntry.class));
            } catch (JsonProcessingException e) {
                logger.error(e);
            }
        });
        return entries;
    }

}
