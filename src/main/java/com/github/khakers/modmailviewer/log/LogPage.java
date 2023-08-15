package com.github.khakers.modmailviewer.log;

import com.github.khakers.modmailviewer.Main;
import com.github.khakers.modmailviewer.Page;
import com.github.khakers.modmailviewer.data.ModMailLogEntry;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;


public class LogPage extends Page {
    public final String PATH = "/logs/{id}";

    public final ModMailLogEntry log;


    public LogPage(Context ctx) {
        super(ctx);
        var entry = Main.MOD_MAIL_LOG_CLIENT.getModMailLogEntry(ctx.pathParam("id"));
        if (entry.isEmpty()) {
            throw new NotFoundResponse("No modmail log entry found");
        }
        log = entry.get();
    }

    @Override
    public String getTitle() {
        return "Modmail Log " + log.getTitle().orElse(log.getKey());
    }

    @Override
    public boolean isNSFW() {
        return log.isNsfw();
    }

    @Override
    public String getTemplate() {
        return "pages/LogEntryView.jte";
    }
}
