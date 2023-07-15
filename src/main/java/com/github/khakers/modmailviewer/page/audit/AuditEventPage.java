package com.github.khakers.modmailviewer.page.audit;

import com.github.khakers.modmailviewer.Page;
import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import io.javalin.http.Context;

public class AuditEventPage extends Page {
    public final String PATH = "/audit";


    public final AuditEvent auditEvent;


    public AuditEventPage(Context ctx, AuditEvent auditEvent) {
        super(ctx);
        this.auditEvent = auditEvent;
    }

    @Override
    public String getTemplate() {
        return "pages/AuditEventView.jte";
    }
}
