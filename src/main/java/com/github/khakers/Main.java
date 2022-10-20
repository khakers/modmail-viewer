package com.github.khakers;

import com.github.khakers.data.ModMailLogEntry;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class Main {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {

        var db = new ModMailLogDB("mongodb://127.0.0.1:27017");

        var app = Javalin.create(javalinConfig -> {
                    //todo we need to make our own jsonmapper class that include jackson java 8 modules
                    javalinConfig.jsonMapper(new JavalinJackson());
                    javalinConfig.plugins.enableDevLogging();
                    javalinConfig.requestLogger.http((ctx, executionTimeMs) -> {
                        logger.info("{} {}:{} {} in {}", ctx.method(), ctx.ip(), ctx.port(), ctx.fullUrl(), executionTimeMs);
                    });
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