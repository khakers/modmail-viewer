package com.github.khakers.modmailviewer.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Localizer {
    private static final ConcurrentMap<Locale, Localizer> byLocale = new ConcurrentHashMap<>();
    private final ResourceBundle bundle;

    private Localizer(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public static Localizer getInstance(@NotNull String languageTag) {
        Locale locale = new Locale(languageTag);
        return getInstance(locale);
    }

    public static Localizer getInstance(@NotNull Locale locale) {
        return byLocale.computeIfAbsent(locale, k -> {
            ResourceBundle bundle = ResourceBundle.getBundle("localization/LocalizationBundle", k);
            return new Localizer(bundle);
        });
    }

    public String localize(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

}
