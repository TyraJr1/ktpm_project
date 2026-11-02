package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordChangeRequest(
        @NotBlank(message = "Mật khẩu không được để trống.")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.")
        String password
) {
}
