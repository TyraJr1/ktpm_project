package com.ktpm.salesmanagement.dto.response;

import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
