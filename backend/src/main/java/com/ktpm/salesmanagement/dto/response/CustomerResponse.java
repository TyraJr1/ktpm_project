package com.ktpm.salesmanagement.dto.response;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String customerName,
        String phone,
        String email,
        String address,
        LocalDateTime createdAt
) {
}
