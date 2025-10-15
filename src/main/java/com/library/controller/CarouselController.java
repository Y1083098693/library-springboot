package com.library.controller;

import com.library.model.dto.CarouselDTO;
import com.library.model.dto.response.ApiResponse;
import com.library.service.CarouselService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carousels")
@RequiredArgsConstructor
@Tag(name = "轮播图管理", description = "轮播图的查询接口")
public class CarouselController {

    private final CarouselService carouselService;

    /**
     * 获取所有轮播图
     */
    @GetMapping
    @Operation(summary = "获取所有轮播图", description = "查询所有轮播图数据，按排序顺序返回")
    public ResponseEntity<ApiResponse<List<CarouselDTO>>> getAllCarousels() {
        List<CarouselDTO> carousels = carouselService.getAllCarousels();
        return ResponseEntity.ok(new ApiResponse<>(true, "轮播图列表获取成功", carousels));
    }
}