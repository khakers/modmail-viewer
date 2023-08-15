package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.khakers.modmailviewer.page.admin.AdminController;
import com.github.khakers.modmailviewer.auditlog.OutboundAuditEventLogger;
import com.github.khakers.modmailviewer.auditlog.MongoAuditEventLogger;
import com.github.khakers.modmailviewer.auditlog.NoopAuditEventLogger;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.page.dashboard.DashboardController;
import com.github.khakers.modmailviewer.page.dashboard.MetricsAccessor;
import com.github.khakers.modmailviewer.log.LogController;
import com.github.khakers.modmailviewer.markdown.channelmention.ChannelMentionExtension;
import com.github.khakers.modmailviewer.markdown.customemoji.CustomEmojiExtension;
import com.github.khakers.modmailviewer.markdown.spoiler.SpoilerExtension;
import com.github.khakers.modmailviewer.markdown.timestamp.TimestampExtension;
import com.github.khakers.modmailviewer.markdown.underline.UnderlineExtension;
import com.github.khakers.modmailviewer.markdown.usermention.UserMentionExtension;
import com.github.khakers.modmailviewer.page.audit.AuditController;
import com.github.khakers.modmailviewer.util.RoleUtils;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
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
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.plugin.bundled.GlobalHeaderConfig;
import io.javalin.rendering.template.JavalinJte;
import io.javalin.validation.JavalinValidation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;

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

    private static final ConnectionString connectionString = new ConnectionString(Config.MONGODB_URI);

    private static final MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
          .applyConnectionString(connectionString)
          .codecRegistry(
                CodecRegistries.fromRegistries(
                      MongoClientSettings
                            .getDefaultCodecRegistry(),
                      CodecRegistries
                            .fromProviders(PojoCodecProvider.builder()
                                  .automatic(true)
                                  .build())))
          .build());

    private static final MongoDatabase MODMAIL_DATABASE = mongoClient.getDatabase(connectionString.getDatabase() == null ? "modmail_bot" : connectionString.getDatabase());
    public static final ModMailLogDB MOD_MAIL_LOG_CLIENT = new ModMailLogDB(MODMAIL_DATABASE);

    // We will always need an audit logger for searching, even if pushing to an audit logger is disabled
    public static MongoAuditEventLogger AuditLogClient = new MongoAuditEventLogger(mongoClient, Config.MONGODB_URI, "modmail_bot", "audit_log");

    public static final OutboundAuditEventLogger auditLogger = Config.isAuthEnabled
            ? AuditLogClient
            : new NoopAuditEventLogger();

    static final AuthHandler authHandler =
            Config.isAuthEnabled ?
                    new AuthHandler(Config.WEB_URL + "/callback",
                            Config.DISCORD_CLIENT_ID,
                            Config.DISCORD_CLIENT_SECRET,
                            Config.JWT_SECRET_KEY,
                          MOD_MAIL_LOG_CLIENT)
                    : null;

    public static final UpdateChecker updateChecker = new UpdateChecker();

    public static MetricsAccessor metricsAccessor = new MetricsAccessor(MODMAIL_DATABASE);

    public static void main(String[] args) {

        TemplateEngine templateEngine;

        if (Config.isDevMode) {
            templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/main/jte")), ContentType.Html);
        } else {
            templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        }

        registerValidators();

        JavalinJte.init(templateEngine);
        var app = Javalin.create(Main::configure)
                .get("/hello", ctx -> ctx.status(200).result("hello"), RoleUtils.anyone())
                .post("/logout", ctx -> {
                    auditLogger.pushAuditEventWithContext(ctx, "viewer.logout", "User logged out");
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
                .get("/dashboard", DashboardController.serveDashboardPage, RoleUtils.atLeastSupporter())
                //todo maybe after?
                .after("/logs/{id}", ctx -> {
                    if (ctx.statusCode() == HttpStatus.FORBIDDEN.getCode()) {

                    }

                    if (Config.isDetailedAuditingEnabled) {
                        auditLogger.pushAuditEventWithContext(ctx, "log.accessed", String.format("accessed log id %s", ctx.pathParam("id")));
                    }
                })
                .get("/admin", AdminController.serveAdminPage, RoleUtils.atLeastAdministrator())
                .get("/audit/{id}", AuditController.serveAuditPage, RoleUtils.atLeastAdministrator())
                .after("/api/*", ctx -> {
                    if (Config.isApiAuditingEnabled) {
                        if (ctx.statusCode() == HttpStatus.FORBIDDEN.getCode()) {
                            auditLogger.pushAuditEventWithContext(ctx, "viewer.api", String.format("DENIED %s %s", ctx.method(), ctx.path()));
                        } else {
                            auditLogger.pushAuditEventWithContext(ctx, "viewer.api", String.format("%s %s", ctx.method(), ctx.path()));
                        }
                    }
                })
                .start(Config.httpPort);

        if (Config.isAuthEnabled) {
            // Register api only if authentication is enabled
            app.get("/api/logs/{id}", ctx -> {
                        var entry = MOD_MAIL_LOG_CLIENT.getModMailLogEntry(ctx.pathParam("id"));
                        entry.ifPresentOrElse(
                                ctx::json,
                                () -> {
                                    throw new NotFoundResponse();
                                });

                    }, RoleUtils.atLeastAdministrator())
                    .get("/api/config", ctx -> ctx.json(MOD_MAIL_LOG_CLIENT.getConfig()), RoleUtils.atLeastAdministrator());

            app.get("/callback", authHandler::handleCallback, Role.ANYONE);
        }


        logger.info("You are running Modmail-Viewer {} built on {}", ModmailViewer.VERSION, ModmailViewer.BUILD_TIMESTAMP);

    }

    private static void configure(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.jsonMapper(new JavalinJackson().updateMapper(objectMapper -> objectMapper.registerModule(new Jdk8Module())));
        config.plugins.enableGlobalHeaders(Main::configureHeaders);
        if (Config.isHttpsOnly) {
            logger.info("HTTPS only is ENABLED");
            config.plugins.enableSslRedirects();
        }
        if (Config.isDevMode) {
            logger.info("Loading static files from {}", System.getProperty("user.dir") + "/src/main/resources/static");
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.mimeTypes.add(io.javalin.http.ContentType.TEXT_JS, "js");
                staticFileConfig.mimeTypes.add(io.javalin.http.ContentType.TEXT_CSS, "css");
                staticFileConfig.location = Location.EXTERNAL;
                staticFileConfig.directory = System.getProperty("user.dir") + "/src/main/resources/static";
            });
        } else {
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.mimeTypes.add(io.javalin.http.ContentType.TEXT_JS, "js");
                staticFileConfig.mimeTypes.add(io.javalin.http.ContentType.TEXT_CSS, "css");
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.directory = "/static";
            });
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

    private static GlobalHeaderConfig configureHeaders() {
        var globalHeaderConfig =  new GlobalHeaderConfig();
        globalHeaderConfig.xFrameOptions(GlobalHeaderConfig.XFrameOptions.DENY);
        globalHeaderConfig.xContentTypeOptionsNoSniff();
        globalHeaderConfig.xPermittedCrossDomainPolicies(GlobalHeaderConfig.CrossDomainPolicy.NONE);
        globalHeaderConfig.crossOriginOpenerPolicy(GlobalHeaderConfig.CrossOriginOpenerPolicy.SAME_ORIGIN);
        globalHeaderConfig.crossOriginResourcePolicy(GlobalHeaderConfig.CrossOriginResourcePolicy.SAME_ORIGIN);
        if (Config.isSTSEnabled) {
            globalHeaderConfig.strictTransportSecurity(Duration.ofDays(356), false);
        }
        if (Config.CUSTOM_CSP != null && !Config.CUSTOM_CSP.isBlank()) {
            globalHeaderConfig.contentSecurityPolicy(Config.CUSTOM_CSP);
        } else {
            globalHeaderConfig.contentSecurityPolicy(String.format("default-src 'self';  img-src * 'self' data:; object-src 'none'; media-src media.discordapp.com; style-src-attr 'unsafe-hashes' 'self' 'sha256-biLFinpqYMtWHmXfkA1BPeCY0/fNt46SAZ+BBk5YUog='; script-src-elem 'self' https://cdn.jsdelivr.net/npm/@twemoji/api@14.1.0/dist/twemoji.min.js %s;", Objects.requireNonNullElse(Config.CSP_SCRIPT_SRC_ELEM_EXTRA, "")));

        }

        return globalHeaderConfig;
    }

    private static void registerValidators() {
        // Javalin uses these in param validators to convert the string to the correct type
        JavalinValidation.register(LocalTime.class, LocalTime::parse);
        JavalinValidation.register(LocalDate.class, LocalDate::parse);
        JavalinValidation.register(ZoneId.class, ZoneId::of);
    }
}