package com.github.khakers.modmailviewer.page.audit;

import com.github.khakers.modmailviewer.auditlog.AuditEventDAO;
import io.javalin.http.Handler;

public class AuditController {

    private AuditEventDAO auditEventDAO;

    public AuditController(AuditEventDAO auditEventDAO) {
        this.auditEventDAO = auditEventDAO;
    }

    public Handler serveAuditPage = ctx -> {
        var id = ctx.pathParam("id");
        var event = this.auditEventDAO.getAuditEvent(id);
        if (event.isEmpty()) {
            ctx.status(404);
            return;
        }
        var page = new AuditEventPage(ctx, event.get());
        page.render();
    };
}
