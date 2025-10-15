package com.library.service;

import com.library.model.dto.CategoryDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 获取所有激活的分类（对应Node.js中的getAllActiveCategories方法）
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        List<Map<String, Object>> categories = categoryRepository.findAllActive();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据slug获取分类（对应Node.js中的getCategoryBySlug方法）
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::convertToDTO);
    }

    /**
     * 根据ID获取激活的分类（对应Node.js中的getCategoryById方法）
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findByIdActive(id)
                .map(this::convertToDTO);
    }

    /**
     * 获取分类树形结构（对应Node.js中的getCategoryTree方法）
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        List<Map<String, Object>> categoryTree = categoryRepository.findCategoryTree();
        return categoryTree.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换数据库结果到DTO
     */
    private CategoryDTO convertToDTO(Map<String, Object> categoryMap) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(((Number) categoryMap.get("id")).longValue());
        dto.setName((String) categoryMap.get("name"));
        dto.setSlug((String) categoryMap.get("slug"));
        dto.setDescription((String) categoryMap.get("description"));
        if (categoryMap.containsKey("parent_id")) {
            dto.setParentId(categoryMap.get("parent_id") != null ? ((Number) categoryMap.get("parent_id")).longValue() : null);
        }
        if (categoryMap.containsKey("parent_name")) {
            dto.setParentName((String) categoryMap.get("parent_name"));
        }
        return dto;
    }
}