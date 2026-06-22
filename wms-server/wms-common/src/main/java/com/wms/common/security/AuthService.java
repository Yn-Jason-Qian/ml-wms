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
        // 查询用户
String sql = """
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
            throw new BusinessException("用户名或密码错误");
        }

        if ((int) row.get("status") == 0) {
            throw new BusinessException("用户已被禁用");
        }

String encodedPwd = (String) row.get("password");
        if (!passwordEncoder.matches(request.getPassword(), encodedPwd)) {
            throw new BusinessException("用户名或密码错误");
        }

Long userId = (Long) row.get("id");
Long tenantId = (Long) row.get("tenant_id");
String username = (String) row.get("username");
String realName = (String) row.get("real_name");
String tenantName = (String) row.get("tenant_name");

        // 生成 JWT
String token = jwtUtil.generate(userId, username, tenantId);

        log.info("User {} logged in", username);

        return new LoginUser(userId, username, realName, tenantId, tenantName, token);
    }
}
