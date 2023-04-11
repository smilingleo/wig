package me.smilingleo.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UncheckedIOException;

public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String uncheckedJsonPrettify(Object data, ObjectMapper customObjectMapper) {
        try {
            return customObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String uncheckedJsonPrettify(Object data) {
        return uncheckedJsonPrettify(data, objectMapper);
    }

    public static String uncheckedJsonToString(Object data, ObjectMapper customObjectMapper) {
        try {
            return customObjectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String uncheckedJsonToString(Object data) {
        return uncheckedJsonToString(data, objectMapper);
    }

    public static <T> T uncheckedStringToJson(String jsonString, TypeReference<T> typeReference, ObjectMapper customObjectMapper) {
        try {
            return customObjectMapper.readValue(jsonString, typeReference);
        } catch (JacksonException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T uncheckedStringToJson(String jsonString, TypeReference<T> typeReference) {
        return uncheckedStringToJson(jsonString, typeReference, objectMapper);
    }
}
