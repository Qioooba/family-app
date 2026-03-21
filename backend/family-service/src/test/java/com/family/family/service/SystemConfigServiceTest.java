package com.family.family.service;

import com.family.family.entity.SystemConfig;
import com.family.family.mapper.SystemConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SystemConfigServiceTest {

    private final Map<String, SystemConfig> store = new HashMap<>();
    private SystemConfigMapper mapper;
    private SystemConfigService service;

    @BeforeEach
    void setUp() {
        mapper = (SystemConfigMapper) Proxy.newProxyInstance(
            SystemConfigMapper.class.getClassLoader(),
            new Class[]{SystemConfigMapper.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "selectByKey" -> store.get(args[0]);
                case "insert" -> {
                    SystemConfig config = (SystemConfig) args[0];
                    store.put(config.getConfigKey(), config);
                    yield 1;
                }
                case "updateById" -> {
                    SystemConfig config = (SystemConfig) args[0];
                    store.put(config.getConfigKey(), config);
                    yield 1;
                }
                case "selectList" -> store.values().stream().toList();
                default -> null;
            }
        );

        service = new SystemConfigService();
        ReflectionTestUtils.setField(service, "configMapper", mapper);
        store.clear();
    }

    @Test
    void shouldReadDotNotationKeys() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("wechat.work.corpid");
        config.setConfigValue("corp-id");
        store.put(config.getConfigKey(), config);

        assertEquals("corp-id", service.getWechatWorkCorpId());
    }

    @Test
    void shouldFallbackToLegacyUnderscoreKeys() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("wechat_work_secret");
        config.setConfigValue("legacy-secret");
        store.put(config.getConfigKey(), config);

        assertEquals("legacy-secret", service.getWechatWorkSecret());
    }

    @Test
    void shouldNormalizeKeysWhenSaving() {
        service.setValue("wechat_work_userid", "QiJun");

        assertEquals("QiJun", store.get("wechat.work.userid").getConfigValue());
    }

    @Test
    void shouldReturnNullForUnknownConfig() {
        assertNull(service.getValue("unknown.key"));
    }
}
