package com.github.khakers.modmailviewer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateCheckerTest {

    @ParameterizedTest
    @ValueSource(strings = {"v0.0.1", "50.0.0"})
    void isSemVerUpdateAvailable(String version) {
        var updateChecker = new UpdateChecker();
        assertTrue(updateChecker.isSemVerUpdateAvailable("0.0.1"));
    }
}