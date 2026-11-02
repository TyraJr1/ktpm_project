package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 150) String categoryName,
        @Size(max = 255) String description,
        @NotBlank @Size(max = 20) String status
) {
}
