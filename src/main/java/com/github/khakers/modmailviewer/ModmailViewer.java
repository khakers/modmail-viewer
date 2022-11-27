package com.github.khakers.modmailviewer;

import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

public class ModmailViewer {

    public static final Instant BUILD_TIMESTAMP;

    public static final String COMMIT_ID;

    public static final String PRECISE_GIT_VERSION_ID;

    static {
        var gitProperties = new Properties();
        try (var gitPropertiesStream = ModmailViewer.class.getResourceAsStream("/git.properties")) {
            gitProperties.load(gitPropertiesStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BUILD_TIMESTAMP = Instant.parse(gitProperties.getProperty("buildTimestamp"));

        COMMIT_ID = gitProperties.getProperty("git.commit.id.abbrev", "");
        PRECISE_GIT_VERSION_ID = gitProperties.getProperty("git.commit.id.describe", "UNKNOWN VERSION ID");

    }
}
