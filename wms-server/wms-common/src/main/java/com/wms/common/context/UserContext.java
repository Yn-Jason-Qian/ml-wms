package com.wms.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 用户上下文（ThreadLocal），存储当前请求的用户信息。 */
public final class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(UserInfo user) {
        USER_HOLDER.set(user);
    }

    public static UserInfo get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo u = USER_HOLDER.get();
        return u != null ? u.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo u = USER_HOLDER.get();
        return u != null ? u.getUsername() : null;
    }

    public static Long getTenantId() {
        UserInfo u = USER_HOLDER.get();
        return u != null ? u.getTenantId() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private Long tenantId;
    }
}
