USE ktpm_sales_db;

-- Dữ liệu demo tối thiểu. Không seed user vì Authentication/BCrypt thuộc BƯỚC 5.
INSERT INTO categories (category_name, description, status)
VALUES ('Bút viết', 'Danh mục dữ liệu demo', 'ACTIVE');

INSERT INTO products (
    category_id,
    product_code,
    product_name,
    sale_price,
    stock_quantity,
    status,
    description
)
SELECT
    category_id,
    'SP-DEMO-001',
    'Bút bi demo',
    5000.00,
    10,
    'ACTIVE',
    'Sản phẩm dữ liệu demo'
FROM categories
WHERE category_name = 'Bút viết'
ORDER BY category_id
LIMIT 1;

INSERT INTO suppliers (
    supplier_code,
    supplier_name,
    phone,
    email,
    address,
    status
)
VALUES (
    'NCC-DEMO-001',
    'Nhà cung cấp demo',
    NULL,
    NULL,
    NULL,
    'ACTIVE'
);

INSERT INTO customers (
    customer_name,
    phone,
    email,
    address
)
VALUES (
    'Khách hàng demo',
    NULL,
    NULL,
    NULL
);

