package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusRequest(@NotBlank @Size(max = 20) String status) {
}
