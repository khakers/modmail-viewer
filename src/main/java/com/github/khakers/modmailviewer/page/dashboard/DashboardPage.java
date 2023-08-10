package com.github.khakers.modmailviewer.page.dashboard;

import com.github.khakers.modmailviewer.Page;
import io.javalin.http.Context;

public class DashboardPage extends Page {

    public final String PATH = "/dashboard}";

    public final int period;

    public DashboardPage(Context ctx, int period) {
        super(ctx);

        this.period = period;
    }

    @Override
    public String getTemplate() {
        return "pages/DashboardView.jte";
    }
}
