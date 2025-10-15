package com.library.controller;

import com.library.model.dto.UserDTO;
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
        // 1. 通过用户名查询用户DTO
        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. 构造响应：直接使用UserDTO数据
        UserProfileResponse response = new UserProfileResponse(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getNickname(),
                userDTO.getEmail(),
                userDTO.getPhone(),
                userDTO.getBio(),
                userDTO.getAvatarUrl(),
                userDTO.getGender(),
                userDTO.getBirthDate(),
                userDTO.getPoints(),
                userDTO.getCreatedAt()
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

        // 1. 通过用户名查询用户
        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. 调用Service更新资料（传入DTO的userId）
        UserDTO updatedUserDTO = userService.updateProfile(userDTO.getId(), request);

        // 3. 直接使用DTO构造响应
        UserProfileResponse response = new UserProfileResponse(
                updatedUserDTO.getId(),
                updatedUserDTO.getUsername(),
                updatedUserDTO.getNickname(),
                updatedUserDTO.getEmail(),
                updatedUserDTO.getPhone(),
                updatedUserDTO.getBio(),
                updatedUserDTO.getAvatarUrl(),
                updatedUserDTO.getGender(),
                updatedUserDTO.getBirthDate(),
                updatedUserDTO.getPoints(),
                updatedUserDTO.getCreatedAt()
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

        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        String avatarUrl = userService.uploadAvatar(userDTO.getId(), file);

        return ResponseEntity.ok(new MessageResponse(true, "头像上传成功", avatarUrl));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @CurrentUser UserDetails currentUser,
            @RequestBody ChangePasswordRequest request) {

        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        userService.changePassword(userDTO.getId(), request.getOldPassword(), request.getNewPassword());

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

        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserOrders(
                userDTO.getId(),
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

        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserWishlist(
                userDTO.getId(),
                page,
                limit
        ));
    }

    /**
     * 获取用户地址列表
     */
    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses(@CurrentUser UserDetails currentUser) {
        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserAddresses(userDTO.getId()));
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@CurrentUser UserDetails currentUser) {
        UserDTO userDTO = userService.getUserProfileByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return ResponseEntity.ok(userService.getUserStats(userDTO.getId()));
    }
}