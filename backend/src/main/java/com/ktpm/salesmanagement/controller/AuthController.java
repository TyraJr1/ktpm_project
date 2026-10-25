package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.request.LoginRequest;
import com.ktpm.salesmanagement.dto.response.LoginResponse;
import com.ktpm.salesmanagement.dto.response.MessageResponse;
import com.ktpm.salesmanagement.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(new MessageResponse(
                "Đăng xuất thành công. Client phải xóa JWT; server không lưu trạng thái phiên."));
    }
}
