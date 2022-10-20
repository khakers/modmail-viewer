package com.github.khakers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class JacksonJavalinJsonMapper implements JsonMapper {

    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) { // basic method for mapping from json
        try {
            return objectMapper.readValue(json, (JavaType) targetType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

