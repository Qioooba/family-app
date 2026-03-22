package com.family.family.support;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.SaTokenContextForThreadLocalStorage;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.stp.StpUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sa-Token 单元测试辅助工具。
 */
public final class SaTokenTestHelper {

    private SaTokenTestHelper() {
    }

    public static void login(long userId) {
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());
        SaTokenContextForThreadLocalStorage.setBox(
            new SimpleRequest(),
            new SimpleResponse(),
            new SimpleStorage()
        );
        StpUtil.login(userId);
    }

    public static void clear() {
        try {
            StpUtil.logout();
        } catch (Exception ignored) {
        }
        SaTokenContextForThreadLocalStorage.clearBox();
    }

    private static class SimpleStorage implements SaStorage {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Object getSource() {
            return this;
        }

        @Override
        public Object get(String key) {
            return values.get(key);
        }

        @Override
        public SaStorage set(String key, Object value) {
            values.put(key, value);
            return this;
        }

        @Override
        public SaStorage delete(String key) {
            values.remove(key);
            return this;
        }
    }

    private static class SimpleRequest implements SaRequest {
        @Override
        public Object getSource() {
            return this;
        }

        @Override
        public String getParam(String s) {
            return null;
        }

        @Override
        public List<String> getParamNames() {
            return Collections.emptyList();
        }

        @Override
        public Map<String, String> getParamMap() {
            return Collections.emptyMap();
        }

        @Override
        public String getHeader(String s) {
            return null;
        }

        @Override
        public String getCookieValue(String s) {
            return null;
        }

        @Override
        public String getRequestPath() {
            return "/";
        }

        @Override
        public String getUrl() {
            return "http://localhost/test";
        }

        @Override
        public String getMethod() {
            return "GET";
        }

        @Override
        public Object forward(String s) {
            return null;
        }
    }

    private static class SimpleResponse implements SaResponse {
        @Override
        public Object getSource() {
            return this;
        }

        @Override
        public SaResponse setStatus(int i) {
            return this;
        }

        @Override
        public SaResponse setHeader(String s, String s1) {
            return this;
        }

        @Override
        public SaResponse addHeader(String s, String s1) {
            return this;
        }

        @Override
        public Object redirect(String s) {
            return null;
        }
    }
}
