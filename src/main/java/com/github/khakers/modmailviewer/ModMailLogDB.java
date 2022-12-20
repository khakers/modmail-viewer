package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.auth.SiteUser;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.data.ModmailConfig;
import com.github.khakers.modmailviewer.data.internal.ChartData;
import com.github.khakers.modmailviewer.data.internal.TicketStatus;
import com.github.khakers.modmailviewer.util.DateFormatters;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
        logger.debug(result);

    }

    public int getTotalTickets(TicketStatus ticketStatus) {
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

    /**
     * Calculate the amount of tickets each moderator has closed
     * Due to limitations introduced by modmailbot quirks, We determine if a user is a mod based on if their discord id is listed in the config as at least a mod
     *
     * @return An unmodifiable map with key value pairs of the Users name and the amount of tickets they have closed
     */
    public Map<String, Integer> getTicketsClosedByUser() throws Exception {
        var map = new HashMap<String, Integer>();
        var perms = getConfig().getFlatUserPerms();
        logCollection.find(Filters.eq("open", false)).forEach(document -> {
            // It appears that the user closing the ticket is *always* a mod
            // This sucks but
            var closer = document.get("closer", Document.class);
//            var mod = closer.getBoolean("mod");
            var mod = perms.containsKey(Long.parseLong(closer.getString("id")));
            String key = mod ? closer.getString("name") + "#" + closer.getString("discriminator") : "user";

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
                .forEach(document -> {
                    try {
                        logger.debug(document);
                        entries.add(objectMapper.readValue(document.toJson(), ModMailLogEntry.class));
                    } catch (JsonProcessingException e) {
                        logger.error(e);
                        throw new RuntimeException(e);
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
        return getPaginatedMostRecentEntriesByMessageActivity(page, DEFAULT_ITEMS_PER_PAGE, ticketStatus);
    }

    /**
     * Returns a list of log entries based on their most recent activity
     *
     * @param page
     * @param itemsPerPage
     * @return
     */
    public List<ModMailLogEntry> getPaginatedMostRecentEntriesByMessageActivity(int page, int itemsPerPage, TicketStatus ticketStatus) {
        ArrayList<ModMailLogEntry> entries = new ArrayList<>();
        var ticketFilter = switch (ticketStatus) {
            case ALL -> Filters.empty();
            case CLOSED -> Filters.eq("open", false);
            case OPEN -> Filters.eq("open", true);
        };
        logger.debug("filtering by {} with {}", ticketStatus, ticketFilter);
        var foundLogs = logCollection
                .find()
                .filter(Filters.not(Filters.size("messages", 0)))
                .sort(Sorts.descending("messages.timestamp"))
                .filter(ticketFilter)
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

    public ChartData getTicketsPerDay(int period, TicketStatus ticketStatus) {
        var date = LocalDate.now();
        var days = new LocalDate[period];
        var daysString = new String[period];
        for (int i = 0; i < period; i++) {
            days[i] = date.minusDays(period - i);
            daysString[i] = DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[i].atStartOfDay());
        }
        System.out.println(Arrays.toString(days));
        System.out.println(Arrays.stream(daysString).toList());
        logger.debug("looking for days between {} and {}", daysString[0], DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(days[period - 1].plusDays(1).atStartOfDay()));

        var results = switch (ticketStatus) {
            //todo Support or remove ALL
            case ALL -> logCollection.aggregate(List.of(
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
            case OPEN -> logCollection.aggregate(Arrays.asList(
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
            case CLOSED -> logCollection.aggregate(Arrays.asList(
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
        };

//        System.out.println("results = " + results.into());

        if (ticketStatus==TicketStatus.ALL) {
            results.forEach(document -> System.out.println(document.toString()));

            var l = new Integer[period];
            java.util.Arrays.fill(l, 0);
            return new ChartData<>(l, Arrays.stream(days)
                    .map(DateFormatters.MINI_DATE_FORMAT::format)
                    .toArray(String[]::new));
        } else {
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
    }

    /**
     * Determines the number of pages required to display every modmail entry at 8 per page
     *
     * @param ticketStatus ticket status to filter for
     * @return numbers of pages required to paginate all modmail logs
     */
    public int getPaginationCount(TicketStatus ticketStatus) {
        return getPaginationCount(DEFAULT_ITEMS_PER_PAGE, ticketStatus);
    }

    /**
     * Determines the number of pages required to display every modmail entry at the given items per page
     *
     * @param itemsPerPage max entries per page
     * @param ticketStatus ticket status to filter for
     * @return numbers of pages required to paginate all modmail logs
     */
    public int getPaginationCount(int itemsPerPage, TicketStatus ticketStatus) {
        return (int) (Math.ceil(getTotalTickets(ticketStatus) / (double) (itemsPerPage)));
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
