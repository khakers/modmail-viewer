package com.github.khakers;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Collections;

import static io.javalin.rendering.template.TemplateUtil.model;

public class Main {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {

        var db = new ModMailLogDB("mongodb://127.0.0.1:27017");
        var templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "resources", "templates")), ContentType.Html);
        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.jsonMapper(new JacksonJavalinJsonMapper());
                    javalinConfig.staticFiles.add("/static", Location.CLASSPATH);
                    javalinConfig.plugins.enableDevLogging();
                    javalinConfig.requestLogger.http((ctx, executionTimeMs) -> logger.info("{} {}:{} {} in {}", ctx.method(), ctx.ip(), ctx.port(), ctx.fullUrl(), executionTimeMs));
                })
                .get("/", ctx -> {
                    var pageParam = ctx.queryParam("page");
                    Integer page = ctx.queryParamAsClass("page", Integer.class)
                            .check(integer -> integer >= 1, "page must be at least 1")
                            .getOrDefault(1);
                    logger.debug(pageParam);
                    ctx.render("homepage.jte", model("logEntries", db.getPaginatedMostRecentEntries(page), "page", page, "pageCount", db.getPaginationCount()));
                })
                .get("/logs/{id}", ctx -> {
                    var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                    entry.ifPresentOrElse(
                            modMailLogEntry -> ctx.render("logspage.jte", Collections.singletonMap("modmailLog", modMailLogEntry)),
                            () -> {
                                ctx.status(404);
                                ctx.result();
                            });

                })
                .get("/api/logs/{id}", ctx -> {
                    var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                    entry.ifPresentOrElse(
                            ctx::json,
                            () -> {
                                ctx.status(404);
                                ctx.result();
                            });

                })
//                .error(404, ctx -> ctx.result("Generic 404 message"))
                .start(7070);
        JavalinJte.init(templateEngine);
//        var output = new StringOutput();
//        templateEngine.render("hello.jte", "World", output);
//        System.out.println(output);
        app.get("/potato/{id}", ctx -> ctx.result("Your id was " + ctx.pathParam("id")));
    }
}