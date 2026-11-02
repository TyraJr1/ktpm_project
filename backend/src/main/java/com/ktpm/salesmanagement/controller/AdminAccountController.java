package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.request.AccountCreateRequest;
import com.ktpm.salesmanagement.dto.request.AccountUpdateRequest;
import com.ktpm.salesmanagement.dto.request.PasswordChangeRequest;
import com.ktpm.salesmanagement.dto.response.AccountResponse;
import com.ktpm.salesmanagement.dto.response.MessageResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.security.CustomUserPrincipal;
import com.ktpm.salesmanagement.service.AdminAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
    private final AdminAccountService service;

    public AdminAccountController(AdminAccountService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AccountResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(query, role, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody AccountCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public AccountResponse update(@PathVariable Long id,
                                  @Valid @RequestBody AccountUpdateRequest request,
                                  @AuthenticationPrincipal CustomUserPrincipal principal) {
        return service.update(id, request, principal.getUserId());
    }

    @PatchMapping("/{id}/password")
    public MessageResponse changePassword(@PathVariable Long id,
                                          @Valid @RequestBody PasswordChangeRequest request) {
        service.changePassword(id, request.password());
        return new MessageResponse("Đổi mật khẩu thành công.");
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserPrincipal principal) {
        service.delete(id, principal.getUserId());
        return new MessageResponse("Xóa tài khoản thành công.");
    }
}
