package com.library.controller;

import com.library.model.dto.OrderDTO;
import com.library.model.dto.PagedResultDTO;
import com.library.model.dto.request.CreateOrderRequest;
import com.library.model.dto.response.ApiResponse;
import com.library.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单的创建、查询、取消、确认收货等接口")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "根据用户选择的商品和地址创建新订单")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        OrderDTO orderDTO = orderService.createOrder(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "订单创建成功", orderDTO));
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping
    @Operation(summary = "获取订单列表", description = "分页查询当前用户的订单列表，支持按状态筛选")
    public ResponseEntity<ApiResponse<PagedResultDTO<OrderDTO>>> getUserOrders(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = Long.valueOf(authentication.getName());
        PagedResultDTO<OrderDTO> orders = orderService.getUserOrders(userId, status, page, limit);
        return ResponseEntity.ok(new ApiResponse<>(true, "订单列表获取成功", orders));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "根据订单ID查询当前用户的订单详情，包含订单项")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetail(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long userId = Long.valueOf(authentication.getName());
        OrderDTO orderDTO = orderService.getOrderById(userId, orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "订单详情获取成功", orderDTO));
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单", description = "取消当前用户的待支付订单，恢复商品库存")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long userId = Long.valueOf(authentication.getName());
        orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "订单取消成功", null));
    }

    /**
     * 确认收货
     */
    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "确认收货", description = "确认当前用户的已发货订单已收到")
    public ResponseEntity<ApiResponse<Void>> confirmReceipt(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long userId = Long.valueOf(authentication.getName());
        orderService.confirmReceipt(userId, orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "确认收货成功", null));
    }

    /**
     * 支付订单
     */
    @PostMapping("/{orderId}/pay")
    @Operation(summary = "支付订单", description = "支付当前用户的待支付订单")
    public ResponseEntity<ApiResponse<Void>> payOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        Long userId = Long.valueOf(authentication.getName());
        orderService.payOrder(userId, orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "订单支付成功", null));
    }
}