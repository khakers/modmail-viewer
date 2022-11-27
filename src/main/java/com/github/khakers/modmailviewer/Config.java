package com.github.khakers.modmailviewer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Assert;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;

public class Config {
    private static final Logger logger = LogManager.getLogger();

    private static final String ENV_PREPEND = "MODMAIL_VIEWER";

    public static final int httpPort = Objects.nonNull(System.getenv(ENV_PREPEND + "_HTTP_PORT"))
            ? Integer.parseInt(System.getenv(ENV_PREPEND + "_HTTP_PORT"))
            : 80;

    static final int httpsPort = Objects.nonNull(System.getenv(ENV_PREPEND + "_HTTPS_PORT"))
            ? Integer.parseInt(System.getenv(ENV_PREPEND + "_HTTPs_PORT"))
            : 443;

    public static final String MONGODB_URI = Assert.requireNonEmpty(System.getenv(ENV_PREPEND + "_MONGODB_URI"), "No mongodb URI provided. provide one with the option \""+ENV_PREPEND+"_MONGODB_URI\"");

    public static final String WEB_URL = Assert.requireNonEmpty(System.getenv(ENV_PREPEND + "_URL"), "No URL provided. provide one with the option \""+ENV_PREPEND+"_URL\"");

    public static final boolean isSecure = isSetToTrue(System.getenv(ENV_PREPEND + "_SSL"));
    @Nullable
    public static final String SSL_CERT = isSecure
            ? Assert.requireNonEmpty(System.getenv(ENV_PREPEND + "_SSL_CERT"), "SSL was enabled but no certificate file path was provided. Provide one with the option \""+ENV_PREPEND+"_SSL_CERT\"")
            : null;
    @Nullable
    public static final String SSL_KEY = isSecure
            ? Assert.requireNonEmpty(System.getenv(ENV_PREPEND + "_SSL_KEY"), "SSL was enabled but no key file path was provided. Provide one with the option \""+ENV_PREPEND+"_DISCORD_SSL_KEY\"")
            : null;
    public static final boolean isHttpsOnly = isSecure && isNotNullAndFalse(System.getenv(ENV_PREPEND + "_HTTPS_ONLY"), true);

    public static final boolean isAuthEnabled = isNotNullAndFalse(System.getenv(ENV_PREPEND + "_AUTH_ENABLED"), true);

    public static final boolean isDevMode = isNotNullAndFalse(System.getenv(ENV_PREPEND + "_DEV"), false);

    public static final boolean isSNIEnabled = isSecure && !isDevMode && isNotNullAndFalse(System.getenv(ENV_PREPEND + "_SNI"), false);

    public static final String DISCORD_CLIENT_ID = isAuthEnabled
            ? Assert.requireNonEmpty(System.getenv(ENV_PREPEND + "_DISCORD_OAUTH_CLIENT_ID"), "No Discord client ID provided. Provide one with the option \""+ENV_PREPEND+"_DISCORD_OAUTH_CLIENT_ID\"")
            : null;
    public static final String DISCORD_CLIENT_SECRET = isAuthEnabled
            ? Assert.requireNonEmpty(System.getenv(ENV_PREPEND + "_DISCORD_OAUTH_CLIENT_SECRET"), "No Discord client ID provided. Provide one with the option \""+ENV_PREPEND+"_DISCORD_OAUTH_CLIENT_SECRET\"")
            : null;

    public static final String JWT_SECRET_KEY;


    static {
        var jwtSecretKey = System.getenv("MODMAIL_VIEWER_SECRETKEY");
        if (jwtSecretKey == null || jwtSecretKey.isEmpty()) {
            logger.warn("Generated a random key for signing tokens. Sessions will not persist between restarts");
            JWT_SECRET_KEY = new BigInteger(256, new SecureRandom()).toString(32);
        } else if (jwtSecretKey.length() < 32) {
            JWT_SECRET_KEY = jwtSecretKey;
            logger.warn("Your secret key is too short! it should be at least 32 characters (256 bits). Short keys can be trivially brute forced allowing an attacker to create their own auth tokens");
        } else {
            JWT_SECRET_KEY = jwtSecretKey;
        }
    }

    private static <T> T notNullObjOrElse(T obj, T defaultObj) {
        if (obj == null) {
            return defaultObj;
        }
        return obj;
    }

//    /**
//     * Runs
//     *
//     * @param obj
//     * @param supplier supplier to run if obj is not null
//     * @param defaultObj
//     * @param <T>
//     * @return value of supplier if not null or defaultObj
//     */
//    private static <T> T runIfNotNullElse(T obj, Supplier<? extends T> supplier, T defaultObj) {
//        Objects.requireNonNullElseGet()
//    }

    /**
     * Returns true if the given String value is not null and the content is not "false".
     *
     *
     * @param s String
     * @return
     */
    private static boolean isNotNullAndFalse(String s) {
        return isNotNullAndFalse(s, false);
    }

    /**
     * Returns true if the given String value is not "false".
     * Returns ifNull if the given string is null
     *
     * @param s String
     * @param ifNull Return value if the String is null
     * @return
     */
    private static boolean isNotNullAndFalse(String s, boolean ifNull) {
        if (s == null) {
            return ifNull;
        }
        return !s.equalsIgnoreCase("false");
    }

    /**
     * Returns true if the given String value is not null and the content is not "true".
     * Explicitly requires a value of "true" to return true
     *
     * @param s String
     * @return
     */
    private static boolean isSetToTrue(String s) {
        return isSetToTrue(s, false);
    }



    /**
     * Returns true if the given String value is not null and the content is "true".
     * Returns ifNull if the given string is null
     * Explicitly requires a value of "true" to return true
     *
     * @param s String
     * @param ifNull Return value if the String is null
     * @return
     */
    private static boolean isSetToTrue(String s, boolean ifNull) {
        if (s == null) {
            return ifNull;
        }
        return s.equalsIgnoreCase("true");
    }

    /**
     * Returns true if the given String value is not null and the content is not "true".
     * Returns ifNull if the given string is null
     * Explicitly requires a value of "true" to return true
     *
     * @param s String
     * @param ifNull Return value if the String is null
     * @return
     */
    private static boolean isNotSetToTrue(String s, boolean ifNull) {
        if (s == null) {
            return ifNull;
        }
        return !s.equalsIgnoreCase("true");
    }
}
