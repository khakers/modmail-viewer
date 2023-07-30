package com.github.khakers.modmailviewer.jte;

import com.github.khakers.modmailviewer.util.Localizer;
import gg.jte.Content;
import io.javalin.http.Context;

import java.util.Locale;

public final class JteContext {
    private static final ThreadLocal<JteContext> context = ThreadLocal.withInitial(JteContext::new);
    private JteLocalizer localizer;

    private final Locale locale = Locale.ENGLISH;

    public static void init(Context ctx) {
        JteContext context = getContext();
        //todo language
        context.localizer = new JteLocalizer(Localizer.getInstance("en"));
    }

    public static Locale getLocale() {
        return getContext().locale;
    }

    public static Content localize(String key) {
        return getContext().localizer.localize(key);
    }

    public static Content localize(String key, Object... params) {
        return getContext().localizer.localize(key, params);
    }

    private static JteContext getContext() {
        return context.get();
    }
}
