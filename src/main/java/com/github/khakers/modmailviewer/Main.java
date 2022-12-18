package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.auth.SiteUser;
import com.github.khakers.modmailviewer.data.internal.TicketStatus;
import com.github.khakers.modmailviewer.markdown.channelmention.ChannelMentionExtension;
import com.github.khakers.modmailviewer.markdown.customemoji.CustomEmojiExtension;
import com.github.khakers.modmailviewer.markdown.spoiler.SpoilerExtension;
import com.github.khakers.modmailviewer.markdown.timestamp.TimestampExtension;
import com.github.khakers.modmailviewer.markdown.underline.UnderlineExtension;
import com.github.khakers.modmailviewer.markdown.usermention.UserMentionExtension;
import com.github.khakers.modmailviewer.util.RoleUtils;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.community.ssl.SSLPlugin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;

import static io.javalin.rendering.template.TemplateUtil.model;

public class Main {

    static final DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Arrays.asList(
                    StrikethroughExtension.create(),
                    AutolinkExtension.create(),
                    SpoilerExtension.create(),
                    UnderlineExtension.create(),
                    CustomEmojiExtension.create(),
                    TimestampExtension.create(),
                    UserMentionExtension.create(),
                    ChannelMentionExtension.create()
            ))
            .set(Parser.HEADING_PARSER, false)
            //Required to enable underlines to function
            //Otherwise the '_' delimiter conflicts
            .set(Parser.UNDERSCORE_DELIMITER_PROCESSOR, false)
            .set(Parser.HTML_BLOCK_PARSER, false)
            .set(Parser.INDENTED_CODE_BLOCK_PARSER, false)
            .set(HtmlRenderer.SOFT_BREAK, "<br />\n")
            .toImmutable();
    static final Parser PARSER = Parser.builder(OPTIONS)
            .build();
    static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS)
            .escapeHtml(true)
            .build();
    private static final Logger logger = LogManager.getLogger();
    private static final String envPrepend = "MODMAIL_VIEWER";

    public static void main(String[] args) {

        var db = new ModMailLogDB(Config.MONGODB_URI);

        TemplateEngine templateEngine;

        if (Config.isDevMode) {
            templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src", "main", "jte")), ContentType.Html);

        } else {
            templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        }

        AuthHandler authHandler;
        if (Config.isAuthEnabled) {
            authHandler = new AuthHandler(Config.WEB_URL + "/callback",
                    Config.DISCORD_CLIENT_ID,
                    Config.DISCORD_CLIENT_SECRET,
                    Config.JWT_SECRET_KEY,
                    db);
        } else {
            authHandler = null;
        }

        JavalinJte.init(templateEngine);
        var app = Javalin.create(javalinConfig -> {
                    javalinConfig.jsonMapper(new JacksonJavalinJsonMapper());
                    javalinConfig.staticFiles.add("/static", Location.CLASSPATH);
                    javalinConfig.staticFiles.enableWebjars();
                    if (Config.isDevMode) {
                        logger.info("Dev mode is ENABLED");
                        javalinConfig.plugins.enableDevLogging();
                    }
                    if (Config.isAuthEnabled) {
                        javalinConfig.accessManager(authHandler::HandleAuth);
                    } else {
                        logger.warn("Authentication is DISABLED");
                        javalinConfig.accessManager((handler, context, set) -> handler.handle(context));
                    }

                    if (Config.isSecure) {
                        logger.info("SSL is ENABLED");
                        SSLPlugin sslPlugin = new SSLPlugin(sslConfig -> {
                            sslConfig.pemFromPath(Config.SSL_CERT, Config.SSL_KEY);
                            sslConfig.insecurePort = Config.httpPort;
                            sslConfig.securePort = Config.httpsPort;
                            sslConfig.sniHostCheck = Config.isSNIEnabled;
                            if (Config.isHttpsOnly) {
                                logger.warn("SSL is ENABLED but HTTPS only is DISABLED");
                            }
                        });
                        javalinConfig.plugins.register(sslPlugin);
                        if (Config.isHttpsOnly) {
                            logger.info("HTTPS only is ENABLED");
                            javalinConfig.plugins.enableSslRedirects();
                        }
                    }
                })
                .get("/logout", ctx -> {
                    ctx.removeCookie("jwt");
                    ctx.result("logout successful");
                    if (!Config.isAuthEnabled) {
                        ctx.redirect("/");
                    }
                }, RoleUtils.atLeastRegular())
                .get("/", ctx -> {
                    Integer page = ctx.queryParamAsClass("page", Integer.class)
                            .check(integer -> integer >= 1, "page must be at least 1")
                            .getOrDefault(1);
                    String statusFilter = ctx.queryParamAsClass("status", String.class)
                            .check(s -> s.equalsIgnoreCase(String.valueOf(TicketStatus.OPEN))
                                    || s.equalsIgnoreCase(String.valueOf(TicketStatus.CLOSED))
                                    || s.equalsIgnoreCase(String.valueOf(TicketStatus.ALL)), "")
                            .getOrDefault("ALL");
                    Boolean showNSFW = ctx.queryParamAsClass("nsfw", Boolean.class)
                            .getOrDefault(Boolean.TRUE);
                    var ticketFilter = TicketStatus.valueOf(statusFilter.toUpperCase());
                    var pageCount = db.getPaginationCount(ticketFilter);
                    page = Math.min(pageCount, page);
                    ctx.render("pages/homepage.jte",
                            model("logEntries", db.getPaginatedMostRecentEntriesByMessageActivity(page, ticketFilter),
                                    "page", page,
                                    "pageCount", pageCount,
                                    "user", authHandler != null ? AuthHandler.getUser(ctx) : new SiteUser(),
                                    "modMailLogDB", db,
                                    "ticketStatusFilter", ticketFilter,
                                    "showNSFW", showNSFW));
                }, RoleUtils.atLeastModerator())
                .get("/dashboard", ctx -> ctx.render("pages/dashboard.jte",
                        model("user", AuthHandler.getUser(ctx),
                                "totalTickets", db.getTotalTickets(TicketStatus.ALL),
                                "openTickets", db.getTotalTickets(TicketStatus.OPEN),
                                "closedTickets", db.getTotalTickets(TicketStatus.CLOSED),
                                "moderatorCloses", db.getTicketsClosedByUser(),
                                "recentEntries", db.getLogsWithRecentActivity(5))
                ), RoleUtils.atLeastModerator())
                .get("/logs/{id}", ctx -> {
                    var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                    entry.ifPresentOrElse(
                            modMailLogEntry -> {
                                try {
                                    ctx.render("pages/logspage.jte", model(
                                            "modmailLog", modMailLogEntry,
                                            "user", authHandler != null ? AuthHandler.getUser(ctx) : new SiteUser(),
                                            "parser", PARSER,
                                            "renderer", RENDERER));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            () -> {
                                ctx.status(404);
                                ctx.result();
                            });

                }, RoleUtils.atLeastModerator())
                .get("/api/permissions", ctx -> ctx.json(db.getConfig().getFlatUserPerms()), RoleUtils.atLeastAdministrator())
                .get("/api/ticketclosers", ctx -> ctx.json(db.getTicketsClosedByUserOrdered()), RoleUtils.atLeastAdministrator())
                .get("/api/stats/dailytickets", ctx -> {
                    String statusFilter = ctx.queryParamAsClass("status", String.class)
                            .check(s -> s.equalsIgnoreCase(String.valueOf(TicketStatus.OPEN))
                                    || s.equalsIgnoreCase(String.valueOf(TicketStatus.CLOSED))
                                    || s.equalsIgnoreCase(String.valueOf(TicketStatus.ALL)), "")
                            .getOrDefault("ALL");
                    var period = ctx.queryParamAsClass("period", Integer.class)
                            .check(integer -> integer >= 1 && integer <= 90, "invalid period value. Must be between 1 and 90")
                            .getOrDefault(30);
                    ctx.json(db.getTicketsPerDay(period, TicketStatus.CLOSED));
                }, RoleUtils.atLeastSupporter())
                .start(Config.httpPort);

        if (Config.isAuthEnabled) {
            // Register api only if authentication is enabled
            app.get("/api/logs/{id}", ctx -> {
                        var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                        entry.ifPresentOrElse(
                                ctx::json,
                                () -> {
                                    ctx.status(404);
                                    ctx.result();
                                });

                    }, RoleUtils.atLeastAdministrator())
                    .get("/api/config", ctx -> ctx.json(db.getConfig()), RoleUtils.atLeastModerator());

            app.get("/callback", authHandler::handleCallback, Role.ANYONE);
        }

        logger.info("You are running Modmail-Viewer {} built on {}", ModmailViewer.VERSION, ModmailViewer.BUILD_TIMESTAMP);

    }
}