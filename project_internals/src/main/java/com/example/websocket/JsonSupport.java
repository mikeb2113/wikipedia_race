package com.example.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSupport {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T decode(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + json, e);
        }
    }

    public static String encode(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON for: " + obj, e);
        }
    }
}