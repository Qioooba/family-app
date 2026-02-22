package com.family.common.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 结果封装类单元测试
 */
public class ResultTest {

    @Test
    @DisplayName("测试成功结果创建")
    void testSuccess() {
        Result<String> result = Result.success("test data");
        
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test data", result.getData());
    }

    @Test
    @DisplayName("测试失败结果创建")
    void testError() {
        Result<Void> result = Result.error("操作失败");
        
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试带错误码的失败结果")
    void testErrorWithCode() {
        Result<Void> result = Result.error(404, "资源不存在");
        
        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMessage());
    }

    @Test
    @DisplayName("测试空数据成功结果")
    void testSuccessWithNullData() {
        Result<Object> result = Result.success();
        
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试时间戳自动设置")
    void testTimestampAutoSet() {
        Result<Object> result = Result.success();
        
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp() > 0);
    }
}
