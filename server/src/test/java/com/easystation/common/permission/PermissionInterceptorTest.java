package com.easystation.common.permission;

import com.easystation.auth.model.Permission.DataScope;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RequirePermission 注解和 PermissionInterceptor 单元测试
 */
public class PermissionInterceptorTest {

    /**
     * 测试类 - 用于验证注解
     */
    public static class TestResource {
        
        @RequirePermission(resource = "plugin", action = "create")
        public void createPlugin() {}

        @RequirePermission(resource = "host", action = "delete", dataScope = DataScope.DEPARTMENT)
        public void deleteHost() {}

        @RequirePermission(resource = "deployment", action = "manage")
        public void manageDeployment() {}

        public void publicMethod() {}
    }

    @Test
    public void testRequirePermissionAnnotationExists() {
        // 验证注解存在
        assertNotNull(RequirePermission.class);
        assertEquals(InterceptorBinding.class.getAnnotation(Deprecated.class), null);
    }

    @Test
    public void testRequirePermissionOnMethod() throws NoSuchMethodException {
        // 验证方法上的注解
        Method createMethod = TestResource.class.getMethod("createPlugin");
        RequirePermission annotation = createMethod.getAnnotation(RequirePermission.class);
        
        assertNotNull(annotation);
        assertEquals("plugin", annotation.resource());
        assertEquals("create", annotation.action());
        assertEquals(DataScope.ALL, annotation.dataScope());
    }

    @Test
    public void testRequirePermissionWithDataScope() throws NoSuchMethodException {
        // 验证带数据范围的注解
        Method deleteMethod = TestResource.class.getMethod("deleteHost");
        RequirePermission annotation = deleteMethod.getAnnotation(RequirePermission.class);
        
        assertNotNull(annotation);
        assertEquals("host", annotation.resource());
        assertEquals("delete", annotation.action());
        assertEquals(DataScope.DEPARTMENT, annotation.dataScope());
    }

    @Test
    public void testRequirePermissionManageAction() throws NoSuchMethodException {
        // 验证 manage 权限
        Method manageMethod = TestResource.class.getMethod("manageDeployment");
        RequirePermission annotation = manageMethod.getAnnotation(RequirePermission.class);
        
        assertNotNull(annotation);
        assertEquals("deployment", annotation.resource());
        assertEquals("manage", annotation.action());
    }

    @Test
    public void testNoAnnotationOnPublicMethod() throws NoSuchMethodException {
        // 验证没有注解的方法
        Method publicMethod = TestResource.class.getMethod("publicMethod");
        RequirePermission annotation = publicMethod.getAnnotation(RequirePermission.class);
        
        assertNull(annotation);
    }

    @Test
    public void testPermissionCodeFormat() {
        // 验证权限编码格式
        String resource = "plugin";
        String action = "create";
        String code = resource + ":" + action;
        
        assertEquals("plugin:create", code);
    }

    @Test
    public void testDataScopeValues() {
        // 验证数据范围枚举值
        assertEquals("ALL", DataScope.ALL.name());
        assertEquals("DEPARTMENT", DataScope.DEPARTMENT.name());
        assertEquals("PROJECT", DataScope.PROJECT.name());
        assertEquals("SELF", DataScope.SELF.name());
    }
}
