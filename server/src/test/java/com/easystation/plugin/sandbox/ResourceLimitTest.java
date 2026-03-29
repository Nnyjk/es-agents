package com.easystation.plugin.sandbox.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ResourceLimitTest {
    
    @Test
    @DisplayName("测试默认资源限制")
    void testDefaults() {
        ResourceLimit limit = ResourceLimit.defaults();
        assertNotNull(limit);
        assertTrue(limit.maxMemory() > 0);
        assertTrue(limit.maxExecutionTime() > 0);
    }
    
    @Test
    @DisplayName("测试宽松资源限制")
    void testRelaxed() {
        ResourceLimit relaxed = ResourceLimit.relaxed();
        ResourceLimit defaults = ResourceLimit.defaults();
        assertTrue(relaxed.maxMemory() >= defaults.maxMemory());
    }
    
    @Test
    @DisplayName("测试严格资源限制")
    void testStrict() {
        ResourceLimit strict = ResourceLimit.strict();
        ResourceLimit defaults = ResourceLimit.defaults();
        assertTrue(strict.maxMemory() <= defaults.maxMemory());
    }
    
    @Test
    @DisplayName("测试自定义资源限制")
    void testCustomLimit() {
        ResourceLimit limit = new ResourceLimit(512 * 1024 * 1024L, 10, 60, 200, 100);
        assertEquals(512 * 1024 * 1024L, limit.maxMemory());
        assertEquals(10, limit.maxCpuTime());
        assertEquals(60, limit.maxExecutionTime());
    }
}
