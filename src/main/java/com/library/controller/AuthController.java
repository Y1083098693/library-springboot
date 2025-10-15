package com.library.controller;

import com.library.model.dto.request.LoginRequest;
import com.library.model.dto.request.RegisterRequest;
import com.library.model.dto.response.ApiResponse;
import com.library.model.dto.response.JwtResponse;
import com.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录与注册接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码获取JWT令牌")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "登录成功", jwtResponse));
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号，返回注册结果")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "注册成功", null));
    }

    /**
     * 刷新令牌接口
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用过期令牌获取新的JWT令牌")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestBody String refreshToken) {
        JwtResponse newToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>(true, "令牌刷新成功", newToken));
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "使当前JWT令牌失效")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody String token) {
        authService.logout(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "登出成功", null));
    }
}