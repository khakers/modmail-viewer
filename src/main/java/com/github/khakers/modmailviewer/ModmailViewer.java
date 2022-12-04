package com.github.khakers.modmailviewer;

import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

public class ModmailViewer {

    public static final Instant BUILD_TIMESTAMP;

    public static final String COMMIT_ID;

    public static final String PRECISE_GIT_VERSION_ID;

    public static final String VERSION;

    public static final String BRANCH;

    static {
        String PRECISE_GIT_VERSION_ID_TEMP;
        var gitProperties = new Properties();
        try (var gitPropertiesStream = ModmailViewer.class.getResourceAsStream("/git.properties")) {
            gitProperties.load(gitPropertiesStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BUILD_TIMESTAMP = Instant.parse(gitProperties.getProperty("buildTimestamp"));
        BRANCH = gitProperties.getProperty("git.branch", "NULL");
        COMMIT_ID = gitProperties.getProperty("git.commit.id.abbrev", "");
        PRECISE_GIT_VERSION_ID_TEMP = gitProperties.getProperty("git.commit.id.describe", "UNKNOWN VERSION ID");
        if (PRECISE_GIT_VERSION_ID_TEMP.isBlank()) {
            PRECISE_GIT_VERSION_ID_TEMP = "UNKNOWN VERSION ID";
        }
        PRECISE_GIT_VERSION_ID = PRECISE_GIT_VERSION_ID_TEMP;
        if (BRANCH.equalsIgnoreCase("HEAD") && gitProperties.getProperty("git.dirty", "false").equals("false")) {
            VERSION = gitProperties.getProperty("git.build.version", "");
        } else {
            VERSION = gitProperties.getProperty("git.build.version", "")+"-"+BRANCH+"-DIRTY";
        }

    }
}
