package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.auth.SiteUser;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.data.ModmailConfig;
import com.github.khakers.modmailviewer.data.internal.TicketStatus;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ModMailLogDB {

    public static final int DEFAULT_ITEMS_PER_PAGE = 8;
    private static final Logger logger = LogManager.getLogger();
    private final MongoDatabase database;

    private final MongoCollection<Document> configCollection;
    private final MongoCollection<Document> logCollection;

    private final ObjectMapper objectMapper;

    private ModmailConfig cachedConfig = null;
    private Instant cacheTime;

    public ModMailLogDB(String connectionString) {

        this.objectMapper = new JsonMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);

        var settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .codecRegistry(codecRegistry)
                .build();

        MongoClient mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("modmail_bot");
        database.listCollectionNames().forEach(logger::debug);
        this.logCollection = database.getCollection("logs");
        this.configCollection = database.getCollection("config");

        var result = logCollection.createIndex(Indexes.descending("messages.timestamp"));
//        var textIndex = logCollection.createIndex(Indexes.text("messages.content"));
        logger.debug(result);

    }

    public int getTotalTickets(TicketStatus ticketStatus) {
        return getTotalTickets(ticketStatus, "");
    }

    public int getTotalTickets(TicketStatus ticketStatus, @Nullable String text) {
        if (text == null || text.isBlank()) {
            switch (ticketStatus) {
                case OPEN -> {
                    return Math.toIntExact(logCollection.countDocuments(Filters.eq("open", true)));
                }
                case CLOSED -> {
                    return Math.toIntExact(logCollection.countDocuments(Filters.eq("open", false)));
                }
                case ALL -> {
                    return (int) logCollection.estimatedDocumentCount();
                }
                default -> {
                    return 0;
                }
            }
        }
        switch (ticketStatus) {
            case OPEN -> {
                return Math.toIntExact(logCollection.countDocuments(Filters.and(Filters.eq("open", true), Filters.text(text))));
            }
            case CLOSED -> {
                return Math.toIntExact(logCollection.countDocuments(Filters.and(Filters.eq("open", false), Filters.text(text))));
            }
            case ALL -> {
                return Math.toIntExact(logCollection.countDocuments(Filters.text(text)));
            }
            default -> {
                return 0;
            }
        }
    }

    public Optional<ModMailLogEntry> getModMailLogEntry(String id) {
        try {
            var result = logCollection.find(Filters.eq("_id", id)).limit(1).first();
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

    public List<ModMailLogEntry> getMostRecentEntries(int count) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        var foundLogs = logCollection
                .find()
                .sort(Sorts.descending("created_at"))
                .limit(count);
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
        return getPaginatedMostRecentEntries(page, DEFAULT_ITEMS_PER_PAGE);
    }

    public List<ModMailLogEntry> getPaginatedMostRecentEntries(int page, int itemsPerPage) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        var foundLogs = logCollection
                .find()
                .sort(Sorts.descending("created_at"))
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage);
        foundLogs.forEach(document -> {
            try {
                entries.add(objectMapper.readValue(document.toJson(), ModMailLogEntry.class));
            } catch (JsonProcessingException e) {
                logger.error(e);
            }
        });
        return entries;
    }

    /**
     * Returns a list of log entries based on their most recent activity
     *
     * @param page
     * @return
     */
    public List<ModMailLogEntry> getPaginatedMostRecentEntriesByMessageActivity(int page) {
        return getPaginatedMostRecentEntriesByMessageActivity(page, TicketStatus.ALL);
    }

    /**
     * Returns a list of log entries based on their most recent activity
     *
     * @param page
     * @return
     */
    public List<ModMailLogEntry> getPaginatedMostRecentEntriesByMessageActivity(int page, TicketStatus ticketStatus) {
        return getPaginatedMostRecentEntriesByMessageActivity(page, DEFAULT_ITEMS_PER_PAGE, ticketStatus, null);
    }

    /**
     * Returns a list of log entries based on their most recent activity
     *
     * @param page
     * @param itemsPerPage
     * @param searchText
     * @return
     */
    public List<ModMailLogEntry> getPaginatedMostRecentEntriesByMessageActivity(int page, int itemsPerPage, TicketStatus ticketStatus, String searchText) {
        return searchPaginatedMostRecentEntriesByMessageActivity(page, itemsPerPage, ticketStatus, searchText);
//        ArrayList<ModMailLogEntry> entries = new ArrayList<>(itemsPerPage);
//        var ticketFilter = switch (ticketStatus) {
//            case ALL -> Filters.empty();
//            case CLOSED -> Filters.eq("open", false);
//            case OPEN -> Filters.eq("open", true);
//        };
//        logger.debug("filtering by {} with {}", ticketStatus, ticketFilter);
//        var foundLogs = logCollection
//                .find()
//                .filter(Filters.not(Filters.size("messages", 0)))
//                .sort(Sorts.descending("messages.timestamp"))
//                .filter(ticketFilter)
//                .skip((page - 1) * itemsPerPage)
//                .limit(itemsPerPage);
//        foundLogs.forEach(document -> {
//            try {
//                entries.add(objectMapper.readValue(document.toJson(), ModMailLogEntry.class));
//            } catch (JsonProcessingException e) {
//                logger.error(e);
//            }
//        });
//        logger.trace("Entries: {}", entries);
//        return entries;
    }

    public List<ModMailLogEntry> searchPaginatedMostRecentEntriesByMessageActivity(int page, TicketStatus ticketStatus, String searchkey) {
        return searchPaginatedMostRecentEntriesByMessageActivity(page, DEFAULT_ITEMS_PER_PAGE, ticketStatus, searchkey);
    }

    public List<ModMailLogEntry> searchPaginatedMostRecentEntriesByMessageActivity(int page, int itemsPerPage, TicketStatus ticketStatus, String searchkey) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>(itemsPerPage);
        var ticketFilter = switch (ticketStatus) {
            case ALL -> Filters.empty();
            case CLOSED -> Filters.eq("open", false);
            case OPEN -> Filters.eq("open", true);
        };
        logger.debug("filtering by {} with {} and search text '{}'", ticketStatus, ticketFilter, searchkey);
        var foundLogs = logCollection
                .find()
                .filter(Filters.not(Filters.size("messages", 0)))
                .sort(Sorts.descending("messages.timestamp"))
                .filter(Objects.nonNull(searchkey) && !searchkey.isBlank() ? Filters.and(ticketFilter, Filters.text(searchkey)) : ticketFilter)
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage);
        foundLogs.forEach(document -> {
            try {
                entries.add(objectMapper.readValue(document.toJson(), ModMailLogEntry.class));
            } catch (JsonProcessingException e) {
                logger.error(e);
            }
        });
        logger.trace("Entries: {}", entries);
        return entries;
    }

    /**
     * Determines the number of pages required to display every modmail entry at 8 per page
     *
     * @param ticketStatus ticket status to filter for
     * @return numbers of pages required to paginate all modmail logs
     */
    public int getPaginationCount(TicketStatus ticketStatus, String search) {
        return getPaginationCount(DEFAULT_ITEMS_PER_PAGE, ticketStatus, search);
    }

    /**
     * Determines the number of pages required to display every modmail entry at the given items per page
     *
     * @param itemsPerPage max entries per page
     * @param ticketStatus ticket status to filter for
     * @return numbers of pages required to paginate all modmail logs
     */
    public int getPaginationCount(int itemsPerPage, TicketStatus ticketStatus, String search) {
        return (int) (Math.ceil(getTotalTickets(ticketStatus, search) / (double) (itemsPerPage)));
    }

    public ModmailConfig getConfig() throws Exception {
        // Check to see if the config is cached
        if (cacheTime != null && Instant.now().isBefore(cacheTime.plus(5, ChronoUnit.MINUTES))) {
            logger.debug("grabbed cached config from {}", cacheTime.toString());
            cacheTime = Instant.now();
            return cachedConfig;
        }
        var conf = configCollection.find().first();
        if (conf != null) {
            try {
                logger.trace(conf.toJson());
                var config = objectMapper.readValue(conf.toJson(), ModmailConfig.class);
                // set cache
                cachedConfig = config;
                cacheTime = Instant.now();
                return config;
            } catch (JsonProcessingException e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        } else {
            throw new Exception("Config could not be loaded from mongodb");
        }

    }

    public Role getUserRole(SiteUser user) throws Exception {
        var levelPermissions = getConfig().getLevelPermissions();

        for (Map.Entry<Role, List<Long>> entry :
                levelPermissions.entrySet()) {
            if (entry.getValue().contains(user.getId())) {
                return entry.getKey();
            }
        }
        return Role.ANYONE;
    }

    public MongoCollection<Document> getConfigCollection() {
        return configCollection;
    }

    public MongoCollection<Document> getLogCollection() {
        return logCollection;
    }
}
