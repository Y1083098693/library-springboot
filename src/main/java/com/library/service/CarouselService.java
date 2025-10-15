package com.library.service;

import com.library.model.dto.CarouselDTO;
import com.library.repository.CarouselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarouselService {

    private final CarouselRepository carouselRepository;

    /**
     * 获取所有轮播图（对应Node.js中的getAllCarousels方法）
     */
    @Transactional(readOnly = true)
    public List<CarouselDTO> getAllCarousels() {
        List<Map<String, Object>> carousels = carouselRepository.findAllCarousels();
        return carousels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换数据库结果到DTO
     */
    private CarouselDTO convertToDTO(Map<String, Object> carouselMap) {
        CarouselDTO dto = new CarouselDTO();
        dto.setId(((Number) carouselMap.get("id")).longValue());
        dto.setImageUrl((String) carouselMap.get("image_url"));
        dto.setTitle((String) carouselMap.get("title"));
        dto.setDescription((String) carouselMap.get("description"));
        dto.setLink((String) carouselMap.get("link"));
        dto.setButtonText((String) carouselMap.get("button_text"));
        dto.setSortOrder(((Number) carouselMap.get("sort_order")).intValue());
        dto.setCreatedAt((String) carouselMap.get("created_at"));
        dto.setUpdatedAt((String) carouselMap.get("updated_at"));
        return dto;
    }
}