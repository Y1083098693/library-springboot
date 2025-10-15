package com.library.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

/**
 * 自定义注解，用于简化获取当前登录用户信息
 * 替代直接使用 @AuthenticationPrincipal，增强代码可读性
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal // 集成Spring Security的认证注解
public @interface CurrentUser {
}