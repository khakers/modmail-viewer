package com.github.khakers.modmailviewer.auditlog;

import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import io.javalin.http.Context;

/**
 * Interface for logging outbound audit events
 *
 * only classes that intend to push audit events should implement this interface
 */
public interface OutboundAuditEventLogger {

    public void pushEvent(AuditEvent event);

    public void pushAuditEventWithContext(Context ctx, String event, String description) throws Exception;
}
