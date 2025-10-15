package com.library.controller;

import com.library.model.dto.UserAddressDTO;
import com.library.model.dto.request.CreateAddressRequest;
import com.library.model.dto.request.UpdateAddressRequest;
import com.library.model.dto.response.ApiResponse;
import com.library.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
@Tag(name = "用户地址管理", description = "用户地址的CRUD及默认地址设置接口")
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 获取当前用户所有地址
     */
    @GetMapping
    @Operation(summary = "获取用户所有地址", description = "查询当前登录用户的所有收货地址列表")
    public ResponseEntity<ApiResponse<List<UserAddressDTO>>> getUserAddresses(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName()); // 从JWT中获取用户ID
        List<UserAddressDTO> addresses = userAddressService.getUserAddresses(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "地址列表获取成功", addresses));
    }

    /**
     * 获取当前用户的默认地址
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认地址", description = "查询当前登录用户的默认收货地址")
    public ResponseEntity<ApiResponse<UserAddressDTO>> getDefaultAddress(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        UserAddressDTO defaultAddress = userAddressService.getDefaultAddress(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "默认地址获取成功", defaultAddress));
    }

    /**
     * 根据地址ID获取指定地址
     */
    @GetMapping("/{addressId}")
    @Operation(summary = "获取指定地址", description = "根据地址ID查询当前登录用户的指定收货地址")
    public ResponseEntity<ApiResponse<UserAddressDTO>> getAddressById(
            Authentication authentication,
            @PathVariable Long addressId) {
        Long userId = Long.valueOf(authentication.getName());
        UserAddressDTO address = userAddressService.getAddressById(userId, addressId);
        return ResponseEntity.ok(new ApiResponse<>(true, "地址获取成功", address));
    }

    /**
     * 新增收货地址
     */
    @PostMapping
    @Operation(summary = "新增收货地址", description = "为当前登录用户添加新的收货地址")
    public ResponseEntity<ApiResponse<UserAddressDTO>> createAddress(
            Authentication authentication,
            @Valid @RequestBody CreateAddressRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        UserAddressDTO newAddress = userAddressService.createAddress(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "地址创建成功", newAddress));
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/{addressId}")
    @Operation(summary = "更新收货地址", description = "修改当前登录用户的指定收货地址信息")
    public ResponseEntity<ApiResponse<UserAddressDTO>> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        Long userId = Long.valueOf(authentication.getName());
        UserAddressDTO updatedAddress = userAddressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "地址更新成功", updatedAddress));
    }

    /**
     * 设置默认地址
     */
    @PatchMapping("/{addressId}/default")
    @Operation(summary = "设置默认地址", description = "将当前登录用户的指定地址设为默认地址")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        Long userId = Long.valueOf(authentication.getName());
        userAddressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(new ApiResponse<>(true, "默认地址设置成功", null));
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/{addressId}")
    @Operation(summary = "删除收货地址", description = "删除当前登录用户的指定收货地址")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        Long userId = Long.valueOf(authentication.getName());
        userAddressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(new ApiResponse<>(true, "地址删除成功", null));
    }
}