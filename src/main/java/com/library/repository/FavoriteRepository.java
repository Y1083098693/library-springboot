package com.library.repository;

import com.library.model.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 检查用户是否已收藏某本书
    boolean existsByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    // 根据用户ID和图书ID查询收藏记录
    Optional<Favorite> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
}