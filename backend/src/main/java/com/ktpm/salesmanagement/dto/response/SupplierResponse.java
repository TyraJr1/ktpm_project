package com.ktpm.salesmanagement.dto.response;

public record SupplierResponse(
        Long id,
        String supplierCode,
        String supplierName,
        String phone,
        String email,
        String address,
        String status
) {
}
