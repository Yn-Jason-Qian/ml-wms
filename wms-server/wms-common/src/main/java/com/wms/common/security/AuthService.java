package com.wms.common.security;

import com.wms.common.exception.BusinessException;
import com.wms.common.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginUser login(LoginRequest request) {
        String sql =
                """
            SELECT u.id, u.username, u.password, u.real_name, u.tenant_id, u.status,
                   t.tenant_code, t.tenant_name
            FROM wms_sys_user u
            LEFT JOIN wms_sys_tenant t ON t.id = u.tenant_id
            WHERE u.username = ? AND u.is_deleted = 0
            """;

        Map<String, Object> row;
        try {
            row = jdbcTemplate.queryForMap(sql, request.getUsername());
        } catch (Exception e) {
            log.warn("Login failed for '{}': {}", request.getUsername(), e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }

        // 兼容 MySQL JDBC tinyInt1isBit: TINYINT(1) 可能返回 Boolean 或 Integer
        int status =
                row.get("status") instanceof Boolean
                        ? (((Boolean) row.get("status")) ? 1 : 0)
                        : ((Number) row.get("status")).intValue();
        if (status == 0) {
            throw new BusinessException("用户已被禁用");
        }

        String encodedPwd = (String) row.get("password");
        if (!passwordEncoder.matches(request.getPassword(), encodedPwd)) {
            throw new BusinessException("用户名或密码错误");
        }

        Long userId = ((Number) row.get("id")).longValue();
        Long tenantId =
                row.get("tenant_id") != null ? ((Number) row.get("tenant_id")).longValue() : 0L;
        String username = (String) row.get("username");
        String realName = (String) row.get("real_name");
        String tenantName = (String) row.get("tenant_name");

        String token = jwtUtil.generate(userId, username, tenantId);

        log.info("User {} logged in", username);

        return new LoginUser(userId, username, realName, tenantId, tenantName, token);
    }
}
