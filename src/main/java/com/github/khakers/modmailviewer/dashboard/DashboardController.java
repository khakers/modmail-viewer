package com.github.khakers.modmailviewer.dashboard;

import io.javalin.http.Handler;

public class DashboardController {

    public static Handler serveDashboardPage = ctx -> {
        var page = new DashboardPage(ctx);
        page.render();
    };
}
