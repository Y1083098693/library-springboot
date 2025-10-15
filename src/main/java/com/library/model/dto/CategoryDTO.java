package com.library.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 分类数据传输对象
 * 用于在服务层与控制层之间传递分类信息，支持树形结构展示
 */
@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String parentName;
    private Integer displayOrder; // 新增：显示顺序，用于前端排序
    private Boolean isActive; // 新增：是否激活状态
    private List<CategoryDTO> children; // 新增：子分类列表，支持树形结构
}