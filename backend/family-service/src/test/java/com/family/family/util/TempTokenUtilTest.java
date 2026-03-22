package com.family.family.util;

import com.family.common.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TempTokenUtilTest {

    @Test
    void generatedTokenShouldBeReusableUntilExpired() {
        TempTokenUtil util = new TempTokenUtil();
        AppProperties properties = new AppProperties();
        properties.setBaseUrl("https://example.com");
        ReflectionTestUtils.setField(util, "appProperties", properties);
        ReflectionTestUtils.setField(util, "jwtSecretKey", "test-secret-key-for-temp-token");

        String token = util.generateTempToken(7L);

        assertNotNull(token);
        assertEquals(7L, util.validateTempToken(token));
        assertEquals(7L, util.validateTempToken(token));
        String autoLoginUrl = util.getAutoLoginUrl(7L);
        org.junit.jupiter.api.Assertions.assertTrue(autoLoginUrl.startsWith("https://example.com/auto-login.html?token="));
    }
}
