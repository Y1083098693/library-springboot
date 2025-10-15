package com.library.service;

import com.library.model.entity.User;
import com.library.model.dto.request.UpdateProfileRequest;
import com.library.model.vo.UserStatsVO;
import com.library.repository.UserRepository;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // 头像上传路径（可配置在application.properties中，此处示例）
    private static final String AVATAR_UPLOAD_PATH = "uploads/avatars/";

    /**
     * 更新用户个人资料
     * @param userId 用户ID（Long类型，避免String类型不匹配）
     * @param request 更新请求DTO
     */
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        // 1. 通过userId查询用户（比username更高效，且类型统一）
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. 逐项更新字段（只更新非null的字段）
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            // 可选：校验邮箱唯一性
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("邮箱已被占用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        // 3. 生日字符串转LocalDateTime（前端传入格式：yyyy-MM-dd）
        if (request.getBirthDate() != null) {
            try {
                LocalDateTime birthDate = LocalDateTime.parse(
                        request.getBirthDate() + " 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                user.setBirthDate(birthDate);
            } catch (Exception e) {
                throw new BadRequestException("生日格式错误，需为yyyy-MM-dd");
            }
        }

        // 4. 更新时间戳
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param file 头像文件
     */
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        // 1. 校验用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. 校验文件类型（示例：只允许图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("请上传图片类型文件");
        }

        // 3. 创建上传目录（若不存在）
        File uploadDir = new File(AVATAR_UPLOAD_PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 4. 生成唯一文件名（避免覆盖）
        String originalFilename = file.getOriginalFilename();
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = userId + "_" + System.currentTimeMillis() + fileExt;
        String filePath = AVATAR_UPLOAD_PATH + fileName;

        // 5. 保存文件
        file.transferTo(new File(filePath));

        // 6. 更新用户头像URL
        String avatarUrl = "/" + filePath; // 前端访问路径
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 校验用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. 校验旧密码正确性
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("旧密码错误");
        }

        // 3. 加密新密码并更新
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedNewPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 获取用户订单列表（示例：实际需结合OrderRepository）
     */
    public Map<String, Object> getUserOrders(Long userId, String status, int page, int limit) {
        // 1. 校验用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. 计算分页偏移量（page从1开始）
        int offset = (page - 1) * limit;

        // 3. 调用UserRepository查询订单（实际需OrderRepository，此处复用UserRepository示例方法）
        List<Object[]> orders = userRepository.getOrders(userId, status, limit, offset);
        long total = userRepository.countOrders(userId, status);

        // 4. 封装分页结果（可转为OrderDTO列表）
        return Map.of(
                "list", orders, // 实际需转为DTO
                "total", total,
                "page", page,
                "limit", limit,
                "pages", (total + limit - 1) / limit
        );
    }

    /**
     * 获取用户收藏列表（示例）
     */
    public Map<String, Object> getUserWishlist(Long userId, int page, int limit) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        int offset = (page - 1) * limit;
        List<Object[]> wishlist = userRepository.getFavorites(userId, limit, offset);
        long total = userRepository.countFavorites(userId);

        return Map.of(
                "list", wishlist, // 实际需转为DTO
                "total", total,
                "page", page,
                "limit", limit,
                "pages", (total + limit - 1) / limit
        );
    }

    /**
     * 获取用户地址列表（调用UserAddressService，避免耦合）
     */
    public List<?> getUserAddresses(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 实际需注入UserAddressService，此处示例
        // return userAddressService.getUserAddresses(userId);
        return List.of(); // 占位，需替换为实际逻辑
    }

    /**
     * 获取用户统计数据
     */
    public UserStatsVO getUserStats(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return userRepository.getStats(userId);
    }
}