package com.github.khakers.modmailviewer.util;

import io.javalin.http.Context;
import org.owasp.encoder.Encode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryParamUtil {
    /**
     * Updates the current request URL with the given parameters, overriding them if they currently exist
     *
     * @param ctx Javalin request context
     * @param key Query parameter key
     * @param value Query Parameter value
     * @return URL with the query parameters updated.
     */
    public static String updateQueryParams(Context ctx, String key, String value) {
        return updateQueryParams(ctx, Map.of(key, List.of(value)));
    }

    /**
     * @param ctx Javalin request context
     * @param params A map of new params to add to the URL
     * @return URL with the query parameters updated.
     */
    public static String updateQueryParams(Context ctx, Map<String, List<String>> params) {
        //Using linked hashmap since it preserves order
        Map<String, List<String>> paramMap = new LinkedHashMap<>(ctx.queryParamMap());
        paramMap.putAll(params);
        StringBuilder stringBuilder = new StringBuilder("?");
        paramMap.forEach((s, strings) -> strings.forEach(s1 -> {
            if (stringBuilder.length() != 1) {
                stringBuilder.append("&");
            }

            stringBuilder
                    .append(Encode.forUriComponent(s))
                    .append("=")
                    .append(Encode.forUriComponent(s1));

        }));

        return ctx.url() + stringBuilder;
    }
}
