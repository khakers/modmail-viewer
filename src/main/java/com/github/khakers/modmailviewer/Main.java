package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.auth.SiteUser;
import com.github.khakers.modmailviewer.util.RoleUtils;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.community.ssl.SSLPlugin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Assert;

import java.math.BigInteger;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Objects;

import static io.javalin.rendering.template.TemplateUtil.model;

public class Main {

    private static final Logger logger = LogManager.getLogger();

    private static final String envPrepend = "MODMAIL_VIEWER";

    public static boolean isSecure = false;

    public static void main(String[] args) {

        boolean enableAuth = !(Objects.nonNull(System.getenv(envPrepend + "_AUTH_ENABLED")) && System.getenv(envPrepend + "_AUTH_ENABLED").equalsIgnoreCase("false"));

        Assert.requireNonEmpty(System.getenv(envPrepend + "_URL"), "No URL provided. provide one with the option \"MODMAIL_VIEWER_URL\"");
        Assert.requireNonEmpty(System.getenv(envPrepend + "_MONGODB_URI"), "No mongodb URI provided. provide one with the option \"MODMAIL_VIEWER_MONGODB_URI\"");
        if (enableAuth) {
            Assert.requireNonEmpty(System.getenv(envPrepend + "_DISCORD_OAUTH_CLIENT_ID"), "No Discord client ID provided. Provide one with the option \"MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID\"");
            Assert.requireNonEmpty(System.getenv(envPrepend + "_DISCORD_OAUTH_CLIENT_SECRET"), "No Discord client secret provided. Provide one with the option \"MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET\"");
        }

        String jwtSecretKey = System.getenv("MODMAIL_VIEWER_SECRETKEY");

        boolean dev = Objects.nonNull(System.getenv(envPrepend + "_DEV")) && !System.getenv(envPrepend + "_DEV").equalsIgnoreCase("false");

        if (Objects.nonNull(System.getenv(envPrepend + "_SSL")) && System.getenv(envPrepend + "_SSL").equalsIgnoreCase("true")) {
            logger.info("SSL is ENABLED");
            isSecure = true;
        }
        boolean httpsOnly = isSecure;
        if ((Objects.nonNull(System.getenv(envPrepend + "_HTTPS_ONLY")) && System.getenv(envPrepend + "_HTTPS_ONLY").equalsIgnoreCase("true"))) {
            isSecure = true;
        }
        if (httpsOnly) {
            logger.info("HTTPS only is ENABLED");
        }
        if (isSecure) {
            Assert.requireNonEmpty(System.getenv(envPrepend + "_SSL_CERT"), "SSL was enabled but no certificate file path was provided. Provide one with the option \"MODMAIL_VIEWER_SSL_CERT\"");
            Assert.requireNonEmpty(System.getenv(envPrepend + "_SSL_KEY"), "SSL was enabled but no key file path was provided. Provide one with the option \"MODMAIL_VIEWER_DISCORD_SSL_KEY\"");
        }

        if (jwtSecretKey == null || jwtSecretKey.isEmpty()) {
            logger.warn("Generated a random key for signing tokens. Sessions will not persist between restarts");
            jwtSecretKey = new BigInteger(256, new SecureRandom()).toString(32);
        } else if (jwtSecretKey.length() < 32) {
            logger.warn("Your secret key is too short! it should be at least 32 characters (256 bits). Short keys can be trivially brute forced allowing an attacker to create their own auth tokens");
        }

        var db = new ModMailLogDB(System.getenv(envPrepend + "_MONGODB_URI"));

        TemplateEngine templateEngine;

        if (dev) {
            templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Html);

        } else {
            templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        }

        AuthHandler authHandler;
        if (enableAuth) {
            authHandler = new AuthHandler(System.getenv(envPrepend + "_URL") + "/callback",
                    System.getenv(envPrepend + "_DISCORD_OAUTH_CLIENT_ID"),
                    System.getenv(envPrepend + "_DISCORD_OAUTH_CLIENT_SECRET"),
                    jwtSecretKey,
                    db);
        } else {
            authHandler = null;
        }

        JavalinJte.init(templateEngine);
        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.jsonMapper(new JacksonJavalinJsonMapper());
                    javalinConfig.staticFiles.add("/static", Location.CLASSPATH);
                    if (dev) {
                        logger.info("Dev mode is ENABLED");
                        javalinConfig.plugins.enableDevLogging();
                    }
                    if (enableAuth) {
                        javalinConfig.accessManager(authHandler::HandleAuth);
                    } else {
                        logger.warn("Authentication is DISABLED");
                        javalinConfig.accessManager((handler, context, set) -> handler.handle(context));
                    }

                    if (isSecure) {
                        SSLPlugin sslPlugin = new SSLPlugin(sslConfig -> {
                            sslConfig.pemFromPath(System.getenv(envPrepend + "_SSL_CERT"), System.getenv(envPrepend + "_SSL_KEY"));
                            if (dev) {
                                sslConfig.sniHostCheck = false;
                            }
                        });
                        javalinConfig.plugins.register(sslPlugin);
                    }
                    if (httpsOnly) {
                        logger.info("HTTPS only enabled");
                        javalinConfig.plugins.enableSslRedirects();
                    }
                })
                .get("/logout", ctx -> {
                    ctx.removeCookie("jwt");
                    ctx.result("logout successful");
                    if (!enableAuth) {
                        ctx.redirect("/");
                    }
                }, RoleUtils.atLeastRegular())
                .get("/", ctx -> {
                    Integer page = ctx.queryParamAsClass("page", Integer.class)
                            .check(integer -> integer >= 1, "page must be at least 1")
                            .getOrDefault(1);
                    ctx.render("pages/homepage.jte",
                            model("logEntries", db.getPaginatedMostRecentEntries(page),
                                    "page", page,
                                    "pageCount", db.getPaginationCount(),
                                    "user", authHandler != null ? AuthHandler.getUser(ctx) : new SiteUser(0L, "anonymous", "0000", "")));
                }, RoleUtils.atLeastModerator())
                .get("/logs/{id}", ctx -> {
                    var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                    entry.ifPresentOrElse(
                            modMailLogEntry -> {
                                try {
                                    ctx.render("pages/logspage.jte", model(
                                            "modmailLog", modMailLogEntry,
                                            "user", authHandler != null ? AuthHandler.getUser(ctx) : new SiteUser(0L, "anonymous", "0000", "")));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            () -> {
                                ctx.status(404);
                                ctx.result();
                            });

                }, RoleUtils.atLeastModerator())
                .get("/api/logs/{id}", ctx -> {
                    var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                    entry.ifPresentOrElse(
                            ctx::json,
                            () -> {
                                ctx.status(404);
                                ctx.result();
                            });

                }, RoleUtils.atLeastAdministrator())
                .get("/api/config", ctx -> ctx.json(db.getConfig()), RoleUtils.atLeastModerator())
                .start(80);

        if (enableAuth) {
            app.get("/callback", authHandler::handleCallback, Role.ANYONE);
        }

    }
}
