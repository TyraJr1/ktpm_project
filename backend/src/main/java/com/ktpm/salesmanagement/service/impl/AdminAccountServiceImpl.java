package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.request.AccountCreateRequest;
import com.ktpm.salesmanagement.dto.request.AccountUpdateRequest;
import com.ktpm.salesmanagement.dto.response.AccountResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.exception.DuplicateResourceException;
import com.ktpm.salesmanagement.exception.ResourceNotFoundException;
import com.ktpm.salesmanagement.repository.PurchaseReceiptRepository;
import com.ktpm.salesmanagement.repository.SalesOrderRepository;
import com.ktpm.salesmanagement.repository.StockTransactionRepository;
import com.ktpm.salesmanagement.repository.UserRepository;
import com.ktpm.salesmanagement.security.SystemRole;
import com.ktpm.salesmanagement.service.AdminAccountService;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccountServiceImpl implements AdminAccountService {
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountServiceImpl(UserRepository userRepository,
                                   SalesOrderRepository salesOrderRepository,
                                   PurchaseReceiptRepository purchaseReceiptRepository,
                                   StockTransactionRepository stockTransactionRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseReceiptRepository = purchaseReceiptRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> list(String query, String role, String status, Pageable pageable) {
        return PageResponse.from(userRepository.search(optional(query), optionalRole(role),
                optionalStatus(status), pageable).map(this::toResponse));
    }

    @Override
    @Transactional
    public AccountResponse create(AccountCreateRequest request) {
        String username = required(request.username(), "Tên đăng nhập không được để trống.");
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại.");
        }
        validatePassword(request.password());
        User user = new User();
        apply(user, username, request.fullName(), request.email(), request.phone(),
                normalizeRole(request.role()), normalizeStatus(request.status()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public AccountResponse update(Long id, AccountUpdateRequest request, Long currentUserId) {
        User user = find(id);
        String username = required(request.username(), "Tên đăng nhập không được để trống.");
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại.");
        }
        String role = normalizeRole(request.role());
        String status = normalizeStatus(request.status());
        if (id.equals(currentUserId)
                && (!user.getRole().equals(role) || !"ACTIVE".equals(status))) {
            throw new BusinessRuleException("Không thể hạ quyền hoặc khóa tài khoản đang đăng nhập.");
        }
        apply(user, username, request.fullName(), request.email(), request.phone(), role, status);
        if (request.password() != null && !request.password().isBlank()) {
            validatePassword(request.password());
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long id, String password) {
        validatePassword(password);
        User user = find(id);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id, Long currentUserId) {
        User user = find(id);
        if (id.equals(currentUserId)) {
            throw new BusinessRuleException("Không thể xóa tài khoản đang đăng nhập.");
        }
        if (salesOrderRepository.existsByCreatedBy_Id(id)
                || purchaseReceiptRepository.existsByCreatedBy_Id(id)
                || stockTransactionRepository.existsByCreatedBy_Id(id)) {
            throw new BusinessRuleException("Không thể xóa tài khoản đã phát sinh giao dịch.");
        }
        userRepository.delete(user);
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản."));
    }

    private void apply(User user, String username, String fullName, String email, String phone,
                       String role, String status) {
        user.setUsername(username);
        user.setFullName(required(fullName, "Họ tên không được để trống."));
        user.setEmail(optional(email));
        user.setPhone(optional(phone));
        user.setRole(role);
        user.setStatus(status);
    }

    private String normalizeRole(String value) {
        String normalized = required(value, "Vai trò không được để trống.").toUpperCase(Locale.ROOT);
        try {
            return SystemRole.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Vai trò không hợp lệ.");
        }
    }

    private String optionalRole(String value) {
        return value == null || value.isBlank() ? null : normalizeRole(value);
    }

    private String normalizeStatus(String value) {
        String normalized = required(value, "Trạng thái không được để trống.").toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new BusinessRuleException("Trạng thái tài khoản không hợp lệ.");
        }
        return normalized;
    }

    private String optionalStatus(String value) {
        return value == null || value.isBlank() ? null : normalizeStatus(value);
    }

    private void validatePassword(String password) {
        if (password == null || !PASSWORD.matcher(password).matches()) {
            throw new BusinessRuleException(
                    "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số.");
        }
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new BusinessRuleException(message);
        return value.trim();
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AccountResponse toResponse(User user) {
        return new AccountResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.getRole(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
