package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank @Size(max = 50) String supplierCode,
        @NotBlank @Size(max = 200) String supplierName,
        @Pattern(regexp = "^$|^[0-9+() .-]{7,20}$", message = "Số điện thoại không đúng định dạng") String phone,
        @Email @Size(max = 150) String email,
        @Size(max = 255) String address,
        @NotBlank @Size(max = 20) String status
) {
}
