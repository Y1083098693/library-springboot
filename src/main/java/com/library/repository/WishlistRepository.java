package com.library.repository;

import com.library.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * 检查用户是否已收藏某图书
     */
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    /**
     * 根据用户ID和图书ID查询收藏记录
     */
    Optional<Wishlist> findByUserIdAndBookId(Long userId, Long bookId);

    /**
     * 查询用户的收藏列表（关联图书信息）
     */
    @Query(value = "SELECT b.id, b.title, b.author, b.cover_image, b.selling_price, w.created_at " +
            "FROM wishlist w " +
            "JOIN books b ON w.book_id = b.id " +
            "WHERE w.user_id = :userId " +
            "ORDER BY w.created_at DESC " +
            "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Object[]> findUserWishlist(
            @Param("userId") Long userId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /**
     * 统计用户收藏总数
     */
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和图书ID删除收藏
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.userId = :userId AND w.bookId = :bookId")
    int deleteByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
}