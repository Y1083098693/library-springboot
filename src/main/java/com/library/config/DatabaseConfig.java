package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.library.repository")
@EnableTransactionManagement
@EnableConfigurationProperties
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(dbDriverClassName);

        // 配置连接池属性，对应nodejs中mysql2的连接池配置
        config.setMaximumPoolSize(10); // 对应connectionLimit
        config.setConnectionTimeout(30000); // 30秒连接超时
        config.setIdleTimeout(600000); // 10分钟空闲超时
        config.setMaxLifetime(1800000); // 30分钟连接最大生命周期

        return new HikariDataSource(config);
    }
}