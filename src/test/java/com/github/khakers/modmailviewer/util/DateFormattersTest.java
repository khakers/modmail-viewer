package com.github.khakers.modmailviewer.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

class DateFormattersTest {

    @ParameterizedTest
    @ValueSource(strings = { "2022-11-08 07:14:55.624724", "2022-11-09 03:51:31.687000+00:00", "2022-11-09 03:43:26.171000+00:00", "2022-11-08 05:30:50.694581" })
    void testTimeParsing(String timestamp) {
        var time = DateFormatters.DATABASE_TIMESTAMP_FORMAT.parse(timestamp, Instant::from);
        System.out.println("------");
        System.out.println(timestamp);
        System.out.println(time.toString());
        System.out.println(DateFormatters.DATABASE_TIMESTAMP_FORMAT.format(time));
    }

}