package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.library.model.entity.Carousel;
import java.util.List;
import java.util.Map;

public interface CarouselRepository extends JpaRepository<Carousel, Long> {

    // 获取所有轮播图（保持与数据库字段一致）
    @Query(value = "SELECT " +
            "id, " +
            "image_url, " +
            "title, " +
            "description, " +
            "link, " +
            "button_text, " +
            "sort_order, " +
            "created_at, " +
            "updated_at " +
            "FROM carousels " +
            "ORDER BY sort_order ASC",
            nativeQuery = true)
    List<Map<String, Object>> findAllCarousels();
}