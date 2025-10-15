package com.library.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Size(max = 10, message = "性别长度不能超过10个字符")
    private String gender;

    private String  birthDate;

    private String bio;

    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatarUrl;
}