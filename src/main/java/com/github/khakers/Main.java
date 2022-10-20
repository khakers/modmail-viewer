package com.github.khakers;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {

        var db = new ModMailLogDB("mongodb://127.0.0.1:27017");

        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.jsonMapper(new JacksonJavalinJsonMapper());
                    javalinConfig.plugins.enableDevLogging();
                    javalinConfig.requestLogger.http((ctx, executionTimeMs) -> logger.info("{} {}:{} {} in {}", ctx.method(), ctx.ip(), ctx.port(), ctx.fullUrl(), executionTimeMs));
                })
                .get("/", ctx -> ctx.result("Hello World"))
                .get("/api/logs/{id}", ctx -> {
                    try {
                        var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                        entry.ifPresentOrElse(
                                ctx::json,
                                () -> {
                                    ctx.status(404);
                                    ctx.result();
                                });

                    } catch (Exception e) {
                        logger.error(e);
                    }
                })
//                .error(404, ctx -> ctx.result("Generic 404 message"))
                .start(7070);

        app.get("/potato/{id}", ctx -> ctx.result("Your id was " + ctx.pathParam("id")));
    }
}