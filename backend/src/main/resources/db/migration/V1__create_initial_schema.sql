CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NULL,
    phone VARCHAR(20) NULL,
    `role` VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(150) NOT NULL,
    description VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name UNIQUE (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    product_id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    sale_price DECIMAL(15,2) NOT NULL,
    stock_quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(500) NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT uq_products_code UNIQUE (product_code),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE suppliers (
    supplier_id BIGINT NOT NULL AUTO_INCREMENT,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(150) NULL,
    address VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT pk_suppliers PRIMARY KEY (supplier_id),
    CONSTRAINT uq_suppliers_code UNIQUE (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE customers (
    customer_id BIGINT NOT NULL AUTO_INCREMENT,
    customer_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(150) NULL,
    address VARCHAR(255) NULL,
    created_at DATETIME NULL,
    CONSTRAINT pk_customers PRIMARY KEY (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sales_orders (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NULL,
    created_by BIGINT NOT NULL,
    order_code VARCHAR(50) NOT NULL,
    order_date DATETIME NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME NULL,
    CONSTRAINT pk_sales_orders PRIMARY KEY (order_id),
    CONSTRAINT uq_sales_orders_code UNIQUE (order_code),
    CONSTRAINT fk_sales_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
    CONSTRAINT fk_sales_orders_created_by
        FOREIGN KEY (created_by) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_details (
    order_detail_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,
    CONSTRAINT pk_order_details PRIMARY KEY (order_detail_id),
    CONSTRAINT fk_order_details_order
        FOREIGN KEY (order_id) REFERENCES sales_orders (order_id),
    CONSTRAINT fk_order_details_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_receipts (
    purchase_receipt_id BIGINT NOT NULL AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    receipt_code VARCHAR(50) NOT NULL,
    receipt_date DATETIME NOT NULL,
    total_cost DECIMAL(15,2) NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME NULL,
    CONSTRAINT pk_purchase_receipts PRIMARY KEY (purchase_receipt_id),
    CONSTRAINT uq_purchase_receipts_code UNIQUE (receipt_code),
    CONSTRAINT fk_purchase_receipts_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id),
    CONSTRAINT fk_purchase_receipts_created_by
        FOREIGN KEY (created_by) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_receipt_details (
    purchase_receipt_detail_id BIGINT NOT NULL AUTO_INCREMENT,
    purchase_receipt_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    purchase_price DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NULL,
    CONSTRAINT pk_purchase_receipt_details PRIMARY KEY (purchase_receipt_detail_id),
    CONSTRAINT uq_purchase_receipt_product UNIQUE (purchase_receipt_id, product_id),
    CONSTRAINT fk_purchase_receipt_details_receipt
        FOREIGN KEY (purchase_receipt_id) REFERENCES purchase_receipts (purchase_receipt_id),
    CONSTRAINT fk_purchase_receipt_details_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stock_transactions (
    stock_transaction_id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    purchase_receipt_id BIGINT NULL,
    order_id BIGINT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity_change INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reason VARCHAR(500) NULL,
    transaction_date DATETIME NOT NULL,
    CONSTRAINT pk_stock_transactions PRIMARY KEY (stock_transaction_id),
    CONSTRAINT fk_stock_transactions_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT fk_stock_transactions_created_by
        FOREIGN KEY (created_by) REFERENCES users (user_id),
    CONSTRAINT fk_stock_transactions_receipt
        FOREIGN KEY (purchase_receipt_id) REFERENCES purchase_receipts (purchase_receipt_id),
    CONSTRAINT fk_stock_transactions_order
        FOREIGN KEY (order_id) REFERENCES sales_orders (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE invoices (
    invoice_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    invoice_code VARCHAR(50) NOT NULL,
    invoice_date DATETIME NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(50) NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NULL,
    CONSTRAINT pk_invoices PRIMARY KEY (invoice_id),
    CONSTRAINT uq_invoices_order UNIQUE (order_id),
    CONSTRAINT uq_invoices_code UNIQUE (invoice_code),
    CONSTRAINT fk_invoices_order
        FOREIGN KEY (order_id) REFERENCES sales_orders (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

