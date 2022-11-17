package com.github.khakers.modmailviewer.markdown.timestamp;

import com.vladsch.flexmark.util.sequence.BasedSequence;

import static com.github.khakers.modmailviewer.markdown.timestamp.TimestampType.*;

public class TimestampUtil {
    public static TimestampType parseTimestampStyle(BasedSequence style) {
        if (style == null) {
            return SHORT_DATE_TIME;
        }
        return switch (style.toString()) {
            case "t" -> SHORT_TIME;
            case "T" -> LONG_TIME;
            case "d" -> SHORT_DATE;
            case "D" -> LONG_DATE;
            case "f" -> SHORT_DATE_TIME;
            case "F" -> LONG_DATE_TIME;
            case "R" -> RELATIVE;
            default -> SHORT_DATE_TIME;
        };
    }
}
