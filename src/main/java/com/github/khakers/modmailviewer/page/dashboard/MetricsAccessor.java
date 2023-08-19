package com.github.khakers.modmailviewer.page.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.khakers.modmailviewer.data.MessageType;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.util.Constants;
import com.github.khakers.modmailviewer.util.DateFormatters;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.internal.MongoJackModule;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static com.github.khakers.modmailviewer.util.DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME;

public class MetricsAccessor {

    private static final Logger logger = LogManager.getLogger();


    private final MongoCollection<Document> logAggregateCollection;

    private final MongoCollection<ModMailLogEntry> logCollection;
    
    private final ObjectMapper objectMapper = JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .addModule(new Jdk8Module())
          .addModule(new MongoJackModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
          .build();

    public MetricsAccessor(MongoDatabase modmailDatabase) {
        this.logAggregateCollection = modmailDatabase.getCollection(Constants.MODMAIL_LOG_COLLECTION_NAME);
        this.logCollection = JacksonMongoCollection
              .builder()
              .withObjectMapper(objectMapper)
              .build(modmailDatabase, Constants.MODMAIL_LOG_COLLECTION_NAME, ModMailLogEntry.class, UuidRepresentation.STANDARD);
    }

    public String getTicketsPerDayJson(int period) {
        try {
            var date = LocalDate.now(ZoneId.of("UTC"));
            var days = new LocalDate[period];
            var daysString = new String[period];
            for (int i = 0; i < period; i++) {
                days[i] = date.minusDays(period - i);
                daysString[i] = PYTHON_STR_ISO_OFFSET_DATE_TIME.format(days[i].atStartOfDay());
            }
            logger.trace(Arrays.toString(days));
            logger.trace(Arrays.stream(daysString).toList());

            logger.debug("looking for days between {} and {}", daysString[0], DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME.format(days[period - 1].plusDays(1).atStartOfDay()));

            var results = this.logAggregateCollection.aggregate(List.of(
                            Aggregates.facet(
                                    new Facet("OPEN",
                                            Aggregates.match(
                                                    Filters.and(
                                                            Filters.gte("created_at", daysString[0]),
                                                            Filters.lt("created_at", DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME.format(days[period - 1].plusDays(1).atStartOfDay())))),
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
                                                            Filters.lt("closed_at", DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME.format(days[period - 1].plusDays(1).atStartOfDay())))),
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
                    )
            );

            var open = (List<Document>) results.first().get("OPEN");
            var close = (List<Document>) results.first().get("CLOSE");

            try {
                logger.trace(this.objectMapper.writeValueAsString(open));
            } catch (JsonProcessingException e) {
                logger.throwing(e);
                return "";
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

            return this.objectMapper.writeValueAsString(new ChartData<>(data, Arrays.stream(days)
                  .map(DateFormatters.MINI_DATE_FORMAT::format)
                  .toArray(String[]::new)));
        } catch (JsonProcessingException e) {
            logger.throwing(e);
            return "";
        }
    }

    public String getTicketClosersJson() {
        try {
            var map = new LinkedHashMap<String, Integer>(11, 1);
            var foo = this.logAggregateCollection.aggregate(List.of(
                  Aggregates.match(Filters.eq("open", false)),
                  Aggregates.group("$closer.id",
                        Accumulators.sum("count", 1),
                        Accumulators.first("name", "$closer.name")),
                  Aggregates.limit(10),
                  Aggregates.sort(Sorts.descending("count"))));

            foo.forEach(document -> {
                logger.trace(document.toJson());
                map.put(document.getString("name"), document.getInteger("count"));
            });
            var results = Collections.unmodifiableMap(map);
            logger.trace(results);
            String[] labels = new String[results.size()];
            Integer[] data = new Integer[results.size()];
            results.keySet().toArray(labels);
            results.values().toArray(data);
            return this.objectMapper.writeValueAsString(new ChartData<>(data, labels));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getUnansweredTicketCount() {
        var foo = this.logAggregateCollection.aggregate(
              List.of(
                    Aggregates.match(Filters.eq("open", true)),
                    Aggregates.match(
                          Filters.or(
                                Filters.not(Filters.size("messages", 0)),
                                Filters.not(
                                      Filters.elemMatch(
                                            "messages",
                                            Filters.and(
                                                  Filters.eq("type", "thread_message"),
                                                  Filters.eq("author.mod", true),
                                                  Filters.not(
                                                        Filters.eq("author.id", "$$recipient.id")
                                                  )
                                            )
                                      )
                                ))

                    ),
                    Aggregates.count()
              ));
        if (foo.first() == null) {
            return 0;
        }
        return foo.first().getInteger("count", 0);
    }

    public String getMedianResolutionTime(int period) {
        var resolutionTimes = getResolutionTimes(period);

        double medianTime;
//        resolutionTimes.max
        return "";
    }

    public Duration getAverageFirstReplyTime(int period) {
        var foo = this.logCollection.aggregate(
              List.of(
                    // Needs to be log where the number of messages that are not system messages and not by the recipient
                    Aggregates.match(
                          Filters.gte(
                                "created_at",
                                DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME.format(
                                      LocalDate.now(ZoneId.of("UTC")).atStartOfDay().minusDays(period))
                          )),
                    Aggregates.match(Filters.exists("messages.1")),
                    Aggregates.match(
                          Filters.elemMatch(
                                "messages",
                                Filters.and(
                                      Filters.eq("type", "thread_message"),
                                      Filters.eq("author.mod", true),
                                      Filters.not(
                                            Filters.eq("author.id", "$$recipient.id")
                                      )
                                )
                          )
                    )
              )
        );
        var values = new ArrayList<Integer>();
        foo.forEach(logEntry -> {
            logger.trace(logEntry);
            logEntry.getMessages().stream()
                  .filter(message -> message.getType().equals(MessageType.thread))
                  .filter(message -> !message.getAuthor().equals(logEntry.getRecipient()))
                  .findFirst()
                  .ifPresentOrElse(message -> {
                            var duration = Duration.between(logEntry.getCreationTime(), message.getCreationTime());
                            logger.trace("Duration between log creation and first matching message was {}", duration);
                            values.add((int) duration.toSeconds());
                        },
                        () -> logger.error("Tried to calculate the reply time delta for message id {}, but a result was returned by the query that was filtered out by the application. LogEntry: {}", logEntry.get_id(), logEntry.toString()));

        });
        return Duration.ofSeconds((long) values.stream().mapToInt(value -> value).average().orElse(0.0));
    }

    public Duration getAverageResolutionTime(int period) {
        var avg = getResolutionTimes(period)
              .stream()
              .mapToInt(value -> value)
              .average()
              .orElse(0.0);

        return Duration.ofSeconds((long) avg);
    }

    /**
     * @return A list containing a random sample of the resolution time of 250 tickets
     */
    private List<Integer> getResolutionTimes(int period) {
        // Ideally we would be doing this just using a query, but we can't use any of the built-in
        // mongodb time queries because modmail stores everything as a string, thus making everything harder for no reason
        // (technically I believe you can convert strings to date objects, but I suspect it has poor performance and might not even work because this isn't quite iso8601 format for some reason)
        // This query filters for closed tickets, grabs a random sample of 250, and then gets a document of just the  closed at and opened at values of those documents
        var query = this.logAggregateCollection.aggregate(
              List.of(
                    Aggregates.match(Filters.eq("open", false)),
                    Aggregates.match(
                          Filters.gte(
                                "created_at",
                                DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME.format(
                                      LocalDate.now(ZoneId.of("UTC")).atStartOfDay().minusDays(period))
                          )),
                    Aggregates.sample(250),
                    Aggregates.project(new Document("closed_at", "$closed_at")
                          .append("created_at", "$created_at"))
              )
        );
        var values = new ArrayList<Integer>();
        query.forEach(document -> {
            var creationTime = PYTHON_STR_ISO_OFFSET_DATE_TIME.parse(document.get("created_at", String.class), Instant::from);
            var closedTime = PYTHON_STR_ISO_OFFSET_DATE_TIME.parse(document.get("closed_at", String.class), Instant::from);
            var duration = Duration.between(creationTime, closedTime);
            values.add(Math.toIntExact(duration.toSeconds()));
        });
        return values;
    }
}
