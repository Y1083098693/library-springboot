package com.library.service;

import com.library.model.dto.UserAddressDTO;
import com.library.model.dto.request.CreateAddressRequest;
import com.library.model.dto.request.UpdateAddressRequest;
import com.library.model.entity.UserAddress;
import com.library.repository.UserAddressRepository;
import com.library.exception.BadRequestException;
import com.library.exception.ApiError;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    // ------------------------------ 原有方法（保留） ------------------------------
    @Transactional(readOnly = true)
    public List<UserAddressDTO> getUserAddresses(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findAllByUserId(userId);
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserAddressDTO getAddressById(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiError(404, "地址不存在或不属于当前用户"));
        return convertToDTO(address);
    }

    @Transactional
    public UserAddressDTO createAddress(Long userId, CreateAddressRequest request) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());

        // 新用户首次添加地址，默认设为默认地址
        long userAddressCount = userAddressRepository.countByUserId(userId);
        address.setDefault(userAddressCount == 0);

        UserAddress savedAddress = userAddressRepository.save(address);
        return convertToDTO(savedAddress);
    }

    @Transactional
    public UserAddressDTO updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiError(404, "地址不存在或不属于当前用户"));

        address.setRecipientName(request.getRecipientName());
        address.setRecipientPhone(request.getRecipientPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());

        UserAddress updatedAddress = userAddressRepository.save(address);
        return convertToDTO(updatedAddress);
    }

    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        // 先验证地址归属
        if (!userAddressRepository.existsByIdAndUserId(addressId, userId)) {
            throw new ApiError(404, "地址不存在或不属于当前用户");
        }

        // 取消所有地址的默认状态，再将当前地址设为默认
        userAddressRepository.cancelAllDefault(userId);
        int updatedRows = userAddressRepository.setDefaultAddress(addressId, userId);
        if (updatedRows == 0) {
            throw new BadRequestException("设置默认地址失败");
        }
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiError(404, "地址不存在或不属于当前用户"));

        // 若删除的是默认地址，将最新添加的地址设为新默认
        boolean isDeletedDefault = address.isDefault();
        userAddressRepository.delete(address);

        if (isDeletedDefault) {
            // 查询用户剩余地址，按创建时间倒序取第一个设为默认
            List<UserAddress> remainingAddresses = userAddressRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
            if (!remainingAddresses.isEmpty()) {
                UserAddress newDefault = remainingAddresses.get(0);
                userAddressRepository.setDefaultAddress(newDefault.getId(), userId);
            }
        }
    }

    // ------------------------------ 新增：getDefaultAddress方法（核心修复） ------------------------------
    /**
     * 获取用户默认地址（匹配Controller调用）
     */
    @Transactional(readOnly = true)
    public UserAddressDTO getDefaultAddress(Long userId) {
        // 调用Repository的findDefaultByUserId方法，查询默认地址
        UserAddress defaultAddress = userAddressRepository.findDefaultByUserId(userId)
                .orElseThrow(() -> new ApiError(404, "用户暂无默认地址"));
        return convertToDTO(defaultAddress);
    }

    // ------------------------------ 私有工具方法（保留） ------------------------------
    private UserAddressDTO convertToDTO(UserAddress address) {
        UserAddressDTO dto = new UserAddressDTO();
        dto.setId(address.getId());
        dto.setRecipientName(address.getRecipientName());
        dto.setRecipientPhone(address.getRecipientPhone());
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetailAddress(address.getDetailAddress());
        dto.setIsDefault(address.isDefault());
        dto.setCreatedAt(address.getCreatedAt().toString());
        dto.setUpdatedAt(address.getUpdatedAt().toString());
        return dto;
    }
}