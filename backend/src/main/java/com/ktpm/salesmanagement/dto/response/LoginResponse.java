package com.ktpm.salesmanagement.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String username,
        String fullName,
        String role
) {
}
