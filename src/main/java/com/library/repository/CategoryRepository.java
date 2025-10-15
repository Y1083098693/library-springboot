package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.library.model.entity.Category;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 获取所有分类（用于导航栏）
    @Query(value = "SELECT id, name, slug, description " +
            "FROM categories " +
            "WHERE is_active = 1 " +
            "ORDER BY sort_order ASC, name ASC",
            nativeQuery = true)
    List<Map<String, Object>> findAllActive();

    // 根据slug获取单个分类详情
    @Query(value = "SELECT id, name, slug, description " +
            "FROM categories " +
            "WHERE slug = :slug AND is_active = 1",
            nativeQuery = true)
    Optional<Map<String, Object>> findBySlug(@Param("slug") String slug);

    // 根据ID获取单个分类详情
    @Query(value = "SELECT id, name, slug, description " +
            "FROM categories " +
            "WHERE id = :id AND is_active = 1",
            nativeQuery = true)
    Optional<Map<String, Object>> findByIdActive(@Param("id") Long id);

    // 获取分类树形结构（包含父子关系）
    @Query(value = "SELECT " +
            "c1.id, " +
            "c1.name, " +
            "c1.slug, " +
            "c1.description, " +
            "c1.parent_id, " +
            "c2.name as parent_name " +
            "FROM categories c1 " +
            "LEFT JOIN categories c2 ON c1.parent_id = c2.id " +
            "WHERE c1.is_active = 1 " +
            "ORDER BY c1.sort_order ASC, c1.name ASC",
            nativeQuery = true)
    List<Map<String, Object>> findCategoryTree();
}