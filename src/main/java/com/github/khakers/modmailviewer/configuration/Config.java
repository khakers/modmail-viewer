package com.github.khakers.modmailviewer.configuration;

import com.github.khakers.modmailviewer.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.path.mapper.SnakeCasePathMapper;
import org.github.gestalt.config.source.ClassPathConfigSource;
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.github.gestalt.config.source.FileConfigSource;
import org.github.gestalt.config.source.SystemPropertiesConfigSource;

import java.io.File;

public class Config {
    public static final Gestalt gestalt;
    public static final AppConfig appConfig;
    private static final Logger logger = LogManager.getLogger();

    static {
        try {
            var configURI = System.getProperty("configFile");

            var gestaltBuilder = new GestaltBuilder()
                  .setTreatNullValuesInClassAsErrors(true)
                  .setTreatMissingValuesAsErrors(false)
                  .addSource(new ClassPathConfigSource("default.properties"));

            if (configURI != null) {
                File file = new File(configURI);
                if (file.exists() && file.isFile()) {
                    logger.info("Using config file: " + file.getPath());
                    gestaltBuilder.addSource(new FileConfigSource(file));
                } else {
                    logger.fatal("Config file does not exist: " + configURI);
                    throw new RuntimeException("Config file does not exist: " + configURI);
                }

            }

            gestalt = gestaltBuilder.addSource(new EnvironmentConfigSource(Main.envPrepend))
                  .addSource(new SystemPropertiesConfigSource())
                  .addDefaultPathMappers()
                  .addPathMapper(new SnakeCasePathMapper())
                  //              .addSource(new FileConfigSource(devFile))
                  .build();

        } catch (GestaltException e) {
            throw new RuntimeException(e);
        }
        try {
            gestalt.loadConfigs();
        } catch (GestaltException e) {
            logger.fatal("Failed to load configs", e);
            throw new RuntimeException(e);
        }

        try {
            appConfig = gestalt.getConfig("app", AppConfig.class);
        } catch (GestaltException e) {
            logger.fatal("Failed to load application configuration", e);
            throw new RuntimeException(e);
        }
        logger.debug("Loaded application configuration: " + appConfig.toString());

        initialConfigValidation();
    }

    private static void initialConfigValidation() {
        if (appConfig.isAuthEnabled()) {
            try {
                // We're loading the auth config here to make sure it's valid
                // Gestalt will throw an exception if it's not which provides explicit information on which keys are missing
                var authConfig = gestalt.getConfig("app.auth", AuthConfig.class);
                if (appConfig.auth().isEmpty()) {
                    logger.error("mismatch between gestalt config record and app.auth config");
                }
                if (authConfig.secretKey().length() < 32) {
                    logger.fatal("Secret key is too short, must be at least 32 characters");
                    System.exit(1);
                }
            } catch (GestaltException e) {
                logger.fatal("Failed to load authentication configuration", e);
                logger.info("You must manually disable authentication if you wish to run modmail-viewer without any authentication");
                System.exit(1);
            }

        }
    }
}
