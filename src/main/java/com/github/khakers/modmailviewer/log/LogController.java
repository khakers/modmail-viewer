package com.github.khakers.modmailviewer.log;

import io.javalin.http.Handler;

public class LogController {
    public static Handler serveLogsPage = ctx -> {
      LogsPage logsPage = new LogsPage(ctx);
      logsPage.render();
    };
    public static Handler serveLogPage = ctx -> {
        LogPage logPage = new LogPage(ctx);
        logPage.render();
    };
}
