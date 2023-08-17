package com.github.khakers.modmailviewer.page.admin;

import com.github.khakers.modmailviewer.auditlog.AuditEventDAO;
import io.javalin.http.Handler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class AdminController {

    static final Pattern userPattern = Pattern.compile("(\\d{16,20})[,\\s]*?");
    static final Pattern actionPattern = Pattern.compile("([\\w.]+)[,\\s]*?");
    private AuditEventDAO auditLogClient;
    public Handler serveAdminPage = ctx -> {

        var tz = ctx.queryParamAsClass("tz", ZoneId.class).getOrDefault(ZoneOffset.UTC);

        // Get start time (the beginning of the time range)

        var startDate = ctx.queryParamAsClass("startDate", LocalDate.class).getOrDefault(LocalDate.now());
        var startTime = ctx.queryParamAsClass("startTime", LocalTime.class).getOrDefault(LocalTime.MIDNIGHT);

        var localRangeStart = startDate.atTime(startTime);
        var rangeStart = localRangeStart.toInstant(tz.getRules().getOffset(localRangeStart));

        // Get end time (the end of the time range)

        var endDate = ctx.queryParamAsClass("endDate", LocalDate.class).getOrDefault(LocalDate.now());
        var endTime = ctx.queryParamAsClass("endTime", LocalTime.class).getOrDefault(LocalTime.MAX);

        var localRangeEnd = endDate.atTime(endTime);
        var rangeEnd = localRangeEnd.toInstant(tz.getRules().getOffset(localRangeEnd));

        var userString = ctx.queryParam("users");

        // Parse user ids
        List<Long> users;
        if (userString != null && !userString.isEmpty()) {
            users = userPattern.matcher(userString).results().map(MatchResult::group).map(Long::parseLong).toList();
        } else {
            users = List.of();
        }

        var actionString = ctx.queryParam("actions");

        // Parse actions
        List<String> actions;
        if (actionString != null && !actionString.isEmpty()) {
            actions = actionPattern.matcher(actionString).results().map(MatchResult::group).toList();
        } else {
            actions = List.of();
        }

        var page = new AdminPage(ctx, rangeStart, rangeEnd, users, actions, tz, this.auditLogClient.searchAuditEvents(rangeStart, rangeEnd, users, actions));

        page.render();
    };

    public AdminController(AuditEventDAO auditLogClient) {
        Objects.requireNonNull(auditLogClient, "auditLogClient cannot be null");
        this.auditLogClient = auditLogClient;
    }
}
