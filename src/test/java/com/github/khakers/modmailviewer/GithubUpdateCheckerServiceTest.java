package com.github.khakers.modmailviewer;

import com.github.khakers.modmailviewer.updatecheck.GithubUpdateCheckerService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GithubUpdateCheckerServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"v0.0.1", "50.0.0"})
    void isSemVerUpdateAvailable(String version) {
        var updateChecker = new GithubUpdateCheckerService();
        assertTrue(updateChecker.isSemVerUpdateAvailable("0.0.1"));
    }
}