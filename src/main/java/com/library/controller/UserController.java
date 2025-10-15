package com.library.controller;

import com.library.model.entity.User;
import com.library.model.dto.request.ChangePasswordRequest;
import com.library.model.dto.request.UpdateProfileRequest;
import com.library.model.dto.response.MessageResponse;
import com.library.model.dto.response.UserProfileResponse;
import com.library.repository.UserRepository;
import com.library.security.CurrentUser;
import com.library.service.UserService;
import com.library.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    // 统一日期格式化器（前后端一致）
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * 获取用户个人信息
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@CurrentUser UserDetails currentUser) {
        // 1. 通过用户名查询用户（获取完整User对象）
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. LocalDateTime转String（处理null值，避免空指针）
        String birthDateStr = (user.getBirthDate() != null)
                ? user.getBirthDate().format(DATE_FORMAT)
                : null;
        String createdAtStr = (user.getCreatedAt() != null)
                ? user.getCreatedAt().format(DATETIME_FORMAT)
                : null;

        // 3. 构造响应：严格匹配UserProfileResponse的参数顺序
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getGender(),
                birthDateStr,
                user.getPoints(),
                createdAtStr
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户个人资料
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @CurrentUser UserDetails currentUser,
            @RequestBody UpdateProfileRequest request) {

        // 1. 通过用户名查询userId（转为Long类型传给Service）
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        Long userId = user.getId();

        // 2. 调用Service更新资料（传入Long类型userId）
        User updatedUser = userService.updateProfile(userId, request);

        // 3. 格式化日期，构造响应
        String birthDateStr = (updatedUser.getBirthDate() != null)
                ? updatedUser.getBirthDate().format(DATE_FORMAT)
                : null;
        String createdAtStr = (updatedUser.getCreatedAt() != null)
                ? updatedUser.getCreatedAt().format(DATETIME_FORMAT)
                : null;

        UserProfileResponse response = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getNickname(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getGender(),
                birthDateStr,
                updatedUser.getPoints(),
                createdAtStr
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<MessageResponse> uploadAvatar(
            @CurrentUser UserDetails currentUser,
            @RequestParam("avatar") MultipartFile file) throws IOException {

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        String avatarUrl = userService.uploadAvatar(user.getId(), file);

        // 修复：第一个参数传true（成功），第二个传消息，第三个传头像URL（数据）
        return ResponseEntity.ok(new MessageResponse(true, "头像上传成功", avatarUrl));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @CurrentUser UserDetails currentUser,
            @RequestBody ChangePasswordRequest request) {

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        // 修复：第一个参数传true（成功），第二个传消息（无数据，用2参数构造函数）
        return ResponseEntity.ok(new MessageResponse(true, "密码修改成功"));
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @CurrentUser UserDetails currentUser,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserOrders(
                user.getId(), // 传userId而非username
                status,
                page,
                limit
        ));
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/wishlist")
    public ResponseEntity<?> getWishlist(
            @CurrentUser UserDetails currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserWishlist(
                user.getId(), // 传userId而非username
                page,
                limit
        ));
    }

    /**
     * 获取用户地址列表
     */
    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses(@CurrentUser UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserAddresses(user.getId())); // 传userId
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@CurrentUser UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserStats(user.getId())); // 传userId
    }
}