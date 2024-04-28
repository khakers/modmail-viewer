package com.github.khakers.modmailviewer.updatecheck;

import java.time.Instant;
import java.util.Optional;

public interface UpdateChecker {
    static boolean isContainerized() {
        return System.getProperty("ENV_TYPE").equalsIgnoreCase("containerized");
    }

    boolean isUpdateAvailable();

    Optional<Version> getLatestVersion();

    Optional<Instant> getUpdateFoundTime();

    boolean isSemVerUpdateAvailable(String version);
}
