package com.library.model.dto;

import lombok.Data;

@Data
public class CarouselDTO {
    private Long id;
    private String imageUrl;
    private String title;
    private String description;
    private String link;
    private String buttonText;
    private Integer sortOrder;
    private String createdAt;
    private String updatedAt;
}