package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 50) String productCode,
        @NotBlank @Size(max = 200) String productName,
        @NotNull @DecimalMin("0.00") BigDecimal salePrice,
        @NotBlank @Size(max = 20) String status,
        @Size(max = 500) String description
) {
}
