package com.github.khakers.modmailviewer.page.dashboard;

import io.javalin.http.Handler;

public class DashboardController {

    public static Handler serveDashboardPage = ctx -> {
        var period = ctx.queryParamAsClass("period", Integer.class)
              .check(integer -> integer >= 1, "period must be at least 1")
              .getOrDefault(30);

        var page = new DashboardPage(ctx, period);
        page.render();
    };
}
