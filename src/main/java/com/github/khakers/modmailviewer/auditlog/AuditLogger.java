package com.github.khakers.modmailviewer.auditlog;

import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import io.javalin.http.Context;

public interface AuditLogger {

    public void pushEvent(AuditEvent event);

    public void pushAuditEventWithContext(Context ctx, String event, String description) throws Exception;
}
