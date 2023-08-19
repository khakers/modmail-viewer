package com.github.khakers.modmailviewer.log;

import com.github.khakers.modmailviewer.Main;
import com.github.khakers.modmailviewer.ModMailLogDB;
import com.github.khakers.modmailviewer.Page;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import com.github.khakers.modmailviewer.data.internal.TicketStatus;
import io.javalin.http.Context;

import java.util.List;

import static com.github.khakers.modmailviewer.Main.modMailLogDB;

public class LogsPage extends Page {

    public final String PATH = "/logs";

    public final List<ModMailLogEntry> logEntries;

    public final int currentPage;
    public final int pageCount;
    public final ModMailLogDB modMailLogDB = Main.modMailLogDB;
    public final TicketStatus ticketStatusFilter;
    public final boolean showNSFW;
    public final String searchString;

    public final int itemsPerPage;

    public LogsPage(Context ctx) {
        super(ctx);

        int page1;

        page1 = ctx.queryParamAsClass("page", Integer.class)
                .check(integer -> integer >= 1, "page must be at least 1")
                .getOrDefault(1);

        String statusFilter = ctx.queryParamAsClass("status", String.class)
                .check(s -> s.equalsIgnoreCase(String.valueOf(TicketStatus.OPEN))
                        || s.equalsIgnoreCase(String.valueOf(TicketStatus.CLOSED))
                        || s.equalsIgnoreCase(String.valueOf(TicketStatus.ALL)), "Invalid status filter value")
                .getOrDefault("ALL");

        showNSFW = ctx.queryParamAsClass("nsfw", Boolean.class)
                .getOrDefault(Boolean.TRUE);

        searchString = ctx.queryParamAsClass("search", String.class)
                .check(s -> s.length() < 120
                        , "search text cannot be greater than 50 characters")
                .getOrDefault("");

        this.itemsPerPage = ctx.queryParamAsClass("itemsPerPage", Integer.class)
                .check(integer -> integer >= 1, "itemsPerPage must be at least 1")
                .check(integer -> integer <= 50, "itemsPerPage cannot be greater than 50")
                .getOrDefault(8);

        ticketStatusFilter = TicketStatus.valueOf(statusFilter.toUpperCase());
        pageCount = Main.modMailLogDB.getPaginationCount(itemsPerPage, ticketStatusFilter, searchString);
        page1 = Math.min(pageCount, page1);
        currentPage = page1;
        logEntries = Main.modMailLogDB.searchPaginatedMostRecentEntriesByMessageActivity(currentPage, itemsPerPage, ticketStatusFilter, searchString);
    }

    @Override
    public String getTemplate() {
        return "pages/LogListView.jte";
    }
}
