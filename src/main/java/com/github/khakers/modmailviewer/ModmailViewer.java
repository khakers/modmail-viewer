package com.github.khakers.modmailviewer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

public class ModmailViewer {
    public static final Instant BUILD_TIMESTAMP;
    public static final String COMMIT_ID;
    public static final String COMMIT_ID_DESCRIBE;
    public static final String PRECISE_GIT_VERSION_ID;
    public static final String VERSION;
    public static final String BRANCH;
    public static final String TAG;
    private static final Logger logger = LogManager.getLogger();

    static {
        String BRANCH1;
        String VERSION1;
        String COMMIT_ID_DESCRIBE1;
        String COMMIT_ID1;
        Instant BUILD_TIMESTAMP1;
        String TAG1;
        String PRECISE_GIT_VERSION_ID_TEMP;
        var gitProperties = new Properties();
        try (var gitPropertiesStream = ModmailViewer.class.getResourceAsStream("/git.properties")) {
            if (gitPropertiesStream == null) {
                throw new IOException("no git properties present");
            }
            gitProperties.load(gitPropertiesStream);
            if (gitProperties.getProperty("buildTimestamp") != null) {
                BUILD_TIMESTAMP1 = Instant.parse(gitProperties.getProperty("buildTimestamp"));
            } else {
                BUILD_TIMESTAMP1 = Instant.EPOCH;
            }
            BRANCH1 = gitProperties.getProperty("git.branch", "NULL");
            COMMIT_ID1 = gitProperties.getProperty("git.commit.id.abbrev", "NULL");
            COMMIT_ID_DESCRIBE1 = gitProperties.getProperty("git.commit.id.describe", "NULL");
            PRECISE_GIT_VERSION_ID_TEMP = gitProperties.getProperty("git.commit.id.describe", "UNKNOWN VERSION ID");
            if (PRECISE_GIT_VERSION_ID_TEMP.isBlank()) {
                PRECISE_GIT_VERSION_ID_TEMP = "UNKNOWN VERSION ID";
            }
            if (BRANCH1.equalsIgnoreCase("HEAD") && gitProperties.getProperty("git.dirty", "false").equals("false")) {
                VERSION1 = gitProperties.getProperty("git.build.version", "NULL");
            } else {
                VERSION1 = COMMIT_ID_DESCRIBE1;
            }

            TAG1 = gitProperties.getProperty("git.tags", "UNKNOWN");
        } catch (IOException e) {
            logger.error(e);
            BUILD_TIMESTAMP1 = Instant.EPOCH;
            COMMIT_ID1 = "";
            COMMIT_ID_DESCRIBE1 = "";
            PRECISE_GIT_VERSION_ID_TEMP = "";
            VERSION1 = "";
            BRANCH1 = "";
            TAG1 = "";
        }

        BRANCH = BRANCH1;
        VERSION = VERSION1;
        COMMIT_ID_DESCRIBE = COMMIT_ID_DESCRIBE1;
        COMMIT_ID = COMMIT_ID1;
        BUILD_TIMESTAMP = BUILD_TIMESTAMP1;
        TAG = TAG1;
        PRECISE_GIT_VERSION_ID = PRECISE_GIT_VERSION_ID_TEMP;

    }

    public static boolean isSemVerRelease() {
        return !TAG.isEmpty();
    }
}
