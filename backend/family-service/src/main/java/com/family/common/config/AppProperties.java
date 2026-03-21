package com.family.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用公共配置。
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl = "http://localhost:8443";
    private String publicOrigin = "http://localhost:8443";
    private List<String> corsAllowedOrigins = new ArrayList<>();

    public String getBaseUrl() {
        return trimTrailingSlash(baseUrl);
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPublicOrigin() {
        return trimTrailingSlash(publicOrigin);
    }

    public void setPublicOrigin(String publicOrigin) {
        this.publicOrigin = publicOrigin;
    }

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
