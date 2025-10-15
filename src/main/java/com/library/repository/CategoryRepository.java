package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.library.model.entity.Category;
import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问层
 * 提供分类相关的数据库操作方法
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 获取所有激活的分类（用于导航栏展示）
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllActive();

    // 根据slug获取单个激活的分类
    @Query("SELECT c FROM Category c WHERE c.slug = :slug AND c.isActive = true")
    Optional<Category> findBySlugAndActive(@Param("slug") String slug);

    // 根据ID获取单个激活的分类
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.isActive = true")
    Optional<Category> findByIdAndActive(@Param("id") Long id);

    // 获取分类树形结构（包含父子关系）
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllActiveForTree();

    // 检查分类名称是否已存在（用于新增/修改时校验）
    boolean existsByName(String name);

    // 检查分类标识（slug）是否已存在（排除当前ID，用于修改时校验）
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.slug = :slug AND c.id != :id")
    boolean existsBySlugAndNotId(@Param("slug") String slug, @Param("id") Long id);
}