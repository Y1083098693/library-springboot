package com.library.repository;

import com.library.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 原有方法（保留）
    @Query(value = "SELECT stock_quantity, selling_price, title " +
            "FROM books WHERE id = :bookId",
            nativeQuery = true)
    Optional<Map<String, Object>> getBookStock(@Param("bookId") Long bookId);

    @Query(value = "SELECT title, cover_image, selling_price " +
            "FROM books WHERE id = :bookId",
            nativeQuery = true)
    Optional<Map<String, Object>> getBookById(@Param("bookId") Long bookId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE books SET stock_quantity = stock_quantity - :quantity " +
            "WHERE id = :bookId AND stock_quantity >= :quantity",
            nativeQuery = true)
    int decreaseStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    @Transactional
    @Modifying
    @Query(value = "UPDATE books SET stock_quantity = stock_quantity + :quantity " +
            "WHERE id = :bookId",
            nativeQuery = true)
    void increaseStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    // ------------------------------ 修复的findBooks方法 ------------------------------
    /**
     * 多条件查询图书（修复SQL语法：完整CASE表达式，补充闭合括号）
     */
    @Query(value = "SELECT b.id, b.title, b.author, b.cover_image, b.selling_price, b.original_price, b.rating " +
            "FROM books b " +
            "LEFT JOIN book_categories bc ON b.id = bc.book_id " +
            "WHERE (:keyword IS NULL OR b.title LIKE CONCAT('%', :keyword, '%') OR b.author LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:categoryId IS NULL OR bc.category_id = :categoryId) " +
            "GROUP BY b.id " +
            "ORDER BY " +
            // 修复：每个CASE表达式添加END，且用逗号分隔不同排序条件
            "CASE WHEN :sortOrder = 'recommended' THEN b.popularity END DESC, " +
            "CASE WHEN :sortOrder = 'newest' THEN b.publish_date END DESC, " +
            "CASE WHEN :sortOrder = 'price_asc' THEN b.selling_price END ASC, " +
            "CASE WHEN :sortOrder = 'price_desc' THEN b.selling_price END DESC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Map<String, Object>> findBooks(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("sortOrder") String sortOrder,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    // 其他新增方法（countBooks、getHotBooks等，保留不变）
    @Query(value = "SELECT COUNT(DISTINCT b.id) " +
            "FROM books b " +
            "LEFT JOIN book_categories bc ON b.id = bc.book_id " +
            "WHERE (:keyword IS NULL OR b.title LIKE CONCAT('%', :keyword, '%') OR b.author LIKE CONCAT('%', :keyword, '%')) " +
            "AND (:categoryId IS NULL OR bc.category_id = :categoryId)",
            nativeQuery = true)
    Long countBooks(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId
    );

    @Query(value = "SELECT id, title, author, cover_image, selling_price, original_price, rating " +
            "FROM books " +
            "ORDER BY popularity DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Map<String, Object>> getHotBooks(@Param("limit") Integer limit);

    @Query(value = "SELECT id, title, author, cover_image, selling_price, original_price, rating " +
            "FROM books " +
            "ORDER BY publish_date DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Map<String, Object>> getNewBooks(@Param("limit") Integer limit);

    @Query(value = "SELECT b.id, b.title, b.author, b.cover_image, b.selling_price, b.original_price, b.rating " +
            "FROM book_relations br " +
            "LEFT JOIN books b ON br.related_book_id = b.id " +
            "WHERE br.book_id = :bookId " +
            "LIMIT 4",
            nativeQuery = true)
    List<Map<String, Object>> getRelatedBooks(@Param("bookId") Long bookId);

    @Query(value = "SELECT b.id, b.title, b.author, b.cover_image, b.selling_price, b.original_price, b.rating " +
            "FROM books b " +
            "LEFT JOIN book_categories bc ON b.id = bc.book_id " +
            "WHERE bc.category_id = :categoryId " +
            "AND b.id != :excludeBookId " +
            "ORDER BY popularity DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Map<String, Object>> getBooksByCategoryExclude(
            @Param("categoryId") Long categoryId,
            @Param("excludeBookId") Long excludeBookId,
            @Param("limit") Integer limit
    );
}