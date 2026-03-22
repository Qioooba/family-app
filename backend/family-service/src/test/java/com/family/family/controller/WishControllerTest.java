package com.family.family.controller;

import com.family.family.entity.Wish;
import com.family.family.mapper.UserMapper;
import com.family.family.mapper.WishMapper;
import com.family.family.support.SaTokenTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WishControllerTest {

    private WishController controller;
    private Wish wish;

    @BeforeEach
    void setUp() {
        controller = new WishController();
        wish = new Wish();
        wish.setId(1L);
        wish.setUserId(7L);
        wish.setFamilyId(1L);
        wish.setStatus(0);
        wish.setProgress(0);
        wish.setCreateTime(LocalDateTime.now());
        wish.setUpdateTime(LocalDateTime.now());

        WishMapper wishMapper = (WishMapper) java.lang.reflect.Proxy.newProxyInstance(
            WishMapper.class.getClassLoader(),
            new Class[]{WishMapper.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "selectById" -> wish;
                case "updateById" -> {
                    Wish updated = (Wish) args[0];
                    wish = updated;
                    yield 1;
                }
                default -> null;
            }
        );
        UserMapper userMapper = (UserMapper) java.lang.reflect.Proxy.newProxyInstance(
            UserMapper.class.getClassLoader(),
            new Class[]{UserMapper.class},
            (proxy, method, args) -> null
        );

        ReflectionTestUtils.setField(controller, "wishMapper", wishMapper);
        ReflectionTestUtils.setField(controller, "userMapper", userMapper);
    }

    @AfterEach
    void tearDown() {
        SaTokenTestHelper.clear();
    }

    @Test
    void creatorCanClaimOwnWish() {
        SaTokenTestHelper.login(7L);

        Map<String, Object> response = controller.claim(1L);

        assertEquals(200, response.get("code"));
        assertEquals(7L, wish.getClaimantId());
        assertEquals(1, wish.getStatus());
    }

    @Test
    void nonCreatorCannotCompleteWish() {
        wish.setClaimantId(8L);
        wish.setStatus(1);
        SaTokenTestHelper.login(8L);

        Map<String, Object> response = controller.complete(1L);

        assertEquals(403, response.get("code"));
        assertEquals("无权限完成该心愿", response.get("message"));
    }
}
