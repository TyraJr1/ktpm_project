package com.ktpm.salesmanagement;

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
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EnableAutoConfiguration(excludeName = {
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
        "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
        "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class SalesManagementApplicationTests {

    private static final String TEST_SECRET = randomSecret();

    @MockitoBean
    UserRepository userRepository;
    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean ProductRepository productRepository;
    @MockitoBean SupplierRepository supplierRepository;
    @MockitoBean InvoiceRepository invoiceRepository;
    @MockitoBean OrderDetailRepository orderDetailRepository;
    @MockitoBean SalesOrderRepository salesOrderRepository;
    @MockitoBean PurchaseReceiptRepository purchaseReceiptRepository;
    @MockitoBean StockTransactionRepository stockTransactionRepository;

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add("security.jwt.secret", () -> TEST_SECRET);
    }

    @Test
    void contextLoads() {
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}

