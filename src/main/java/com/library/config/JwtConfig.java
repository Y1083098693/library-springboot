package com.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime; // 访问令牌过期时间（毫秒）

    @Value("${jwt.refresh-expiration}") // 需要在配置文件中添加此配置
    private long refreshExpirationTime; // 刷新令牌过期时间（毫秒）

    public String getSecretKey() {
        return secretKey;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    // 新增getter方法
    public long getRefreshExpirationTime() {
        return refreshExpirationTime;
    }
}