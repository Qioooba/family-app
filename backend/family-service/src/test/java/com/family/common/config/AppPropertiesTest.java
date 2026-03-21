package com.family.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppPropertiesTest {

    @Test
    void shouldTrimTrailingSlashForUrls() {
        AppProperties properties = new AppProperties();
        properties.setBaseUrl("https://example.com/");
        properties.setPublicOrigin("https://origin.example.com/");

        assertEquals("https://example.com", properties.getBaseUrl());
        assertEquals("https://origin.example.com", properties.getPublicOrigin());
    }
}
