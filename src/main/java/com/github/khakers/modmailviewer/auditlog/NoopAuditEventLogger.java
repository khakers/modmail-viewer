package com.github.khakers.modmailviewer.auditlog;

import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoopAuditEventLogger implements OutboundAuditEventLogger {

    private static final Logger logger = LogManager.getLogger();

    public NoopAuditEventLogger() {
        logger.debug("using NoopAuditLogger");
    }

    @Override
    public void pushEvent(AuditEvent event) {
    }

    @Override
    public void pushAuditEventWithContext(Context ctx, String event, String description) throws Exception {
    }
}
