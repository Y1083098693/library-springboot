package com.library.service;

import com.library.model.dto.request.LoginRequest;
import com.library.model.dto.request.RegisterRequest;
import com.library.model.dto.response.JwtResponse;
import com.library.model.dto.response.MessageResponse;
import com.library.model.entity.User;
import com.library.repository.UserRepository;
import com.library.security.JwtTokenProvider;
import com.library.exception.EntityNotFoundException;
import com.library.exception.EntityExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 构造函数注入依赖
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 用户登录验证并生成JWT令牌
     */
    public JwtResponse login(LoginRequest loginRequest) {
        // 查询用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new EntityNotFoundException("用户名或密码错误");
        }

        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        // 返回登录响应
        return new JwtResponse(token, user);
    }

    /**
     * 用户注册
     */
    @Transactional
    public MessageResponse register(RegisterRequest registerRequest) {
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

        // 保存用户
        userRepository.save(newUser);

        return MessageResponse.success("注册成功，请登录");
    }
}