package com.github.khakers.modmailviewer.dashboard;

import com.github.khakers.modmailviewer.Page;
import io.javalin.http.Context;

public class DashboardPage extends Page {
    public DashboardPage(Context ctx) {
        super(ctx);
    }

    @Override
    public String getTemplate() {
        return "pages/DashboardView.jte";
    }
}
