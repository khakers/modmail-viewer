package com.github.khakers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.khakers.auth.AuthHandler;
import com.github.khakers.auth.Role;
import com.github.khakers.util.RoleUtils;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

import static com.github.khakers.auth.AuthHandler.getUser;
import static io.javalin.rendering.template.TemplateUtil.model;

public class Main {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {

        var db = new ModMailLogDB(System.getenv("modmail.viewer.mongodb.url"));
        var templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "resources", "templates")), ContentType.Html);

        var authHandler = new AuthHandler("http://127.0.0.1:7070/callback",
                System.getenv("modmail.viewer.discord.oauth.client.id"),
                System.getenv("modmail.viewer.discord.oauth.client.secret"),
                System.getenv("modmail.viewer.secretkey"),
                db);

        JavalinJte.init(templateEngine);
        //todo logout endpoint
        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.jsonMapper(new JacksonJavalinJsonMapper());
                    javalinConfig.staticFiles.add("/static", Location.CLASSPATH);
                    javalinConfig.plugins.enableDevLogging();
                    javalinConfig.accessManager(authHandler::HandleAuth);
                })
                .get("/callback", authHandler::handleCallback, Role.ANYONE)
                .get("/", ctx -> {
                    var pageParam = ctx.queryParam("page");
                    Integer page = ctx.queryParamAsClass("page", Integer.class)
                            .check(integer -> integer >= 1, "page must be at least 1")
                            .getOrDefault(1);
                    ctx.render("homepage.jte",
                            model("logEntries", db.getPaginatedMostRecentEntries(page),
                                    "page", page,
                                    "pageCount", db.getPaginationCount(),
                                    "user", getUser(ctx)));
                }, RoleUtils.atLeastModerator())
                .get("/logs/{id}", ctx -> {
                    var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                    entry.ifPresentOrElse(
                            modMailLogEntry -> {
                                try {
                                    ctx.render("logspage.jte", model(
                                            "modmailLog", modMailLogEntry,
                                            "user", getUser(ctx)));
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