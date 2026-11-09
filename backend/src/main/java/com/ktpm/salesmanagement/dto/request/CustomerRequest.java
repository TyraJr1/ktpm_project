package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "Tên khách hàng không được để trống")
        @Size(max = 150, message = "Tên khách hàng tối đa 150 ký tự")
        String customerName,

        @Pattern(regexp = "^$|^[0-9+() .-]{7,20}$", message = "Số điện thoại không đúng định dạng")
        String phone,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email,

        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String address
) {
}
