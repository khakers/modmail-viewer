package com.github.khakers.modmailviewer.updatecheck;

import java.time.Instant;
import java.util.Optional;

public class NoopUpdateChecker implements UpdateChecker{
    @Override
    public boolean isUpdateAvailable() {
        return false;
    }

    @Override
    public Optional<Version> getLatestVersion() {
        return Optional.empty();
    }

    @Override
    public Optional<Instant> getUpdateFoundTime() {
        return Optional.empty();
    }

    @Override
    public boolean isSemVerUpdateAvailable(String version) {
        return false;
    }
}
