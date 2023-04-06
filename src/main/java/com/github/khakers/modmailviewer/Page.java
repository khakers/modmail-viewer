package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.auth.UserToken;
import com.github.khakers.modmailviewer.jte.JteContext;
import io.javalin.http.Context;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;

public abstract class Page {
    protected final Context ctx;

    private final long created = System.nanoTime();
    @Nullable
    private UserToken currentUser;

    public Page(Context ctx) {
        this.ctx = ctx;
        if (Main.authHandler == null) {
            currentUser = null;
        } else {
            try {
                currentUser = AuthHandler.getUser(ctx);
            } catch (JsonProcessingException e) {
                currentUser = null;
            }
        }
    }

    public abstract String getTemplate();

    public Optional<UserToken> getCurrentUser() {
        return Optional.ofNullable(this.currentUser);
    }

    public String getLang() {
//        return RequestUtil.getLocale(ctx);
        return "en";
    }

    public Context getCtx() {
        return ctx;
    }

    public String getRenderTime() {
        long duration = System.nanoTime() - created;
        double millis = duration / 1000000.0;

        return Math.round(millis * 1000.0) / 1000.0 + "ms";
    }

    public void render() {
        JteContext.init(ctx);
        ctx.render(getTemplate(), Collections.singletonMap("page", this));
    }

}
