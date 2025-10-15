package com.library.controller;

import com.library.model.dto.CategoryDTO;
import com.library.model.dto.response.ApiResponse;
import com.library.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "分类管理", description = "图书分类的查询、树形结构获取等接口")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有激活的分类
     */
    @GetMapping
    @Operation(summary = "获取所有激活分类", description = "查询所有可用的图书分类（用于导航栏等场景）")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllActiveCategories() {
        List<CategoryDTO> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(new ApiResponse<>(true, "分类列表获取成功", categories));
    }

    /**
     * 根据slug获取分类详情
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "通过slug获取分类", description = "根据分类的slug标识查询分类详情")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryBySlug(@PathVariable String slug) {
        CategoryDTO category = categoryService.getCategoryBySlug(slug)
                .orElseThrow(() -> new RuntimeException("分类不存在或已停用"));
        return ResponseEntity.ok(new ApiResponse<>(true, "分类获取成功", category));
    }

    /**
     * 根据ID获取激活的分类
     */
    @GetMapping("/{id}")
    @Operation(summary = "通过ID获取分类", description = "根据分类ID查询激活状态的分类详情")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在或已停用"));
        return ResponseEntity.ok(new ApiResponse<>(true, "分类获取成功", category));
    }

    /**
     * 获取分类树形结构（包含父子关系）
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树形结构", description = "查询包含父子关系的分类树形结构数据")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoryTree() {
        List<CategoryDTO> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(new ApiResponse<>(true, "分类树形结构获取成功", categoryTree));
    }
}