package com.library.service;

import com.library.model.dto.UserDTO;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String AVATAR_UPLOAD_PATH = "uploads/avatars/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 根据用户名获取用户DTO
     */
    public Optional<UserDTO> getUserProfileByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    /**
     * 更新用户个人资料
     */
    @Transactional
    public UserDTO updateProfile(Long userId, UpdateProfileRequest request) {
        // 1. 通过userId查询用户
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 2. 逐项更新字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
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
        if (request.getBirthDate() != null) {
            try {
                user.setBirthDate(LocalDateTime.parse(
                        request.getBirthDate() + " 00:00:00",
                        DATETIME_FORMAT
                ));
            } catch (Exception e) {
                throw new BadRequestException("生日格式错误，需为yyyy-MM-dd");
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        var updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * 上传用户头像
     */
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("请上传图片类型文件");
        }

        // 保存文件
        File uploadDir = new File(AVATAR_UPLOAD_PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = userId + "_" + System.currentTimeMillis() + fileExt;
        String filePath = AVATAR_UPLOAD_PATH + fileName;
        file.transferTo(new File(filePath));

        // 更新头像URL
        String avatarUrl = "/" + filePath;
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("旧密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // 其他方法保持不变...
    public Map<String, Object> getUserOrders(Long userId, String status, int page, int limit) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        int offset = (page - 1) * limit;
        List<Object[]> orders = userRepository.getOrders(userId, status, limit, offset);
        long total = userRepository.countOrders(userId, status);

        return Map.of(
                "list", orders,
                "total", total,
                "page", page,
                "limit", limit,
                "pages", (total + limit - 1) / limit
        );
    }

    public Map<String, Object> getUserWishlist(Long userId, int page, int limit) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        int offset = (page - 1) * limit;
        List<Object[]> wishlist = userRepository.getFavorites(userId, limit, offset);
        long total = userRepository.countFavorites(userId);

        return Map.of(
                "list", wishlist,
                "total", total,
                "page", page,
                "limit", limit,
                "pages", (total + limit - 1) / limit
        );
    }

    public List<?> getUserAddresses(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return List.of();
    }

    public UserStatsVO getUserStats(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return userRepository.getStats(userId);
    }

    /**
     * 转换User实体到UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender());

        // 格式化日期为字符串
        if (user.getBirthDate() != null) {
            dto.setBirthDate(user.getBirthDate().format(DATE_FORMAT));
        }
        if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt().format(DATETIME_FORMAT));
        }

        dto.setPoints(user.getPoints());
        return dto;
    }
}