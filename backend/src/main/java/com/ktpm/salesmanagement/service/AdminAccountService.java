package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.request.AccountCreateRequest;
import com.ktpm.salesmanagement.dto.request.AccountUpdateRequest;
import com.ktpm.salesmanagement.dto.response.AccountResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminAccountService {
    PageResponse<AccountResponse> list(String query, String role, String status, Pageable pageable);
    AccountResponse create(AccountCreateRequest request);
    AccountResponse update(Long id, AccountUpdateRequest request, Long currentUserId);
    void changePassword(Long id, String password);
    void delete(Long id, Long currentUserId);
}
