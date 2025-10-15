package com.library.service.impl;

import com.library.exception.ApiError;
import com.library.model.dto.CategoryDTO;
import com.library.model.entity.Category;
import com.library.repository.CategoryRepository;
import com.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 获取所有激活的分类
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        // 修复：接收类型改为List<Category>，与Repository返回类型匹配
        List<Category> categories = categoryRepository.findAllActive();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据slug获取分类
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new ApiError(400, "分类标识不能为空");
        }
        // 修复：调用正确的Repository方法findBySlugAndActive
        return categoryRepository.findBySlugAndActive(slug)
                .map(this::convertToDTO);
    }

    /**
     * 根据ID获取激活的分类
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryById(Long id) {
        validateId(id);
        // 修复：调用正确的Repository方法findByIdAndActive
        return categoryRepository.findByIdAndActive(id)
                .map(this::convertToDTO);
    }

    /**
     * 获取分类树形结构（包含父子关系）
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        // 修复：接收类型改为List<Category>，调用正确的Repository方法
        List<Category> categoryTree = categoryRepository.findAllActiveForTree();
        return categoryTree.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换实体到DTO（替换原有的Map转换方法）
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParentId());
        // 如需父分类名称，可在此处通过额外查询补充
        return dto;
    }

    /**
     * 验证分类ID有效性（非null且>0）
     */
    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ApiError(400, "无效的分类ID: " + id);
        }
    }
}