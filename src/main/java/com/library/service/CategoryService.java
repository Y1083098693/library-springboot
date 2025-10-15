package com.library.service;

import com.library.model.dto.CategoryDTO;
import java.util.List;
import java.util.Optional;

/**
 * 分类服务接口
 * 定义分类相关的业务方法规范
 */
public interface CategoryService {

    /**
     * 获取所有激活的分类
     * @return 激活的分类DTO列表
     */
    List<CategoryDTO> getAllActiveCategories();

    /**
     * 根据slug获取分类
     * @param slug 分类标识
     * @return 分类DTO（Optional包装，可能为空）
     */
    Optional<CategoryDTO> getCategoryBySlug(String slug);

    /**
     * 根据ID获取激活的分类
     * @param id 分类ID
     * @return 分类DTO（Optional包装，可能为空）
     */
    Optional<CategoryDTO> getCategoryById(Long id);

    /**
     * 获取分类树形结构（包含父子关系）
     * @return 树形结构的分类DTO列表
     */
    List<CategoryDTO> getCategoryTree();
}