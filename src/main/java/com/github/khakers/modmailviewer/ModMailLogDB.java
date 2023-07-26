package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.auth.UserToken;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.data.ModmailConfig;
import com.github.khakers.modmailviewer.data.internal.TicketStatus;
import com.github.khakers.modmailviewer.util.SingleItemCache;
import com.mongodb.ConnectionString;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.Nullable;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.internal.MongoJackModule;

import java.util.*;

public class ModMailLogDB {

    public static final int DEFAULT_ITEMS_PER_PAGE = 8;
    private static final Logger logger = LogManager.getLogger();
    private final MongoDatabase database;
    private final MongoCollection<Document> configCollection;
    private final JacksonMongoCollection<ModMailLogEntry> logCollection;
    private final ObjectMapper objectMapper;

    private final SingleItemCache<ModmailConfig> configCache = new SingleItemCache<>(300000L, this::fetchConfig);

    public ModMailLogDB(MongoClient mongoClient, String connectionString) {

        this.objectMapper = new JsonMapper()
                .findAndRegisterModules()
                .registerModules(new MongoJackModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var connectionString1 = new ConnectionString(connectionString);


        this.database = mongoClient.getDatabase(connectionString1.getDatabase() == null ? "modmail_bot" : connectionString1.getDatabase());
        database.listCollectionNames().forEach(logger::debug);
        this.logCollection = JacksonMongoCollection.builder().withObjectMapper(objectMapper).build(database, "logs", ModMailLogEntry.class, UuidRepresentation.STANDARD);
        this.configCollection = database.getCollection("config");
        if (configCollection.countDocuments() > 1 && Config.BOT_ID == 0) {
            logger.warn("Multiple configuration documents were found in your MongoDB database. " +
                    "You *MUST* set the BOT_ID variable to your bots ID in order for the correct modmail configuration to be used.");
        }

        var result = logCollection.createIndex(Indexes.descending("messages.timestamp"));
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
        ModMailLogEntry result = logCollection.find(Filters.eq("_id", id)).limit(1).first();
        return Optional.ofNullable(result);
    }

    public List<ModMailLogEntry> getMostRecentEntries(int count) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        FindIterable<ModMailLogEntry> foundLogs = logCollection
                .find()
                .sort(Sorts.descending("created_at"))
                .limit(count);
        foundLogs.forEach(entries::add);
        return entries;
    }

    public List<ModMailLogEntry> getPaginatedMostRecentEntries(int page) {
        return getPaginatedMostRecentEntries(page, DEFAULT_ITEMS_PER_PAGE);
    }

    public List<ModMailLogEntry> getPaginatedMostRecentEntries(int page, int itemsPerPage) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        FindIterable<ModMailLogEntry> foundLogs = logCollection
                .find()
                .sort(Sorts.descending("created_at"))
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage);
        foundLogs.forEach(entries::add);
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
        FindIterable<ModMailLogEntry> foundLogs = logCollection
                .find()
                .filter(Filters.not(Filters.size("messages", 0)))
                .sort(Sorts.descending("messages.timestamp"))
                .filter(Objects.nonNull(searchkey) && !searchkey.isBlank() ? Filters.and(ticketFilter, Filters.text(searchkey)) : ticketFilter)
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage);
        foundLogs.forEach(entries::add);

        logger.trace("Entries: {}", entries);
        return entries;
    }

    public List<ModMailLogEntry> getAllLogs() {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();

        FindIterable<ModMailLogEntry> foundLogs = logCollection
                .find();
        foundLogs.forEach(entries::add);

        logger.trace("Entries: {}", entries.size());
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
        return configCache.getItem();
    }

    public Role getUserOrGuildRole(UserToken user, long[] roles) throws Exception {
        var levelPermissions = getConfig().getLevelPermissions();

        var role = Role.ANYONE;

        for (Map.Entry<Role, List<Long>> entry :
                levelPermissions.entrySet()) {
            // Check if user ID matches
            if (entry.getValue().contains(user.getId())) {
                var foundRole = entry.getKey();
                logger.trace("Matched user by their ID to role with permission level {}", entry.getKey());
                // We always want to get the user's highest possible role
                if (foundRole.value > role.value) {
                    role = foundRole;
                }
            }
            // Check if a role id matches
            for (long roleID :
                    roles) {
                if (entry.getValue().contains(roleID)) {
                    var foundRole = entry.getKey();
                    logger.trace("Matched user to role id {} with permission level {}", roleID, foundRole);
                    if (foundRole.value > role.value) {
                        role = foundRole;
                    }
                }
            }
        }
        return role;

    }

    public Role getUserRole(UserToken user) throws Exception {
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

    public JacksonMongoCollection<ModMailLogEntry> getLogCollection() {
        return logCollection;
    }

    /**
     * This fetches the config from the database with no caching
     * DO NOT CALL DIRECTLY
     *
     * @return the config from the database
     */
    private ModmailConfig fetchConfig() throws Exception {
        Document conf = null;
        if (Config.BOT_ID == 0) {
            conf = configCollection.find().first();
        } else {
            conf = configCollection.find(Filters.eq("bot_id", Config.BOT_ID)).first();
        }

        if (conf != null) {
            try {
                logger.trace(conf.toJson());
                var config = objectMapper.readValue(conf.toJson(), ModmailConfig.class);
                return config;
            } catch (JsonProcessingException e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        } else {
            throw new Exception("Config could not be loaded from mongodb");
        }
    }
}
