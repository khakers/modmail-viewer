package com.github.khakers.modmailviewer;

import org.junit.jupiter.api.Assertions;
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
    void version_ShouldNotParseInvalidSemver(String input){
        Assertions.assertThrows(IllegalStateException.class, () -> new Version(input));
    }
}