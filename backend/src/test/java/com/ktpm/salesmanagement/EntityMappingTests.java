package com.ktpm.salesmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ktpm.salesmanagement.entity.Category;
import com.ktpm.salesmanagement.entity.Customer;
import com.ktpm.salesmanagement.entity.Invoice;
import com.ktpm.salesmanagement.entity.OrderDetail;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.entity.PurchaseReceipt;
import com.ktpm.salesmanagement.entity.PurchaseReceiptDetail;
import com.ktpm.salesmanagement.entity.SalesOrder;
import com.ktpm.salesmanagement.entity.StockTransaction;
import com.ktpm.salesmanagement.entity.Supplier;
import com.ktpm.salesmanagement.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EntityMappingTests {

    private static final Map<Class<?>, String> ENTITY_TABLES = Map.ofEntries(
            Map.entry(User.class, "users"),
            Map.entry(Category.class, "categories"),
            Map.entry(Product.class, "products"),
            Map.entry(Supplier.class, "suppliers"),
            Map.entry(Customer.class, "customers"),
            Map.entry(SalesOrder.class, "sales_orders"),
            Map.entry(OrderDetail.class, "order_details"),
            Map.entry(PurchaseReceipt.class, "purchase_receipts"),
            Map.entry(PurchaseReceiptDetail.class, "purchase_receipt_details"),
            Map.entry(StockTransaction.class, "stock_transactions"),
            Map.entry(Invoice.class, "invoices")
    );

    @Test
    void mapsExactlyElevenBaselineEntitiesWithLongIds() {
        assertEquals(11, ENTITY_TABLES.size());

        ENTITY_TABLES.forEach((entityType, tableName) -> {
            assertNotNull(entityType.getAnnotation(Entity.class));
            assertEquals(tableName, entityType.getAnnotation(Table.class).name());

            Field idField = findAnnotatedField(entityType, Id.class);
            assertEquals(Long.class, idField.getType());
        });
    }

    @Test
    void mapsLockedForeignKeyNullability() throws NoSuchFieldException {
        assertJoinColumn(Product.class, "category", "category_id", false);
        assertJoinColumn(SalesOrder.class, "customer", "customer_id", true);
        assertJoinColumn(SalesOrder.class, "createdBy", "created_by", false);
        assertJoinColumn(OrderDetail.class, "salesOrder", "order_id", false);
        assertJoinColumn(OrderDetail.class, "product", "product_id", false);
        assertJoinColumn(PurchaseReceipt.class, "supplier", "supplier_id", false);
        assertJoinColumn(PurchaseReceipt.class, "createdBy", "created_by", false);
        assertJoinColumn(PurchaseReceiptDetail.class, "purchaseReceipt", "purchase_receipt_id", false);
        assertJoinColumn(PurchaseReceiptDetail.class, "product", "product_id", false);
        assertJoinColumn(StockTransaction.class, "product", "product_id", false);
        assertJoinColumn(StockTransaction.class, "createdBy", "created_by", false);
        assertJoinColumn(StockTransaction.class, "purchaseReceipt", "purchase_receipt_id", true);
        assertJoinColumn(StockTransaction.class, "salesOrder", "order_id", true);
        assertJoinColumn(Invoice.class, "salesOrder", "order_id", false);
    }

    @Test
    void mapsMoneyAsBigDecimal() throws NoSuchFieldException {
        assertEquals(BigDecimal.class, Product.class.getDeclaredField("salePrice").getType());
        assertEquals(BigDecimal.class, SalesOrder.class.getDeclaredField("totalAmount").getType());
        assertEquals(BigDecimal.class, OrderDetail.class.getDeclaredField("unitPrice").getType());
        assertEquals(BigDecimal.class, OrderDetail.class.getDeclaredField("lineTotal").getType());
        assertEquals(BigDecimal.class, PurchaseReceipt.class.getDeclaredField("totalCost").getType());
        assertEquals(BigDecimal.class, PurchaseReceiptDetail.class.getDeclaredField("purchasePrice").getType());
        assertEquals(BigDecimal.class, PurchaseReceiptDetail.class.getDeclaredField("lineTotal").getType());
        assertEquals(BigDecimal.class, Invoice.class.getDeclaredField("totalAmount").getType());
    }

    @Test
    void mapsLockedUniqueConstraints() {
        Table receiptDetailTable = PurchaseReceiptDetail.class.getAnnotation(Table.class);
        assertEquals(1, receiptDetailTable.uniqueConstraints().length);
        assertEquals(
                java.util.List.of("purchase_receipt_id", "product_id"),
                java.util.List.of(receiptDetailTable.uniqueConstraints()[0].columnNames())
        );

        Table invoiceTable = Invoice.class.getAnnotation(Table.class);
        assertTrue(java.util.Arrays.stream(invoiceTable.uniqueConstraints())
                .anyMatch(constraint -> java.util.List.of(constraint.columnNames())
                        .equals(java.util.List.of("order_id"))));
    }

    private static Field findAnnotatedField(
            Class<?> type,
            Class<? extends java.lang.annotation.Annotation> annotationType
    ) {
        return java.util.Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotationType))
                .findFirst()
                .orElseThrow();
    }

    private static void assertJoinColumn(
            Class<?> entityType,
            String fieldName,
            String columnName,
            boolean nullable
    ) throws NoSuchFieldException {
        JoinColumn joinColumn = entityType.getDeclaredField(fieldName).getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals(columnName, joinColumn.name());
        assertEquals(nullable, joinColumn.nullable());
    }
}

