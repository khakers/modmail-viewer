package com.github.khakers.modmailviewer.dashboard;

import com.github.khakers.modmailviewer.Page;
import io.javalin.http.Context;

public class DashboardPage extends Page {

    public final String PATH = "/dashboard}";

    public final int period;

    public DashboardPage(Context ctx) {
        super(ctx);

        period = ctx.queryParamAsClass("period", Integer.class)
                .check(integer -> integer >= 1, "period must be at least 1")
                .getOrDefault(30);

    }

    @Override
    public String getTemplate() {
        return "pages/DashboardView.jte";
    }
}
