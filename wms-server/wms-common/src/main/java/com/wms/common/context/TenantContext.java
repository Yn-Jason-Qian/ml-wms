package com.wms.common.context;

/**
 * 租户上下文（ThreadLocal），由 JWT 过滤器解析后设置，MyBatis-Plus 拦截器自动注入 SQL。
 */
public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Long tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    public static Long get() {
        return TENANT_HOLDER.get();
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
