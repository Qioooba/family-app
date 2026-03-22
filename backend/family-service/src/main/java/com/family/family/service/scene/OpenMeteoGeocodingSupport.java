package com.family.family.service.scene;

import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class OpenMeteoGeocodingSupport {

    private static final Map<String, double[]> BUILTIN_CITY_COORDINATES = new HashMap<>();

    static {
        BUILTIN_CITY_COORDINATES.put("北京", new double[] {39.9042, 116.4074});
        BUILTIN_CITY_COORDINATES.put("上海", new double[] {31.2304, 121.4737});
        BUILTIN_CITY_COORDINATES.put("广州", new double[] {23.1291, 113.2644});
        BUILTIN_CITY_COORDINATES.put("深圳", new double[] {22.5431, 114.0579});
        BUILTIN_CITY_COORDINATES.put("杭州", new double[] {30.2741, 120.1551});
        BUILTIN_CITY_COORDINATES.put("南京", new double[] {32.0603, 118.7969});
        BUILTIN_CITY_COORDINATES.put("苏州", new double[] {31.2989, 120.5853});
        BUILTIN_CITY_COORDINATES.put("无锡", new double[] {31.4900, 120.3124});
        BUILTIN_CITY_COORDINATES.put("常州", new double[] {31.8107, 119.9741});
        BUILTIN_CITY_COORDINATES.put("成都", new double[] {30.5728, 104.0668});
        BUILTIN_CITY_COORDINATES.put("重庆", new double[] {29.5630, 106.5516});
        BUILTIN_CITY_COORDINATES.put("天津", new double[] {39.3434, 117.3616});
        BUILTIN_CITY_COORDINATES.put("武汉", new double[] {30.5928, 114.3055});
        BUILTIN_CITY_COORDINATES.put("西安", new double[] {34.3416, 108.9398});
        BUILTIN_CITY_COORDINATES.put("长沙", new double[] {28.2282, 112.9388});
        BUILTIN_CITY_COORDINATES.put("青岛", new double[] {36.0671, 120.3826});
        BUILTIN_CITY_COORDINATES.put("郑州", new double[] {34.7473, 113.6249});
        BUILTIN_CITY_COORDINATES.put("厦门", new double[] {24.4798, 118.0894});
        BUILTIN_CITY_COORDINATES.put("宁波", new double[] {29.8683, 121.5440});
        BUILTIN_CITY_COORDINATES.put("合肥", new double[] {31.8206, 117.2290});
    }

    private OpenMeteoGeocodingSupport() {
    }

    static double[] resolveCoordinates(RestTemplate restTemplate, String geocodingApi, String cityName) {
        try {
            double[] direct = parseCoordinateString(cityName);
            if (direct != null) {
                return direct;
            }

            String normalized = normalizeCityName(cityName);
            double[] builtin = lookupBuiltinCoordinates(normalized);
            if (builtin != null) {
                return builtin;
            }
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

    static double[] resolveCoordinates(RestTemplate restTemplate, String geocodingApi, String cityName,
                                       Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return new double[] { latitude, longitude };
        }
        return resolveCoordinates(restTemplate, geocodingApi, cityName);
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
        if (normalized.contains("市")) {
            normalized = normalized.substring(0, normalized.indexOf("市"));
        }
        if (normalized.contains("区")) {
            normalized = normalized.substring(0, normalized.indexOf("区"));
        }
        return normalized;
    }

    private static double[] lookupBuiltinCoordinates(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }
        double[] exact = BUILTIN_CITY_COORDINATES.get(cityName);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<String, double[]> entry : BUILTIN_CITY_COORDINATES.entrySet()) {
            if (cityName.contains(entry.getKey()) || entry.getKey().contains(cityName)) {
                return entry.getValue();
            }
        }
        return null;
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
