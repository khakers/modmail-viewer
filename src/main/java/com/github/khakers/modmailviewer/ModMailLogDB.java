package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.auth.UserToken;
import com.github.khakers.modmailviewer.dashboard.ChartData;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.data.ModmailConfig;
import com.github.khakers.modmailviewer.data.internal.TicketStatus;
import com.github.khakers.modmailviewer.util.DateFormatters;
import com.github.khakers.modmailviewer.util.SingleItemCache;
import com.mongodb.ConnectionString;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.Nullable;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.internal.MongoJackModule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ModMailLogDB {

    public static final int DEFAULT_ITEMS_PER_PAGE = 8;
    private static final Logger logger = LogManager.getLogger();
    private final MongoDatabase database;
    private final MongoCollection<Document> configCollection;
    private final JacksonMongoCollection<ModMailLogEntry> logCollection;
    private final MongoCollection<Document> logAggregateCollection;
    private final ObjectMapper objectMapper;
    private final SingleItemCache<ModmailConfig> configCache = new SingleItemCache<>(300000L, this::fetchConfig);

    public ModMailLogDB(MongoClient mongoClient, String connectionString) {

        this.objectMapper = JsonMapper.builder()
              .addModule(new JavaTimeModule())
              .addModule(new Jdk8Module())
              .addModule(new MongoJackModule())
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
              .withConfigOverride(Instant.class, cfg -> cfg.setFormat(JsonFormat.Value.forPattern(DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME_STRING)))
              .build();

        var connectionString1 = new ConnectionString(connectionString);

        this.database = mongoClient.getDatabase(connectionString1.getDatabase() == null ? "modmail_bot" : connectionString1.getDatabase());
        database.listCollectionNames().forEach(logger::debug);
        this.logCollection = JacksonMongoCollection.builder().withObjectMapper(objectMapper).build(database, "logs", ModMailLogEntry.class, UuidRepresentation.STANDARD);
        this.logAggregateCollection = database.getCollection("logs");
        this.configCollection = database.getCollection("config");
        if (configCollection.countDocuments() > 1 && Config.BOT_ID == 0) {
            logger.warn("Multiple configuration documents were found in your MongoDB database. " +
                    "You *MUST* set the BOT_ID variable to your bots ID in order for the correct modmail configuration to be used.");
        }

        var result = logCollection.createIndex(Indexes.descending("messages.timestamp"));
        logger.debug(result);

    }

    public MongoCollection<Document> getLogAggregateCollection() {
        return logAggregateCollection;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public int getTotalTickets(TicketStatus ticketStatus) {
        return getTotalTickets(ticketStatus, null);
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
        try {
            foundLogs.forEach(entries::add);
        } catch (Exception e) {
            logger.error("an exception occurred while trying to parse");
            throw e;
        }

        logger.trace("Entries: {}", entries);
        return entries;
    }

    public ChartData<Integer[], String> getTicketsActionsPerDay(int period) {
        var date = LocalDate.now(ZoneId.of("UTC"));
        var days = new LocalDate[period];
        var daysString = new String[period];
        for (int i = 0; i < period; i++) {
            days[i] = date.minusDays(period - i);
            daysString[i] = DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[i].atStartOfDay());
        }
        logger.trace(Arrays.toString(days));
        logger.trace(Arrays.stream(daysString).toList());

        logger.debug("looking for days between {} and {}", daysString[0], DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[period - 1].plusDays(1).atStartOfDay()));

        var results = logAggregateCollection.aggregate(List.of(
                        Aggregates.facet(
                                new Facet("OPEN",
                                        Aggregates.match(
                                                Filters.and(
                                                        Filters.gte("created_at", daysString[0]),
                                                        Filters.lt("created_at", DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[period - 1].plusDays(1).atStartOfDay())))),
                                        Aggregates.bucket(
                                                "$created_at",
                                                Arrays.stream(daysString).toList(),
                                                new BucketOptions()
                                                        .defaultBucket("unknown")
                                                        .output(
                                                                Accumulators.sum("count", 1),
                                                                Accumulators.push("created_at", "$created_at"))
                                        )),
                                new Facet("CLOSE",
                                        Aggregates.match(Filters.eq("open", false)),
                                        Aggregates.match(
                                                Filters.and(
                                                        Filters.gte("closed_at", daysString[0]),
                                                        Filters.lt("closed_at", DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[period - 1].plusDays(1).atStartOfDay())))),
                                        Aggregates.bucket(
                                                "$closed_at",
                                                Arrays.stream(daysString).toList(),
                                                new BucketOptions()
                                                        .defaultBucket("unknown")
                                                        .output(
                                                                Accumulators.sum("count", 1),
                                                                Accumulators.push("closed_at", "$closed_at"))
                                        )
                                ))
//                            Aggregates.match(Filters.eq("open", false)),

                )
        );

        var open = (List<Document>) results.first().get("OPEN");
        var close = (List<Document>) results.first().get("CLOSE");

        try {
            logger.trace(objectMapper.writeValueAsString(open));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var data = new Integer[2][period];
        for (Integer[] datum : data) {
            Arrays.fill(datum, 0);
        }
        open.forEach(document -> {
            var time = document.getString("_id");
            var index = 0;
            for (int i = 0; i < daysString.length; i++) {
                if (daysString[i].equals(time)) {
                    index = i;
                }
            }
            data[0][index] = document.getInteger("count");
        });
        close.forEach(document -> {
            var time = document.getString("_id");
            var index = 0;
            for (int i = 0; i < daysString.length; i++) {
                if (daysString[i].equals(time)) {
                    index = i;
                }
            }
            data[1][index] = document.getInteger("count");
        });
        return new ChartData<>(data, Arrays.stream(days)
                .map(DateFormatters.MINI_DATE_FORMAT::format)
                .toArray(String[]::new));

    }

    public ChartData<Integer, String> getTicketsPerDay(int period, TicketStatus ticketStatus) {
        if (ticketStatus == TicketStatus.ALL) {
            throw new IllegalArgumentException("TicketStatus ALL is not supported by this method, use getTicketActionsPerDay instead.");
        }
        var date = LocalDate.now(ZoneId.of("UTC"));
        var days = new LocalDate[period];
        var daysString = new String[period];
        for (int i = 0; i < period; i++) {
            days[i] = date.minusDays(period - i);
            daysString[i] = DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[i].atStartOfDay());
        }
        logger.trace(Arrays.toString(days));
        logger.trace(Arrays.stream(daysString).toList());

        logger.debug("looking for days between {} and {}", daysString[0], DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[period - 1].plusDays(1).atStartOfDay()));

        var results = switch (ticketStatus) {
            //todo Support or remove ALL
            case OPEN -> logAggregateCollection.aggregate(Arrays.asList(
//                            Aggregates.match(Filters.eq("open", false)),
                            Aggregates.match(
                                    Filters.and(
                                            Filters.gte("created_at", daysString[0]),
                                            Filters.lt("created_at", DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[period - 1].plusDays(1).atStartOfDay())))),
                            Aggregates.bucket(
                                    "$created_at",
                                    Arrays.stream(daysString).toList(),
                                    new BucketOptions()
                                            .defaultBucket("unknown")
                                            .output(
                                                    Accumulators.sum("count", 1),
                                                    Accumulators.push("created_at", "$created_at"))
                            )
                    )
            );
            case CLOSED -> logAggregateCollection.aggregate(Arrays.asList(
                            Aggregates.match(Filters.eq("open", false)),
                            Aggregates.match(
                                    Filters.and(
                                            Filters.gte("closed_at", daysString[0]),
                                            Filters.lt("closed_at", DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[period - 1].plusDays(1).atStartOfDay())))),
                            Aggregates.bucket(
                                    "$closed_at",
                                    Arrays.stream(daysString).toList(),
                                    new BucketOptions()
                                            .defaultBucket("unknown")
                                            .output(
                                                    Accumulators.sum("count", 1),
                                                    Accumulators.push("closed_at", "$closed_at"))
                            )
                    )
            );
            default -> throw new IllegalStateException("Unexpected value: " + ticketStatus);
        };

        var countList = new Integer[period];
        java.util.Arrays.fill(countList, 0);

        results.forEach(document -> {
            var time = document.getString("_id");
            var index = 0;
            for (int i = 0; i < daysString.length; i++) {
                if (daysString[i].equals(time)) {
                    index = i;
                }
            }
            countList[index] = document.getInteger("count");
        });

        logger.trace(Arrays.toString(daysString));
        logger.trace(Arrays.toString(countList));

        return new ChartData<>(countList, Arrays.stream(days)
                .map(DateFormatters.MINI_DATE_FORMAT::format)
                .toArray(String[]::new));
    }


    /**
     * Calculate the amount of tickets each moderator has closed
     * Due to limitations introduced by modmailbot quirks, We determine if a user is a mod based on if their discord id is listed in the config as at least a mod
     *
     * @return An unmodifiable map with key value pairs of the Users name and the amount of tickets they have closed
     */
    public Map<String, Integer> getTicketsClosedByUser() throws Exception {
        var map = new HashMap<String, Integer>();
        var perms = getConfig().getFlatUserPerms();
        logCollection.find(Filters.eq("open", false)).forEach(logEntry -> {
            // It appears that the user closing the ticket is *always* a mod
            // This sucks but
            var closer = logEntry.getCloser().get();
            var mod = perms.containsKey(Long.parseLong(closer.id()));
            String key = mod ? closer.name() + "#" + closer.discriminator() : "user";

            map.merge(key, 1, Integer::sum);
        });
        return Collections.unmodifiableMap(map);
    }

    public Map<String, Integer> getTicketsClosedByUserOrdered() throws Exception {
        var map = getTicketsClosedByUser();

        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public List<ModMailLogEntry> getLogsWithRecentActivity(int limit) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>(limit);
        logCollection
                .find()
                .filter(Filters.not(Filters.size("messages", 0)))
                .sort(Sorts.descending("messages.timestamp"))
                .limit(limit)
                .forEach(entries::add);
        return entries;
    }


    public List<ModMailLogEntry> getAllLogs() {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        logCollection
                .find()
                .forEach(entries::add);
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
