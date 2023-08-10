package com.github.khakers.modmailviewer.page.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.khakers.modmailviewer.Main;
import com.github.khakers.modmailviewer.data.MessageType;
import com.github.khakers.modmailviewer.util.DateFormatters;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.khakers.modmailviewer.util.DateFormatters.DATABASE_TIMESTAMP_FORMAT;

public class MetricsAccessor {

    private static final Logger logger = LogManager.getLogger();

    public String getTicketsPerDayJson(int period) {
        try {
            return Main.db.getObjectMapper().writeValueAsString(Main.db.getTicketsActionsPerDay(period));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTicketClosersJson() {
        try {
            Map<String, Integer> results = Main.db.getTicketsClosedByUserOrdered();
            String[] labels = new String[results.size()];
            Integer[] data = new Integer[results.size()];
            results.keySet().toArray(labels);
            results.values().toArray(data);
            return Main.db.getObjectMapper().writeValueAsString(new ChartData<>(data, labels));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getUnansweredTicketCount() {
        var foo = Main.db.getLogAggregateCollection().aggregate(
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

        var foo = Main.db.getLogCollection().aggregate(
              List.of(
                    // Needs to be log where the number of messages that are not system messages and not by the recipient
                    Aggregates.match(
                          Filters.gte(
                                "created_at",
                                DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(
                                      LocalDate.now(ZoneId.of("UTC")).atStartOfDay().minusDays(period))
                          )),
                    Aggregates.match(Filters.not(Filters.size("messages", 0))),
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
                        () -> {
                            logger.error("Tried to calculate the reply time delta for message id {}, but a result was returned by the query that was filtered out by the application. LogEntry: {}", logEntry.get_id(), logEntry.toString());
                        });

        });
        return Duration.ofSeconds((long) values.stream().mapToInt(value -> value).average().orElse(0.0));
    }

    public Duration getAverageResolutionTime(int period) {
        long sum = 0;
        var avg = getResolutionTimes(period).stream().mapToInt(value -> value).average().orElse(0.0);
//        var resolutionTimes = getResolutionTimes().stream().mapToInt(value -> value).average().orElse(0.0);
//        for (Integer integer : resolutionTimes) {
//            sum += integer;
//        }
//        var avg = sum / resolutionTimes.size();

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
        var query = Main.db.getLogAggregateCollection().aggregate(
              List.of(
                    Aggregates.match(Filters.eq("open", false)),
                    Aggregates.match(
                          Filters.gte(
                                "created_at",
                                DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(
                                      LocalDate.now(ZoneId.of("UTC")).atStartOfDay().minusDays(period))
                          )),
                    Aggregates.sample(250),
                    Aggregates.project(new Document("closed_at", "$closed_at")
                          .append("created_at", "$created_at"))
              )
        );
        var values = new ArrayList<Integer>();
        query.forEach(document -> {

            var creationTime = DATABASE_TIMESTAMP_FORMAT.parse(document.get("created_at", String.class), Instant::from);
            var closedTime = DATABASE_TIMESTAMP_FORMAT.parse(document.get("closed_at", String.class), Instant::from);
            var duration = Duration.between(creationTime, closedTime);
            values.add(Math.toIntExact(duration.toSeconds()));
        });
        return values;
    }
}
