package com.github.khakers.modmailviewer.page.audit;

import com.github.khakers.modmailviewer.Main;
import io.javalin.http.Handler;

public class AuditController {

    public static Handler serveAuditPage = ctx -> {
        var id = ctx.pathParam("id");
        var event = Main.AuditLogClient.getAuditEvent(id);
        if (event.isEmpty()) {
            ctx.status(404);
            return;
        }
        var page = new AuditEventPage(ctx, event.get());
        page.render();
    };
}
