package com.family.family.service.scene;

import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

final class OpenMeteoGeocodingSupport {

    private OpenMeteoGeocodingSupport() {
    }

    static double[] resolveCoordinates(RestTemplate restTemplate, String geocodingApi, String cityName) {
        try {
            double[] direct = parseCoordinateString(cityName);
            if (direct != null) {
                return direct;
            }

            String normalized = normalizeCityName(cityName);
            for (String keyword : buildSearchKeywords(normalized)) {
                double[] coords = queryCoordinates(restTemplate, geocodingApi, keyword);
                if (coords != null) {
                    return coords;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static double[] queryCoordinates(RestTemplate restTemplate, String geocodingApi, String keyword) {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String[] urls = new String[] {
            String.format("%s?name=%s&count=1&language=zh&format=json", geocodingApi, encoded),
            String.format("%s?name=%s&count=1&format=json", geocodingApi, encoded)
        };

        for (String url : urls) {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                continue;
            }
            Map<String, Object> first = results.get(0);
            Object lat = first.get("latitude");
            Object lon = first.get("longitude");
            if (lat instanceof Number latNum && lon instanceof Number lonNum) {
                return new double[] { latNum.doubleValue(), lonNum.doubleValue() };
            }
        }
        return null;
    }

    private static String[] buildSearchKeywords(String cityName) {
        return new String[] {
            cityName,
            cityName + ", China",
            cityName + " China"
        };
    }

    private static String normalizeCityName(String cityName) {
        if (cityName == null) {
            return "";
        }
        String normalized = cityName.trim();
        normalized = normalized.replace("中国", "")
            .replace("江苏省", "")
            .replace("浙江省", "")
            .replace("安徽省", "")
            .replace("上海市", "上海")
            .replace("北京市", "北京")
            .replace("重庆市", "重庆")
            .replace("天津市", "天津");
        normalized = normalized.replaceAll("[省市区县]$", "").trim();
        if (normalized.contains(" ")) {
            normalized = normalized.split("\\s+")[0];
        }
        return normalized;
    }

    private static double[] parseCoordinateString(String cityName) {
        if (cityName == null || !cityName.matches(".*\\d+.*")) {
            return null;
        }
        String[] parts = cityName.replaceAll("[^0-9.,-]", "").split(",");
        if (parts.length < 2) {
            return null;
        }
        try {
            return new double[] {
                Double.parseDouble(parts[0].trim()),
                Double.parseDouble(parts[1].trim())
            };
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
