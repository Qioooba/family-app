package com.family.family.service.scene;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OpenMeteoGeocodingSupportTest {

    @Test
    void shouldResolveBuiltinChineseCityCoordinatesWithoutApiCall() {
        double[] coords = OpenMeteoGeocodingSupport.resolveCoordinates(new RestTemplate(), "https://example.com", "南京");

        assertArrayEquals(new double[] {32.0603, 118.7969}, coords, 0.0001);
    }

    @Test
    void shouldParseDirectCoordinateString() {
        double[] coords = OpenMeteoGeocodingSupport.resolveCoordinates(new RestTemplate(), "https://example.com", "32.06,118.79");

        assertArrayEquals(new double[] {32.06, 118.79}, coords, 0.0001);
    }

    @Test
    void shouldReturnNullForUnknownCityWhenApiReturnsNothing() {
        RestTemplate restTemplate = new RestTemplate() {
            @Override
            public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
                return null;
            }
        };

        double[] coords = OpenMeteoGeocodingSupport.resolveCoordinates(restTemplate, "https://example.com", "不存在的城市");

        assertNull(coords);
    }
}
