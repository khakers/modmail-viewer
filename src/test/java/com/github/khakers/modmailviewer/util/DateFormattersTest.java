package com.github.khakers.modmailviewer.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

class DateFormattersTest {
    private static final String[] TEST_STRINGS = {"2022-11-08 07:14:55.624724", "2022-11-09 03:51:31.687000+00:00", "2022-11-09 03:43:26.171000+00:00", "2022-11-08 05:30:50.694581", "2023-07-28 22:26:23+00:00", "2023-07-28 22:20:23.052530+00:00"};

    @ParameterizedTest
    @ValueSource(strings = {"2022-11-08 07:14:55.624724", "2022-11-09 03:51:31.687000+00:00", "2022-11-09 03:43:26.171000+00:00", "2022-11-08 05:30:50.694581", "2023-07-28 22:26:23+00:00", "2023-07-28 22:20:23.052530+00:00", "2023-07-28 22:20:23.052530+07:00"})
    void testTimeParsing(String timestamp) {
        var time = DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from);
        System.out.println("------------------------");
        System.out.println("ts        = "+timestamp);
        System.out.println("parsed    = "+time.toString());
        System.out.println("formatted = "+DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME.format(time));
    }
    @ParameterizedTest
    @ValueSource(strings = { "2022-11-08 07:14:55.624724", "2022-11-09 03:51:31.687000+00:00", "2022-11-09 03:43:26.171000+00:00", "2022-11-08 05:30:50.694581", "2023-07-28 22:26:23+00:00", "2023-07-28 22:20:23.052530+00:00", "2023-07-28 22:20:23.052530+07:00" })
    void testPatternTimeParsing(String timestamp) {
        var formatter = DateTimeFormatter.ofPattern(DateFormatters.PYTHON_STR_ISO_OFFSET_DATE_TIME_STRING).withZone(ZoneId.of("UTC"));
        var time = formatter.parse(timestamp, Instant::from);
        System.out.println("------------------------");
        System.out.println("ts        = "+timestamp);
        System.out.println("parsed    = "+time.toString());
        System.out.println("formatted = "+formatter.format(time));
    }

}