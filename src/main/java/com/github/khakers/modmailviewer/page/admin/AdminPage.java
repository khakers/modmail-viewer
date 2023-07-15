package com.github.khakers.modmailviewer.page.admin;

import com.github.khakers.modmailviewer.Main;
import com.github.khakers.modmailviewer.Page;
import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import io.javalin.http.Context;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class AdminPage extends Page {

    public final String PATH = "/admin";

    public final Instant startTime;
    public final Instant endTime;

    public final List<Long> users;

    public final List<String> actions;

    public final ZoneId tz;

    public final List<AuditEvent> auditEvents;

    AdminPage(Context ctx, Instant rangeStartTime, Instant rangeEndTime, List<Long> users, List<String> actions, ZoneId tz) {
        super(ctx);
        this.startTime = rangeStartTime;
        this.endTime = rangeEndTime;
        this.users = users;
        this.actions = actions;
        this.tz = tz;
        auditEvents = Main.AuditLogClient.searchAuditEvents(rangeStartTime, rangeEndTime, users, actions);
    }

    @Override
    public String getTemplate() {
        return "pages/AdminView.jte";
    }
}
