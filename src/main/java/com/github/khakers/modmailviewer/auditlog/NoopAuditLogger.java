package com.github.khakers.modmailviewer.auditlog;

import com.github.khakers.modmailviewer.auditlog.event.AuditEvent;
import io.javalin.http.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoopAuditLogger implements AuditLogger {

    private static final Logger logger = LogManager.getLogger();

    public NoopAuditLogger() {
        logger.debug("using NoopAuditLogger");
    }

    @Override
    public void pushEvent(AuditEvent event) {
        return;
    }

    @Override
    public void pushAuditEventWithContext(Context ctx, String event, String description) throws Exception {
        return;
    }
}
