package com.library.security;

import com.library.config.JwtConfig;
import io.jsonwebtoken.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    // 注入JwtConfig替代AppConfig
    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * 根据用户ID和用户名生成JWT令牌
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationTime());

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // 存储用户ID
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecretKey()) // 使用JwtConfig中的密钥
                .compact();
    }

    /**
     * 从JWT令牌中获取用户名
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtConfig.getSecretKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 从JWT令牌中获取用户ID
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtConfig.getSecretKey())
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.get("userId").toString());
    }

    /**
     * 验证JWT令牌
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtConfig.getSecretKey()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // 签名无效
        } catch (MalformedJwtException ex) {
            // 令牌格式错误
        } catch (ExpiredJwtException ex) {
            // 令牌过期
        } catch (UnsupportedJwtException ex) {
            // 不支持的令牌
        } catch (IllegalArgumentException ex) {
            // 令牌为空
        }
        return false;
    }
}