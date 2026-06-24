package com.wms.common.security;

import com.wms.common.base.ApiResponse;
import com.wms.common.log.OperationLog;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @OperationLog(module = "系统管理", action = "用户登录", desc = "用户登录")
    public ApiResponse<LoginUser> login(@Valid @RequestBody LoginRequest request) {
        LoginUser user = authService.login(request);
        return ApiResponse.ok(user);
    }
}
