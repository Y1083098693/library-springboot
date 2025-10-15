package com.library.repository;

import com.library.model.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    // 补充：查询用户默认地址（匹配Service的getDefaultAddress调用）
    @Query(value = "SELECT * FROM user_addresses WHERE user_id = :userId AND is_default = true", nativeQuery = true)
    Optional<UserAddress> findDefaultByUserId(@Param("userId") Long userId);

    // 原有方法（保留）
    List<UserAddress> findAllByUserId(@Param("userId") Long userId);
    Optional<UserAddress> findByIdAndUserId(@Param("id") Long addressId, @Param("userId") Long userId);
    long countByUserId(@Param("userId") Long userId);
    boolean existsByIdAndUserId(@Param("id") Long addressId, @Param("userId") Long userId);
    @Transactional
    @Modifying
    @Query(value = "UPDATE user_addresses SET is_default = false WHERE user_id = :userId", nativeQuery = true)
    void cancelAllDefault(@Param("userId") Long userId);
    @Transactional
    @Modifying
    @Query(value = "UPDATE user_addresses SET is_default = true WHERE id = :addressId AND user_id = :userId", nativeQuery = true)
    int setDefaultAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);
    // 按创建时间倒序查询用户地址（用于删除默认地址后选新默认）
    List<UserAddress> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}