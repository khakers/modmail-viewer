package com.github.khakers.modmailviewer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class VersionTest {

    @ParameterizedTest()
    @CsvFileSource(resources = "/validSemver.csv")
    void version_ShouldParseAllValidSemver(String input){
        System.out.println(new Version(input));
    }
    @ParameterizedTest()
    @CsvFileSource(resources = "/invalidSemver.csv")
    void version_ShouldNotParseInvalidSemver(String input) {
        Assertions.assertThrows(IllegalStateException.class, () -> new Version(input));
    }

    //Test release comparisons
    @Test
    void version_NewerMajorReleaseIsNewer() {
        var oldVersion = new Version("1.0.0");
        var newerVersion = new Version("2.0.0");
        Assertions.assertTrue(newerVersion.isNewerThan(oldVersion));
    }

    @Test
    void version_OlderMajorReleaseIsOlder() {
        var oldVersion = new Version("1.0.0");
        var newerVersion = new Version("2.0.0");
        Assertions.assertTrue(oldVersion.isOlderThan(newerVersion));
    }

    @Test
    void version_NewerMinorReleaseIsNewer() {
        var oldVersion = new Version("1.0.0");
        var newerVersion = new Version("1.1.0");
        Assertions.assertTrue(newerVersion.isNewerThan(oldVersion));
    }

    @Test
    void version_OlderMinorReleaseIsOlder() {
        var oldVersion = new Version("1.0.0");
        var newerVersion = new Version("1.1.0");
        Assertions.assertTrue(oldVersion.isOlderThan(newerVersion));
    }

    @Test
    void version_NewerPatchReleaseIsNewer() {
        var oldVersion = new Version("1.0.0");
        var newerVersion = new Version("1.0.1");
        Assertions.assertTrue(newerVersion.isNewerThan(oldVersion));
    }

    @Test
    void version_OlderPatchReleaseIsOlder() {
        var oldVersion = new Version("1.0.0");
        var newerVersion = new Version("1.0.1");
        Assertions.assertTrue(oldVersion.isOlderThan(newerVersion));
    }

    @Test
    void version_NewerPrereleaseIsNewer() {
        var olderPrerelease = new Version("1.0.0-alpha.1");
        var newerPrerelease = new Version("1.0.0-alpha.2");
        // newer version should be greater than old version
        Assertions.assertTrue(newerPrerelease.isNewerThan(olderPrerelease), "Newer pre-release version should be greater than older prerelease version");
    }
    @Test
    void version_OlderPrereleaseIsOlder() {
        var oldVersion = new Version("1.0.0-alpha.1");
        var newerVersion = new Version("1.0.0-alpha.2");
        // newer version should be greater than old version
        Assertions.assertTrue(oldVersion.isOlderThan(newerVersion), "Newer pre-release version should be greater than older prerelease version");
    }
    @Test
    void version_IdenticalPrereleaseVersionsAreSame() {
        var version = new Version("1.0.0-alpha.1");
        var version1 = new Version("1.0.0-alpha.1");
        Assertions.assertFalse(version.isOlderThan(version1));
        Assertions.assertFalse(version.isNewerThan(version1));
    }
    @Test
    void version_StableGreaterThanPrerelease() {
        var stable = new Version("1.0.0");
        var prerelease = new Version("1.0.0-alpha");
        // stable should be greater than prerelease
        Assertions.assertTrue(stable.isNewerThan(prerelease), "Stable should be newer than pre-release of the same version");
    }

    @Test
    void version_PrereleaseLessThanStable() {
        var stable = new Version("1.0.0");
        var prerelease = new Version("1.0.0-alpha");
        // prerelease should be less than stable
        Assertions.assertTrue(prerelease.isOlderThan(stable), "Pre-release should be older than stable of the same version");
    }
}