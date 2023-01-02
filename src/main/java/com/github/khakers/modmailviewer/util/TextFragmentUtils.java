package com.github.khakers.modmailviewer.util;

import org.owasp.encoder.Encode;

public class TextFragmentUtils {
//    public static String getTextFragment(String text) {
//        if (text == null || text.isEmpty() || text.isBlank()) {
//            return "";
//        }
//        return Objects.nonNull(text) && !text.isEmpty() ? "#:~:text=" + Encode.forUriComponent(search.substring(0, search.indexOf(" "))) + "," + : ""
//    }

    public static String getTextFragmentAsSnippet(String text) {
        if (text == null || text.isEmpty() || text.isBlank()) {
            return "";
        }
        return "#:~:text=" + Encode.forUriComponent(text);
    }
}
