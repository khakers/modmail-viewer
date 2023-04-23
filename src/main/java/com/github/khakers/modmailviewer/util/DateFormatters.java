package com.github.khakers.modmailviewer.util;

import java.time.ZoneId;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateFormatters {
    @Deprecated
    public static final DateTimeFormatter DATABASE_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .parseLenient()
          .appendPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
          .optionalStart()
          .appendPattern("xxx")
          .toFormatter()
          .withZone(ZoneId.of("UTC"));

    public static final DateTimeFormatter PYTHON_STR_ISO_OFFSET_DATE_TIME = new DateTimeFormatterBuilder()
          // Functionally identical to ISO_DATE_TIME, but allows for a space instead of a 'T' between date and time
          .parseCaseInsensitive()
          .append(DateTimeFormatter.ISO_LOCAL_DATE)
          // Python uses a ' ' instead of a 'T' to separate date and time
          .appendLiteral(' ')
          .append(DateTimeFormatter.ISO_LOCAL_TIME)
          // Offset segment
          .parseLenient()
          .optionalStart()
          .appendOffsetId()
          .parseStrict()
          .toFormatter()
          .withChronology(IsoChronology.INSTANCE)
          .withZone(ZoneId.of("UTC"));

    public static final String PYTHON_STR_ISO_OFFSET_DATE_TIME_STRING = "uuuu-MM-dd HH:mm[:ss[.n]][XXX]";

    public static final DateTimeFormatter SIMPLE_TIME_FORMAT = new DateTimeFormatterBuilder()
          .appendPattern("HH:mm a")
          .toFormatter()
          .withZone(ZoneId.of("UTC"));

    public static final DateTimeFormatter MINI_DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .toFormatter()
            .withZone(ZoneId.of("UTC"));
}
