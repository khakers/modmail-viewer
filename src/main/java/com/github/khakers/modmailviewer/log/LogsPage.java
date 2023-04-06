package com.github.khakers.modmailviewer.log;

import com.github.khakers.modmailviewer.ModMailLogDB;
import com.github.khakers.modmailviewer.Page;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.data.internal.TicketStatus;
import io.javalin.http.Context;

import java.util.List;

import static com.github.khakers.modmailviewer.Main.db;

public class LogsPage extends Page {

    public final String PATH = "/logs";

    public final List<ModMailLogEntry> logEntries;

    public final int currentPage;
    public final int pageCount;
    public final ModMailLogDB modMailLogDB = db;
    public final TicketStatus ticketStatusFilter;
    public final boolean showNSFW;
    public final String searchString;

    public LogsPage(Context ctx) {
        super(ctx);

        int page1;

        page1 = ctx.queryParamAsClass("page", Integer.class)
                .check(integer -> integer >= 1, "page must be at least 1")
                .getOrDefault(1);

        String statusFilter = ctx.queryParamAsClass("status", String.class)
                .check(s -> s.equalsIgnoreCase(String.valueOf(TicketStatus.OPEN))
                        || s.equalsIgnoreCase(String.valueOf(TicketStatus.CLOSED))
                        || s.equalsIgnoreCase(String.valueOf(TicketStatus.ALL)), "")
                .getOrDefault("ALL");

        showNSFW = ctx.queryParamAsClass("nsfw", Boolean.class)
                .getOrDefault(Boolean.TRUE);

        searchString = ctx.queryParamAsClass("search", String.class)
                .check(s -> s.length() > 0 && s.length() < 120
                        , "search text cannot be greater than 50 characters")
                .getOrDefault("");

        ticketStatusFilter = TicketStatus.valueOf(statusFilter.toUpperCase());
        pageCount = db.getPaginationCount(ticketStatusFilter, searchString);
        page1 = Math.min(pageCount, page1);
        currentPage = page1;
        logEntries = db.searchPaginatedMostRecentEntriesByMessageActivity(currentPage, ticketStatusFilter, searchString);
    }

    @Override
    public String getTemplate() {
        return "pages/LogListView.jte";
    }
}
