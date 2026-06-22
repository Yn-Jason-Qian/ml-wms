package com.wms.common.security;

import com.wms.common.context.TenantContext;
import com.wms.common.context.UserContext;
import com.wms.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
String token = extractToken(request);

        if (StringUtils.hasText(token) && !jwtUtil.isExpired(token)) {
            try {
                Claims claims = jwtUtil.parse(token);
Long userId = Long.parseLong(claims.getSubject());
String username = claims.get("username", String.class);
Long tenantId = claims.get("tenantId", Long.class);

                // 设置上下文
                TenantContext.set(tenantId);
                UserContext.set(new UserContext.UserInfo(userId, username, tenantId));

                // 设置 Spring Security 认证信息
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.debug("JWT parse failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);

        // 清理 ThreadLocal，防止内存泄漏
        TenantContext.clear();
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
