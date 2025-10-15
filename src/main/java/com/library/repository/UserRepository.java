package com.library.repository;

import com.library.model.entity.User;
import com.library.model.entity.UserAddress;
import com.library.model.vo.UserProfileVO;
import com.library.model.vo.UserStatsVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 根据ID查询用户（基础信息）- 对应原findById
    @Query("SELECT new User(u.id, u.username, u.nickname, u.email, u.phone, u.avatarUrl, u.points) " +
            "FROM User u WHERE u.id = :userId")
    Optional<User> findById(@Param("userId") Long userId);

    // 获取用户订单列表 - 严格对齐原getOrders SQL
    @Query(value = "SELECT * FROM orders WHERE user_id = :userId " +
            "AND (:status != 'all' AND status = :status OR :status = 'all') " +
            "ORDER BY created_at DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Object[]> getOrders(@Param("userId") Long userId,
                             @Param("status") String status,
                             @Param("limit") int limit,
                             @Param("offset") int offset);

    // 获取订单总数 - 对应原getOrders中的count查询
    @Query(value = "SELECT COUNT(*) as total FROM orders WHERE user_id = :userId " +
            "AND (:status != 'all' AND status = :status OR :status = 'all')",
            nativeQuery = true)
    long countOrders(@Param("userId") Long userId, @Param("status") String status);

    // 获取用户收藏列表 - 对应原getFavorites
    @Query(value = "SELECT b.* FROM books b " +
            "JOIN favorites f ON b.id = f.book_id " +
            "WHERE f.user_id = :userId " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Object[]> getFavorites(@Param("userId") Long userId,
                                @Param("limit") int limit,
                                @Param("offset") int offset);

    // 获取收藏总数 - 对应原getFavorites中的count查询
    @Query(value = "SELECT COUNT(*) as total FROM favorites WHERE user_id = :userId",
            nativeQuery = true)
    long countFavorites(@Param("userId") Long userId);

    // 获取用户个人资料 - 对应原getUserProfile
    @Query(value = "SELECT id, username, nickname, email, phone, bio, " +
            "avatar_url, gender, birth_date, points, created_at " +
            "FROM users WHERE id = :userId",
            nativeQuery = true)
    Optional<UserProfileVO> getUserProfile(@Param("userId") Long userId);

    // 根据用户名查询用户（带密码）- 对应原findByUsername
    @Query(value = "SELECT id, username, password_hash FROM users WHERE username = :username",
            nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);

    // 创建用户 - 对应原createUser
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO users (username, password_hash, email, phone, created_at) " +
            "VALUES (:username, :passwordHash, :email, :phone, NOW())",
            nativeQuery = true)
    void createUser(@Param("username") String username,
                    @Param("passwordHash") String passwordHash,
                    @Param("email") String email,
                    @Param("phone") String phone);

    // 更新用户资料 - 对应原updateProfile（修复参数数量）
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET " +
            "username = COALESCE(:username, username), " +
            "email = COALESCE(:email, email), " +
            "nickname = COALESCE(:nickname, nickname), " +
            "phone = COALESCE(:phone, phone), " +
            "bio = COALESCE(:bio, bio), " +
            "updated_at = NOW() " +
            "WHERE id = :userId",
            nativeQuery = true)
    int updateProfile(@Param("userId") Long userId,
                      @Param("username") String username,
                      @Param("email") String email,
                      @Param("nickname") String nickname,
                      @Param("phone") String phone,
                      @Param("bio") String bio);

    // 更新密码 - 对应原updatePassword
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET password_hash = :hashedPassword, updated_at = NOW() WHERE id = :userId",
            nativeQuery = true)
    int updatePassword(@Param("userId") Long userId, @Param("hashedPassword") String hashedPassword);

    // 获取用户地址列表 - 对应原getAddresses
    @Query(value = "SELECT * FROM user_addresses WHERE user_id = :userId",
            nativeQuery = true)
    List<UserAddress> getAddresses(@Param("userId") Long userId);

    // 获取用户统计数据 - 对应原getStats
    @Query(value = "SELECT " +
            "(SELECT COUNT(*) FROM orders WHERE user_id = :userId) as orderTotal, " +
            "(SELECT COUNT(*) FROM favorites WHERE user_id = :userId) as favoriteTotal, " +
            "(SELECT IFNULL(SUM(final_amount), 0) FROM orders WHERE user_id = :userId) as spendTotal",
            nativeQuery = true)
    UserStatsVO getStats(@Param("userId") Long userId);

    // 更新用户头像 - 对应原updateAvatar
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET avatar_url = :avatarUrl, updated_at = NOW() WHERE id = :userId",
            nativeQuery = true)
    int updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    // 检查用户名是否存在（创建用户时使用）
    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE username = :username",
            nativeQuery = true)
    boolean existsByUsername(@Param("username") String username);

    // 新增：检查邮箱是否存在（注册时使用）
    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE email = :email",
            nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);
}