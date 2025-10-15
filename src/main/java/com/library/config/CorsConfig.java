package com.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 设置允许的源（从配置文件读取）
        config.addAllowedOrigin(allowedOrigins);

        // 允许携带凭证（对应nodejs的credentials: true）
        config.setAllowCredentials(true);

        // 允许的请求方法（对应nodejs的methods配置）
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // 允许的请求头（对应nodejs的allowedHeaders配置）
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Authorization");

        // 暴露的响应头
        config.addExposedHeader("Authorization");

        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);

        // 应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}