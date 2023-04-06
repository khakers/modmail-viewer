package com.github.khakers.modmailviewer;

import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.log.LogController;
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
import io.javalin.config.JavalinConfig;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.rendering.template.JavalinJte;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;

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
    public static final Parser PARSER = Parser.builder(OPTIONS)
            .build();
    public static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS)
            .escapeHtml(true)
            .build();
    private static final Logger logger = LogManager.getLogger();
    private static final String envPrepend = "MODMAIL_VIEWER";

    public static final ModMailLogDB db = new ModMailLogDB(Config.MONGODB_URI);

    static final AuthHandler authHandler =
            Config.isAuthEnabled ?
                    new AuthHandler(Config.WEB_URL + "/callback",
                            Config.DISCORD_CLIENT_ID,
                            Config.DISCORD_CLIENT_SECRET,
                            Config.JWT_SECRET_KEY,
                            db)
                    : null;


    public static void main(String[] args) {

        var updateThread = new Thread(() -> {
            var updateChecker = new UpdateChecker();
            updateChecker.isUpdateAvailable();
        });


        TemplateEngine templateEngine;

        if (Config.isDevMode) {
            templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/main/jte")), ContentType.Html);
        } else {
            templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        }

        JavalinJte.init(templateEngine);
        var app = Javalin.create(Main::configure)
                .get("/hello", ctx -> ctx.status(200).result("hello"), RoleUtils.anyone())
                .post("/logout", ctx -> {
                    ctx.removeCookie("jwt");
                    ctx.result("logout successful");
                    if (!Config.isAuthEnabled) {
                        ctx.redirect("/");
                    }
                }, RoleUtils.atLeastSupporter())
                .get("/", LogController.serveLogsPage, RoleUtils.atLeastSupporter())
//                .routes(() -> {
//                    path("logs", () -> {
//                        get(LogController.serveLogsPage, RoleUtils.atLeastSupporter());
//                        path("{id}", () ->
//                                get(LogController.serveLogPage, RoleUtils.atLeastSupporter())
//                        );
//                    });
//                })
                .get("/logs", LogController.serveLogsPage, RoleUtils.atLeastSupporter())
                .get("/logs/{id}", LogController.serveLogPage, RoleUtils.atLeastSupporter())
                .start(Config.httpPort);

        if (Config.isAuthEnabled) {
            // Register api only if authentication is enabled
            app.get("/api/logs/{id}", ctx -> {
                        var entry = db.getModMailLogEntry(ctx.pathParam("id"));
                        entry.ifPresentOrElse(
                                ctx::json,
                                () -> {
                                    throw new NotFoundResponse();
                                });

                    }, RoleUtils.atLeastAdministrator())
                    .get("/api/config", ctx -> ctx.json(db.getConfig()), RoleUtils.atLeastAdministrator());

            app.get("/callback", authHandler::handleCallback, Role.ANYONE);
        }


        logger.info("You are running Modmail-Viewer {} built on {}", ModmailViewer.VERSION, ModmailViewer.BUILD_TIMESTAMP);

    }

    private static void configure(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.jsonMapper(new JavalinJackson());
        if (Config.isHttpsOnly) {
            logger.info("HTTPS only is ENABLED");
            config.plugins.enableSslRedirects();
        }
        if (Config.isDevMode) {
            logger.info("Loading static files from {}", System.getProperty("user.dir") + "/src/main/resources/static");
            config.staticFiles.add(System.getProperty("user.dir") + "/src/main/resources/static", Location.EXTERNAL);
        } else {
            config.staticFiles.add("/static", Location.CLASSPATH);
        }
        config.staticFiles.enableWebjars();
        if (Config.isDevMode) {
            logger.info("Dev mode is ENABLED");
            config.showJavalinBanner = true;
            config.plugins.enableDevLogging();
        }
        if (Config.isAuthEnabled) {
            config.accessManager(authHandler::HandleAuth);
        } else {
            logger.warn("Authentication is DISABLED");
            config.accessManager((handler, context, set) -> handler.handle(context));
        }
        if (Config.isSecure) {
            logger.info("SSL is ENABLED");
            SSLPlugin sslPlugin = new SSLPlugin(sslConfig -> {
                sslConfig.pemFromPath(Config.SSL_CERT, Config.SSL_KEY);
                sslConfig.insecurePort = Config.httpPort;
                sslConfig.securePort = Config.httpsPort;
                sslConfig.sniHostCheck = Config.isSNIEnabled;
                if (!Config.isHttpsOnly) {
                    logger.warn("SSL is ENABLED but HTTPS only is DISABLED");
                }
            });
            config.plugins.register(sslPlugin);
        }
    }
}