package com.aisa.pw.libs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/*
* Centralized configuration for configurable values.
* */

public class ConfigManager {
    private static JsonNode config;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readTree(new File("src/main/resources/config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getBaseUrl() {
        return config.get("baseUrl").asText();
    }

    public static boolean isHeadless() {
        return config.get("headless").asBoolean();
    }
}
