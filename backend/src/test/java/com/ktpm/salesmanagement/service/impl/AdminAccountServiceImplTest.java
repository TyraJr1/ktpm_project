package com.ktpm.salesmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.dto.request.AccountCreateRequest;
import com.ktpm.salesmanagement.dto.request.AccountUpdateRequest;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.repository.PurchaseReceiptRepository;
import com.ktpm.salesmanagement.repository.SalesOrderRepository;
import com.ktpm.salesmanagement.repository.StockTransactionRepository;
import com.ktpm.salesmanagement.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock SalesOrderRepository salesOrderRepository;
    @Mock PurchaseReceiptRepository purchaseReceiptRepository;
    @Mock StockTransactionRepository stockTransactionRepository;

    private AdminAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminAccountServiceImpl(userRepository, salesOrderRepository,
                purchaseReceiptRepository, stockTransactionRepository, new BCryptPasswordEncoder(4));
    }

    @Test
    void createHashesPasswordAndUsesExistingRoleSet() {
        when(userRepository.existsByUsernameIgnoreCase("staff02")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        var result = service.create(new AccountCreateRequest(" staff02 ", "Strong123",
                "Nhân viên mới", "staff@example.com", "0901234567", "SALES_STAFF", "ACTIVE"));

        assertEquals("staff02", result.username());
        assertEquals("SALES_STAFF", result.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createRejectsWeakPasswordBeforeSave() {
        when(userRepository.existsByUsernameIgnoreCase("staff02")).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> service.create(new AccountCreateRequest(
                "staff02", "password", "Nhân viên", null, null, "SALES_STAFF", "ACTIVE")));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRejectsLoweringCurrentAdminRole() {
        User current = user(1L, "admin01", "ADMIN", "ACTIVE");
        when(userRepository.findById(1L)).thenReturn(Optional.of(current));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("admin01", 1L)).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> service.update(1L,
                new AccountUpdateRequest("admin01", "Admin", null, null, "SALES_STAFF", "ACTIVE", null), 1L));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteRejectsAccountWithSalesTransaction() {
        User user = user(2L, "sales01", "SALES_STAFF", "ACTIVE");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(salesOrderRepository.existsByCreatedBy_Id(2L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> service.delete(2L, 1L));

        verify(userRepository, never()).delete(user);
    }

    private User user(Long id, String username, String role, String status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setFullName("Admin");
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
