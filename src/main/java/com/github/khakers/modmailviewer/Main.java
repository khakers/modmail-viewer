package com.github.khakers.modmailviewer;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.khakers.modmailviewer.auditlog.AuditEventDAO;
import com.github.khakers.modmailviewer.auditlog.MongoAuditEventLogger;
import com.github.khakers.modmailviewer.auditlog.NoopAuditEventLogger;
import com.github.khakers.modmailviewer.auditlog.OutboundAuditEventLogger;
import com.github.khakers.modmailviewer.auth.AuthHandler;
import com.github.khakers.modmailviewer.auth.Role;
import com.github.khakers.modmailviewer.configuration.AppConfig;
import com.github.khakers.modmailviewer.configuration.CSPConfig;
import com.github.khakers.modmailviewer.configuration.SSLConfig;
import com.github.khakers.modmailviewer.log.LogController;
import com.github.khakers.modmailviewer.markdown.channelmention.ChannelMentionExtension;
import com.github.khakers.modmailviewer.markdown.customemoji.CustomEmojiExtension;
import com.github.khakers.modmailviewer.markdown.spoiler.SpoilerExtension;
import com.github.khakers.modmailviewer.markdown.timestamp.TimestampExtension;
import com.github.khakers.modmailviewer.markdown.underline.UnderlineExtension;
import com.github.khakers.modmailviewer.markdown.usermention.UserMentionExtension;
import com.github.khakers.modmailviewer.page.admin.AdminController;
import com.github.khakers.modmailviewer.page.audit.AuditController;
import com.github.khakers.modmailviewer.page.dashboard.DashboardController;
import com.github.khakers.modmailviewer.page.dashboard.MetricsAccessor;
import com.github.khakers.modmailviewer.util.RoleUtils;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.path.mapper.SnakeCasePathMapper;
import org.github.gestalt.config.source.ClassPathConfigSource;
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
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
    public static final UpdateChecker updateChecker = new UpdateChecker();
    private static final Logger logger = LogManager.getLogger();
    private static final String envPrepend = "MODMAIL_VIEWER";
    public static ModMailLogDB modMailLogDB;
    public static MetricsAccessor metricsAccessor;
    static AuthHandler authHandler;
    private static OutboundAuditEventLogger auditLogger;

    public static void main(String[] args) throws GestaltException {

        Gestalt gestalt = new GestaltBuilder()
              .setTreatNullValuesInClassAsErrors(true)
              .setTreatMissingValuesAsErrors(false)
              .addSource(new ClassPathConfigSource("/default.properties"))
              .addSource(new EnvironmentConfigSource(envPrepend))
              .addDefaultPathMappers()
              .addPathMapper(new SnakeCasePathMapper())
//              .addSource(new ClassPathConfigSource("/default.properties"))  // Load the default property files from resources.
//              .addSource(new FileConfigSource(devFile))
//              .addSource(new MapConfigSource(configs))
              .build();
        gestalt.loadConfigs();

        var appConfig = gestalt.getConfig("app", AppConfig.class);
        logger.debug(appConfig.toString());
        var authConfig = appConfig.auth();
        var auditLogConfig = appConfig.auditLogConfig();
//        var cspConfig = appConfig.cspConfig();


        TemplateEngine templateEngine;

        if (appConfig.dev()) {
            templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/main/jte")), ContentType.Html);
        } else {
            templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        }
        JavalinJte.init(templateEngine);

        registerValidators();


        var uri = URI.create(appConfig.url()); // Validate the URL
        if (uri.getScheme().equals("http")) {
            logger.warn("You are running Modmail-Viewer over HTTP with an http callback URI. It is highly recommended that you use HTTPS.");
        } else if (uri.getScheme().equals("https") && !appConfig.secureCookies()) {
            logger.warn("You are running Modmail-Viewer over HTTPS but have disabled enabled secure cookies. There should be no reason to do this. ");
        }
        var callbackUri = uri.resolve("./callback"); // Append the callback path to the URL
        logger.debug("Callback URL: " + callbackUri);

        var connectionString = new ConnectionString(appConfig.mongodbUri());

        MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
              .applyConnectionString(connectionString)
              .applicationName("Modmail-Viewer")
              .codecRegistry(
                    CodecRegistries.fromRegistries(
                          MongoClientSettings
                                .getDefaultCodecRegistry(),
                          CodecRegistries
                                .fromProviders(PojoCodecProvider.builder()
                                      .automatic(true)
                                      .build())))
              .build());

        var mongoClientDatabase = mongoClient.getDatabase(Objects.requireNonNullElse(connectionString.getDatabase(), "modmail_bot"));

        modMailLogDB = new ModMailLogDB(mongoClientDatabase, appConfig.botId());

        // We will always need an audit logger for searching, even if pushing to an audit logger is disabled
        AuditEventDAO auditLogClient = new MongoAuditEventLogger(mongoClient, appConfig.mongodbUri(), "modmail_bot", "audit_log");
        auditLogger = authConfig.isPresent() && authConfig.get().enabled()
              ? (OutboundAuditEventLogger) auditLogClient
              : new NoopAuditEventLogger();



        authHandler = authConfig.isPresent() && authConfig.get().enabled() ?
              new AuthHandler(callbackUri.toString(),
                    authConfig.get().discordClient().clientId(),
                    authConfig.get().discordClient().clientSecret(),
                    authConfig.get().secretKey(),
                    modMailLogDB,auditLogger, authConfig.get().discordClient().guildId(), appConfig.secureCookies())
              : null;

        metricsAccessor = new MetricsAccessor(mongoClientDatabase);

        var adminController = new AdminController(auditLogClient);
        var auditController = new AuditController(auditLogClient);


        var app = Javalin.create(javalinConfig -> {
                  try {
                      Main.configure(javalinConfig, appConfig);
                  } catch (GestaltException e) {
                      throw new RuntimeException(e);
                  }
              })
              .get("/hello", ctx -> ctx.status(200).result("hello"), RoleUtils.anyone())
              .post("/logout", ctx -> {
                  auditLogger.pushAuditEventWithContext(ctx, "viewer.logout", "User logged out");
                  ctx.removeCookie("jwt");
                  ctx.result("logout successful");
                  if (authConfig.isEmpty() || !authConfig.get().enabled()) {
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
//                  if (ctx.statusCode() == HttpStatus.FORBIDDEN.getCode()) {
//
//                  }

                  if (auditLogConfig.isDetailedAuditingEnabled()) {
                      auditLogger.pushAuditEventWithContext(ctx, "viewer.log.accessed", String.format("accessed log id %s", ctx.pathParam("id")));
                  }
              })
              .get("/admin", adminController.serveAdminPage, RoleUtils.atLeastAdministrator())
              .get("/audit/{id}", auditController.serveAuditPage, RoleUtils.atLeastAdministrator())
              .after("/api/*", ctx -> {
                  if (auditLogConfig.isApiAuditingEnabled()) {
                      if (ctx.statusCode() == HttpStatus.FORBIDDEN.getCode()) {
                          auditLogger.pushAuditEventWithContext(ctx, "viewer.api", String.format("DENIED %s %s", ctx.method(), ctx.path()));
                      } else {
                          auditLogger.pushAuditEventWithContext(ctx, "viewer.api", String.format("%s %s", ctx.method(), ctx.path()));
                      }
                  }
              })
              .start(appConfig.httpPort());

            if (authConfig.isPresent() && authConfig.get().enabled()) {
                // Register api only if authentication is enabled
                app.get("/api/logs/{id}", ctx -> {
                          var entry = modMailLogDB.getModMailLogEntry(ctx.pathParam("id"));
                          entry.ifPresentOrElse(
                                    ctx::json,
                                    () -> {
                                        throw new NotFoundResponse();
                                    });

                      }, RoleUtils.atLeastAdministrator())
                      .get("/api/config", ctx -> ctx.json(modMailLogDB.getConfig()), RoleUtils.atLeastAdministrator());

                app.get("/callback", authHandler::handleCallback, Role.ANYONE);
            }


        logger.info("You are running Modmail-Viewer {} built on {}", ModmailViewer.VERSION, ModmailViewer.BUILD_TIMESTAMP);
    }

    private static void configure(JavalinConfig config, AppConfig appConfig) throws GestaltException {
        var sslOptions = appConfig.sslOptions();
        var cspConfig = appConfig.cspConfig();

        config.showJavalinBanner = false;
        config.jsonMapper(new JavalinJackson().updateMapper(objectMapper -> objectMapper.registerModule(new Jdk8Module())));
        config.plugins.enableGlobalHeaders(() -> configureHeaders(sslOptions.get(), cspConfig));
        if (sslOptions.isPresent() && sslOptions.get().httpsOnly()) {
            logger.info("HTTPS only is ENABLED");
            config.plugins.enableSslRedirects();
        }
        if (appConfig.dev()) {
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
        if (appConfig.dev()) {
            logger.info("Dev mode is ENABLED");
            config.showJavalinBanner = true;
            config.plugins.enableDevLogging();
        }
        if (appConfig.auth().isPresent() && appConfig.auth().get().enabled()) {
            logger.debug("Authentication is ENABLED");
            config.accessManager(authHandler::HandleAuth);
        } else {
            logger.warn("Authentication is DISABLED");
            config.accessManager((handler, context, set) -> handler.handle(context));
        }
        if (sslOptions.isPresent() && sslOptions.get().enabled()) {
            logger.info("SSL is ENABLED");
            logger.debug(sslOptions.get().toString());
            SSLPlugin sslPlugin = getSslPlugin(appConfig, sslOptions.get());
            config.plugins.register(sslPlugin);
        } else {
            if (sslOptions.isPresent())
                logger.warn("SSL options are present but SSL was disabled");
            logger.debug("SSL is DISABLED");
        }
    }

    @NotNull
    private static SSLPlugin getSslPlugin(AppConfig appConfig, SSLConfig sslOptions) {
        return new SSLPlugin(sslConfig -> {
            sslConfig.pemFromPath(sslOptions.cert().get(), sslOptions.key().get());
            sslConfig.insecurePort = appConfig.httpPort();
            sslConfig.securePort = appConfig.httpsPort();
            sslConfig.sniHostCheck = sslOptions.isSNIEnabled();
            if (!sslOptions.httpsOnly()) {
                logger.warn("SSL is ENABLED but HTTPS only is DISABLED");
            }
        });
    }

    private static GlobalHeaderConfig configureHeaders(SSLConfig sslOptions, CSPConfig cspConfig) {
        var globalHeaderConfig =  new GlobalHeaderConfig();
        globalHeaderConfig.xFrameOptions(GlobalHeaderConfig.XFrameOptions.DENY);
        globalHeaderConfig.xContentTypeOptionsNoSniff();
        globalHeaderConfig.xPermittedCrossDomainPolicies(GlobalHeaderConfig.CrossDomainPolicy.NONE);
        globalHeaderConfig.crossOriginOpenerPolicy(GlobalHeaderConfig.CrossOriginOpenerPolicy.SAME_ORIGIN);
        globalHeaderConfig.crossOriginResourcePolicy(GlobalHeaderConfig.CrossOriginResourcePolicy.SAME_ORIGIN);
        if (sslOptions.isSTSEnabled()) {
            globalHeaderConfig.strictTransportSecurity(Duration.ofDays(356), false);
        }
        if (cspConfig.override().isPresent() && !cspConfig.override().get().isBlank()) {
            globalHeaderConfig.contentSecurityPolicy(cspConfig.override().get());
        } else {
            globalHeaderConfig.contentSecurityPolicy(String.format(
                        "default-src 'self';  " +
                        "img-src * 'self' data:; " +
                        "object-src 'none'; " +
                        "media-src media.discordapp.com; " +
                        "style-src-attr 'unsafe-hashes' 'self' 'sha256-biLFinpqYMtWHmXfkA1BPeCY0/fNt46SAZ+BBk5YUog='; " +
                        "script-src-elem 'self' https://cdn.jsdelivr.net/npm/@twemoji/api@14.1.0/dist/twemoji.min.js %s;",
                  cspConfig.extraScriptSources().orElse("")));
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