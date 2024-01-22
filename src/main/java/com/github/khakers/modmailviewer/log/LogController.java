package com.github.khakers.modmailviewer.log;

import com.github.khakers.modmailviewer.auditlog.OutboundAuditEventLogger;
import com.github.khakers.modmailviewer.configuration.Config;
import io.javalin.http.Handler;

public class LogController {

    OutboundAuditEventLogger auditLogger;
    public LogController(OutboundAuditEventLogger auditLogClient) {
        this.auditLogger = auditLogClient;
    }

    public Handler serveLogsPage = ctx -> {
      LogsPage logsPage = new LogsPage(ctx);
      logsPage.render();
    };
    public Handler serveLogPage = ctx -> {
        if (Config.appConfig.auditLogConfig().logTicketAccess()) {
            auditLogger.pushAuditEventWithContext(ctx, "viewer.log.accessed", String.format("accessed log id %s", ctx.pathParam("id")));
        }
        LogPage logPage = new LogPage(ctx);
        logPage.render();
    };
}
