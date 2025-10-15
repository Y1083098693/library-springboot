package com.library.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BookDetailDTO {
    private Long id;
    private String title;
    private String author;
    private String coverImage;
    private BigDecimal price;  // 修复：由Double改为BigDecimal，与服务层保持一致
    private BigDecimal originalPrice;  // 修复：由Double改为BigDecimal
    private Integer discount;
    private BigDecimal rating;  // 修复：由Double改为BigDecimal
    private Integer reviews;
    private String description;
    private String category;
    private Integer stock;
    private Boolean isAvailable;  // 注意字段名是isAvailable，setter会自动生成isAvailable()
    private List<BookListItemDTO> relatedBooks;  // 修复：类型改为BookListItemDTO
}