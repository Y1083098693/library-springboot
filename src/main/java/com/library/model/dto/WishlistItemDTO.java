package com.library.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏项DTO：用于展示用户收藏的图书信息及收藏时间
 */
@Data
public class WishlistItemDTO {
    // 1. 新增图书ID字段（匹配 WishlistServiceImpl 中的 setId 调用）
    private Long id;             // 图书ID（对应原代码 dto.setId(bookId)）

    // 2. 新增图书基础信息字段（匹配原代码中的 setTitle/setAuthor 等调用）
    private String title;        // 图书标题
    private String author;       // 图书作者
    private String coverImage;   // 图书封面图
    private BigDecimal price;    // 图书售价
    private String addedAt;      // 收藏时间字符串（对应原代码 dto.setAddedAt）

    // 3. 保留原有字段（不删除，避免其他依赖报错）
    private Long favoriteId;     // 收藏记录ID（原字段）
    private BookListItemDTO book;// 图书完整信息（原字段，若其他地方使用）
    private LocalDateTime createdAt; // 收藏时间（原字段）
}