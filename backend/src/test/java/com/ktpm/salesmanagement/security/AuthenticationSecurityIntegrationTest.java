package com.ktpm.salesmanagement.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.repository.CategoryRepository;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.SupplierRepository;
import com.ktpm.salesmanagement.repository.UserRepository;
import com.ktpm.salesmanagement.repository.SalesOrderRepository;
import com.ktpm.salesmanagement.repository.PurchaseReceiptRepository;
import com.ktpm.salesmanagement.repository.StockTransactionRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@EnableAutoConfiguration(excludeName = {
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
        "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
        "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class AuthenticationSecurityIntegrationTest {
    private static final String TEST_PASSWORD = "integration-test-password";
    private static final String TEST_SECRET = randomSecret();

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add("security.jwt.secret", () -> TEST_SECRET);
    }

    @Autowired WebApplicationContext context;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean ProductRepository productRepository;
    @MockitoBean SupplierRepository supplierRepository;
    @MockitoBean InvoiceRepository invoiceRepository;
    @MockitoBean OrderDetailRepository orderDetailRepository;
    @MockitoBean SalesOrderRepository salesOrderRepository;
    @MockitoBean PurchaseReceiptRepository purchaseReceiptRepository;
    @MockitoBean StockTransactionRepository stockTransactionRepository;

    private MockMvc mockMvc;
    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        users = Map.of(
                "admin01", user(1L, "admin01", "ADMIN", "ACTIVE"),
                "sales01", user(2L, "sales01", "SALES_STAFF", "ACTIVE"),
                "warehouse01", user(3L, "warehouse01", "WAREHOUSE_STAFF", "ACTIVE"),
                "inactive01", user(4L, "inactive01", "SALES_STAFF", "INACTIVE")
        );
        when(userRepository.findByUsername(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(users.get(invocation.getArgument(0))));
        when(userRepository.search(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(users.values().stream().toList()));
        when(invoiceRepository.sumTotalAmountByStatusAndDateRange(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
    }

    @Test
    void loginSucceedsWithValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin01", TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("admin01"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginRejectsUnknownUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("missing", TEST_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không chính xác."));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin01", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không chính xác."));
    }

    @Test
    void loginRejectsInactiveAccount() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("inactive01", TEST_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginRequiresUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"value\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.username").exists());
    }

    @Test
    void loginRequiresPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/ping").header("Authorization", bearer("admin01")))
                .andExpect(status().isOk());
    }

    @Test
    void salesCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/ping").header("Authorization", bearer("sales01")))
                .andExpect(status().isForbidden());
    }

    @Test
    void salesCannotAccessAdminProductApi() throws Exception {
        mockMvc.perform(get("/api/admin/products").header("Authorization", bearer("sales01")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAccountAndReportApis() throws Exception {
        mockMvc.perform(get("/api/admin/accounts").header("Authorization", bearer("admin01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mockMvc.perform(get("/api/admin/reports/summary?fromDate=2026-11-01&toDate=2026-11-02")
                        .header("Authorization", bearer("admin01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(0));
    }

    @Test
    void salesCannotAccessAccountOrReportApis() throws Exception {
        mockMvc.perform(get("/api/admin/accounts").header("Authorization", bearer("sales01")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/reports/summary?fromDate=2026-11-01&toDate=2026-11-02")
                        .header("Authorization", bearer("sales01")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminReportRejectsReversedDateRange() throws Exception {
        mockMvc.perform(get("/api/admin/reports/summary?fromDate=2026-11-02&toDate=2026-11-01")
                        .header("Authorization", bearer("admin01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminProductApiRejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/admin/products?page=-1").header("Authorization", bearer("admin01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void warehouseCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/ping").header("Authorization", bearer("warehouse01")))
                .andExpect(status().isForbidden());
    }

    @Test
    void salesCanAccessSalesEndpoint() throws Exception {
        mockMvc.perform(get("/api/sales/ping").header("Authorization", bearer("sales01")))
                .andExpect(status().isOk());
    }

    @Test
    void warehouseCanAccessWarehouseEndpoint() throws Exception {
        mockMvc.perform(get("/api/warehouse/ping").header("Authorization", bearer("warehouse01")))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointWithoutJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/ping")).andExpect(status().isUnauthorized());
    }

    @Test
    void statelessLogoutRequiresValidJwtAndReturnsAcknowledgement() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer("admin01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    private String bearer(String username) {
        return "Bearer " + jwtService.generateToken(CustomUserPrincipal.from(users.get(username)));
    }

    private User user(Long id, String username, String role, String status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setFullName("Demo " + username);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private static String loginJson(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
