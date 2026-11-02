package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountUpdateRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống.")
        @Size(max = 100, message = "Tên đăng nhập không được vượt quá 100 ký tự.")
        String username,

        @NotBlank(message = "Họ tên không được để trống.")
        @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự.")
        String fullName,

        @Email(message = "Email không đúng định dạng.")
        @Size(max = 150, message = "Email không được vượt quá 150 ký tự.")
        String email,

        @Pattern(regexp = "^$|^[0-9+() .-]{6,20}$", message = "Số điện thoại không đúng định dạng.")
        String phone,

        @NotBlank(message = "Vai trò không được để trống.")
        String role,

        @NotBlank(message = "Trạng thái không được để trống.")
        String status,

        @Pattern(regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.")
        String password
) {
}
