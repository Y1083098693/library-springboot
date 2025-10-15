package com.library.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收藏项DTO：用于展示用户收藏的图书信息及收藏时间
 */
@Data
public class WishlistItemDTO {
    // 收藏记录ID
    private Long favoriteId;

    // 图书基本信息（复用BookListItemDTO）
    private BookListItemDTO book;

    // 收藏时间（用于排序和展示）
    private LocalDateTime createdAt;
}