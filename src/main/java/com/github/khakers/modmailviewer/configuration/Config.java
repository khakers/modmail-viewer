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
    private static final Logger logger = LogManager.getLogger();


    public static final Gestalt gestalt;
    public static final AppConfig appConfig;

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
                }
                else {
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
    }
}
