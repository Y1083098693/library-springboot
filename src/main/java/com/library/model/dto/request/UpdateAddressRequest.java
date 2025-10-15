package com.library.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAddressRequest {
    @NotBlank(message = "收件人姓名不能为空")
    private String recipientName; // 改为recipientName，匹配实体

    @NotBlank(message = "收件人手机号不能为空")
    private String recipientPhone; // 改为recipientPhone，匹配实体

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
}