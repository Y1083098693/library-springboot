package com.library.service;

import com.library.model.dto.request.LoginRequest;
import com.library.model.dto.request.RegisterRequest;
import com.library.model.dto.response.ApiResponse;
import com.library.model.dto.response.JwtResponse;
import com.library.model.entity.User;
import com.library.repository.UserRepository;
import com.library.security.JwtTokenProvider;
import com.library.exception.EntityNotFoundException;
import com.library.exception.EntityExistsException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录验证并生成JWT令牌
     */
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用户名或密码错误",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))})
    })
    public JwtResponse login(LoginRequest loginRequest) {
        // 查询用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new EntityNotFoundException("用户名或密码错误");
        }

        // 生成JWT令牌和刷新令牌
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // 使用新构造器返回响应
        return new JwtResponse(token, refreshToken, user.getId(), user.getUsername());
    }

    /**
     * 用户注册
     */
    @Transactional
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "注册成功",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名或邮箱已存在",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))})
    })
    public ApiResponse<Void> register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new EntityExistsException("用户名已被占用");
        }

        // 检查邮箱是否已存在
        if (registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EntityExistsException("邮箱已被注册");
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPoints(100); // 默认初始积分
        newUser.setRole("USER"); // 默认用户角色（注意：User实体需要有role字段）

        // 保存用户
        userRepository.save(newUser);

        return new ApiResponse<>(true, "注册成功，请登录", null);
    }

    /**
     * 刷新令牌
     */
    public JwtResponse refreshToken(String refreshToken) {
        // 验证刷新令牌有效性
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new EntityNotFoundException("无效的刷新令牌");
        }

        // 修复方法名调用：使用getUserIdFromToken和getUsernameFromToken
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = jwtTokenProvider.generateToken(userId, username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username);

        // 使用新构造器返回响应
        return new JwtResponse(newAccessToken, newRefreshToken, userId, username);
    }

    /**
     * 用户登出（使令牌失效）
     */
    public ApiResponse<Void> logout(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new EntityNotFoundException("无效的令牌");
        }
        return new ApiResponse<>(true, "登出成功", null);
    }
}