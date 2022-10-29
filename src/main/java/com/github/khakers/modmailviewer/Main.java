package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.util.RoleUtils;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Assert;

import java.math.BigInteger;
import java.nio.file.Path;
import java.security.SecureRandom;

import static io.javalin.rendering.template.TemplateUtil.model;

public class Main {

    private static final Logger logger = LogManager.getLogger();

    private static final String envPrepend = "MODMAIL_VIEWER";

    public static void main(String[] args) {
        Assert.requireNonEmpty(System.getenv(envPrepend+"_URL"), "No URL provided. provide one with the option \"MODMAIL_VIEWER_URL\"");
        Assert.requireNonEmpty(System.getenv(envPrepend+"_MONGODB_URI"), "No mongodb URI provided. provide one with the option \"MODMAIL_VIEWER_MONGODB_URI\"");
        Assert.requireNonEmpty(System.getenv(envPrepend+"_DISCORD_OAUTH_CLIENT_ID"), "No Discord client ID provided. provide one with the option \"MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_ID\"");
        Assert.requireNonEmpty(System.getenv(envPrepend+"_DISCORD_OAUTH_CLIENT_SECRET"), "No Discord client secret provided. provide one with the option \"MODMAIL_VIEWER_DISCORD_OAUTH_CLIENT_SECRET\"");

        String jwtSecretKey = System.getenv("MODMAIL_VIEWER_SECRETKEY");

        if (jwtSecretKey == null || jwtSecretKey.isEmpty()) {
            logger.warn("Generated a random key for signing tokens. Sessions will not persist between restarts");
            jwtSecretKey = new BigInteger(256, new SecureRandom()).toString(32);
        } else if (jwtSecretKey.length() < 32) {
            logger.warn("Your secret key is too short! it should be at least 32 characters (256 bits). Short keys can be trivially brute forced allowing an attacker to create their own auth tokens");
        }

        var db = new ModMailLogDB(System.getenv(envPrepend+"_MONGODB_URI"));
        var templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "resources", "templates")), ContentType.Html);


        var authHandler = new AuthHandler(System.getenv(envPrepend+"_URL") + "/callback",
                System.getenv(envPrepend+"_DISCORD_OAUTH_CLIENT_ID"),
                System.getenv(envPrepend+"_DISCORD_OAUTH_CLIENT_SECRET"),
                jwtSecretKey,
                db);



        JavalinJte.init(templateEngine);
        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.jsonMapper(new JacksonJavalinJsonMapper());
                    javalinConfig.staticFiles.add("/static", Location.CLASSPATH);
                    javalinConfig.plugins.enableDevLogging();
                    javalinConfig.accessManager(authHandler::HandleAuth);
                })
                .get("/callback", authHandler::handleCallback, Role.ANYONE)
                .get("/logout", ctx -> {
                    ctx.removeCookie("jwt");
                    ctx.result("logout successful");
                }, RoleUtils.atLeastRegular())
                .get("/", ctx -> {
                    var pageParam = ctx.queryParam("page");
                    Integer page = ctx.queryParamAsClass("page", Integer.class)
                            .check(integer -> integer >= 1, "page must be at least 1")
                            .getOrDefault(1);
                    ctx.render("homepage.jte",
                            model("logEntries", db.getPaginatedMostRecentEntries(page),
                                    "page", page,
                                    "pageCount", db.getPaginationCount(),
                                    "user", AuthHandler.getUser(ctx)));
                }, RoleUtils.atLeastModerator())
                .get("/logs/{id}", ctx -> {
                    var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                    entry.ifPresentOrElse(
                            modMailLogEntry -> {
                                try {
                                    ctx.render("logspage.jte", model(
                                            "modmailLog", modMailLogEntry,
                                            "user", AuthHandler.getUser(ctx)));
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
                .start(7070);

        app.get("/potato/{id}", ctx -> ctx.result("Your id was " + ctx.pathParam("id")));
    }

//    private static void accessManager(Handler handler, Context ctx, Set<? extends RouteRole> routeRoles) throws Exception {
//        new SecurityHandler()
//        var userRole = getUserRole(ctx);
//        if (routeRoles.contains(userRole)) {
//            handler.handle(ctx);
//        } else {
//            ctx.status(401).result("unauthorized");
//        }
//    }

}