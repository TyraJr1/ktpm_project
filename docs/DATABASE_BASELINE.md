# Database Baseline

## Trạng thái

`LOCKED — DATABASE DECISIONS RESOLVED`

- Source document: `docs/report_KTPM_chương_1-2-3 up(tam).docx`.
- Database platform: MySQL.
- Database triển khai kỹ thuật: `ktpm_sales_db`.
- Số bảng: 11.
- Character set: `utf8mb4`.
- Storage engine: `InnoDB`.
- PK/FK: `BIGINT`; khóa chính được triển khai bằng `AUTO_INCREMENT`.
- Tiền: `DECIMAL(15,2)`.
- Không có bảng ngoài danh sách đã khóa.

## Quyết định đã khóa

| Decision | Kết quả |
|---|---|
| DB-DECISION-01 | PK/FK dùng `BIGINT` thống nhất. |
| DB-DECISION-02 | `sales_orders.customer_id` cho phép NULL. |
| DB-DECISION-03 | Không có `invoice_details`; chi tiết hóa đơn đi qua `order_details`. |
| DB-DECISION-04 | Không có `login_sessions`, `login_logs` hoặc field khóa phiên. |
| DB-DECISION-05 | `stock_transactions` là nguồn lịch sử kho duy nhất. |
| DB-DECISION-06 | Không bổ sung field chỉ xuất hiện trong Processing Design. |
| DB-DECISION-07 | UNIQUE `(purchase_receipt_id, product_id)`. |
| DB-DECISION-08 | Không đặt UNIQUE cho `customers.phone` hoặc `customers.email`. |
| DB-DECISION-09 | Tài khoản được vô hiệu hóa qua `users.status`, không DELETE vật lý. |

## 1. `users`

- Mục đích: lưu tài khoản đăng nhập, role và trạng thái.
- Primary Key: `user_id`.
- Foreign Key: không có.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `user_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `username` | VARCHAR(100) | NOT NULL | UNIQUE |
| `password_hash` | VARCHAR(255) | NOT NULL | — |
| `full_name` | VARCHAR(150) | NOT NULL | — |
| `email` | VARCHAR(150) | NULL | — |
| `phone` | VARCHAR(20) | NULL | — |
| `role` | VARCHAR(30) | NOT NULL | — |
| `status` | VARCHAR(20) | NOT NULL | — |
| `created_at` | DATETIME | NULL | — |
| `updated_at` | DATETIME | NULL | — |

- Relationship: được tham chiếu bởi `sales_orders.created_by`, `purchase_receipts.created_by`, `stock_transactions.created_by`.
- FR: FR-AUTH-01, FR-AUTH-02, FR-AUTH-03, FR-AUTH-04.
- Business Rule: username duy nhất; password không lưu plain text; role lưu trực tiếp; tài khoản bị vô hiệu hóa bằng `status`, không DELETE vật lý.

## 2. `categories`

- Mục đích: lưu danh mục sản phẩm.
- Primary Key: `category_id`.
- Foreign Key: không có.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `category_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `category_name` | VARCHAR(150) | NOT NULL | UNIQUE |
| `description` | VARCHAR(255) | NULL | — |
| `status` | VARCHAR(20) | NOT NULL | — |

- Relationship: một category có nhiều `products`.
- FR: FR-PRO-06.
- Business Rule: tên danh mục không rỗng và không trùng; danh mục gán cho sản phẩm phải tồn tại và hoạt động.

## 3. `products`

- Mục đích: lưu sản phẩm và số lượng tồn hiện tại.
- Primary Key: `product_id`.
- Foreign Key: `category_id → categories.category_id`.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `product_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `category_id` | BIGINT | NOT NULL | FK |
| `product_code` | VARCHAR(50) | NOT NULL | UNIQUE |
| `product_name` | VARCHAR(200) | NOT NULL | — |
| `sale_price` | DECIMAL(15,2) | NOT NULL | — |
| `stock_quantity` | INT | NOT NULL | — |
| `status` | VARCHAR(20) | NOT NULL | — |
| `description` | VARCHAR(500) | NULL | — |
| `created_at` | DATETIME | NULL | — |
| `updated_at` | DATETIME | NULL | — |

- Relationship: thuộc một category; được tham chiếu bởi order details, purchase receipt details và stock transactions.
- FR: FR-PRO-01 đến FR-PRO-06; FR-INV-01, FR-INV-03 đến FR-INV-06; FR-SAL-02, FR-SAL-03, FR-SAL-07; FR-RPT-05.
- Business Rule: mã duy nhất; giá bán không âm; tồn hiện tại nằm tại `stock_quantity` và không âm; chỉ sản phẩm hoạt động được bán.

## 4. `suppliers`

- Mục đích: lưu nhà cung cấp.
- Primary Key: `supplier_id`.
- Foreign Key: không có.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `supplier_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `supplier_code` | VARCHAR(50) | NOT NULL | UNIQUE |
| `supplier_name` | VARCHAR(200) | NOT NULL | — |
| `phone` | VARCHAR(20) | NULL | — |
| `email` | VARCHAR(150) | NULL | — |
| `address` | VARCHAR(255) | NULL | — |
| `status` | VARCHAR(20) | NOT NULL | — |

- Relationship: một supplier có nhiều `purchase_receipts`.
- FR: FR-SUP-01, FR-SUP-02, FR-SUP-03; FR-INV-02.
- Business Rule: mã duy nhất; tên bắt buộc; nhà cung cấp dùng trong phiếu nhập phải tồn tại và hoạt động.

## 5. `customers`

- Mục đích: lưu dữ liệu khách hàng; không phải tài khoản đăng nhập.
- Primary Key: `customer_id`.
- Foreign Key: không có.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `customer_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `customer_name` | VARCHAR(150) | NOT NULL | — |
| `phone` | VARCHAR(20) | NULL | không UNIQUE |
| `email` | VARCHAR(150) | NULL | không UNIQUE |
| `address` | VARCHAR(255) | NULL | — |
| `created_at` | DATETIME | NULL | — |

- Relationship: một customer có thể có nhiều `sales_orders`; đơn khách lẻ có thể không tham chiếu customer.
- FR: FR-CUS-01 đến FR-CUS-04; FR-SAL-01.
- Business Rule: tên bắt buộc; phone/email đúng định dạng nếu có; tiêu chí chống trùng chưa được chốt nên chưa tạo UNIQUE.

## 6. `sales_orders`

- Mục đích: lưu đơn hàng bán.
- Primary Key: `order_id`.
- Foreign Key: `customer_id → customers.customer_id` (nullable); `created_by → users.user_id`.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `order_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `customer_id` | BIGINT | NULL | FK |
| `created_by` | BIGINT | NOT NULL | FK |
| `order_code` | VARCHAR(50) | NOT NULL | UNIQUE |
| `order_date` | DATETIME | NOT NULL | — |
| `total_amount` | DECIMAL(15,2) | NOT NULL | — |
| `status` | VARCHAR(20) | NOT NULL | — |
| `note` | VARCHAR(500) | NULL | — |
| `created_at` | DATETIME | NULL | — |

- Relationship: thuộc người tạo và optional customer; có nhiều `order_details` và `stock_transactions`; có tối đa một `invoice`.
- FR: FR-CUS-04; FR-SAL-01 đến FR-SAL-07; FR-BIL-01 đến FR-BIL-04; FR-RPT-01 đến FR-RPT-04.
- Business Rule: khách mua lẻ không bắt buộc chọn customer; mã đơn duy nhất; tổng đơn do hệ thống tính.

## 7. `order_details`

- Mục đích: lưu các dòng sản phẩm của đơn hàng.
- Primary Key: `order_detail_id`.
- Foreign Key: `order_id → sales_orders.order_id`; `product_id → products.product_id`.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `order_detail_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `order_id` | BIGINT | NOT NULL | FK |
| `product_id` | BIGINT | NOT NULL | FK |
| `quantity` | INT | NOT NULL | — |
| `unit_price` | DECIMAL(15,2) | NOT NULL | — |
| `line_total` | DECIMAL(15,2) | NOT NULL | — |

- Relationship: thuộc một sales order và một product; đồng thời là nguồn chi tiết hàng hóa cho invoice qua sales order.
- FR: FR-CUS-04; FR-SAL-01 đến FR-SAL-05; FR-BIL-04; FR-RPT-04.
- Business Rule: số lượng nguyên dương và không vượt tồn; thành tiền bằng đơn giá nhân số lượng.

## 8. `purchase_receipts`

- Mục đích: lưu phiếu nhập hàng.
- Primary Key: `purchase_receipt_id`.
- Foreign Key: `supplier_id → suppliers.supplier_id`; `created_by → users.user_id`.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `purchase_receipt_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `supplier_id` | BIGINT | NOT NULL | FK |
| `created_by` | BIGINT | NOT NULL | FK |
| `receipt_code` | VARCHAR(50) | NOT NULL | UNIQUE |
| `receipt_date` | DATETIME | NOT NULL | — |
| `total_cost` | DECIMAL(15,2) | NULL | — |
| `status` | VARCHAR(20) | NOT NULL | — |
| `note` | VARCHAR(500) | NULL | — |
| `created_at` | DATETIME | NULL | — |

- Relationship: thuộc supplier và người tạo; có nhiều `purchase_receipt_details` và có thể được tham chiếu bởi stock transactions.
- FR: FR-INV-02, FR-INV-03; FR-SUP-03.
- Business Rule: phiếu cần nhà cung cấp hợp lệ, ít nhất một sản phẩm; phiếu chưa xác nhận không đổi tồn; mã phiếu duy nhất.

## 9. `purchase_receipt_details`

- Mục đích: lưu các dòng sản phẩm của phiếu nhập.
- Primary Key: `purchase_receipt_detail_id`.
- Foreign Key: `purchase_receipt_id → purchase_receipts.purchase_receipt_id`; `product_id → products.product_id`.
- Unique: `(purchase_receipt_id, product_id)`.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `purchase_receipt_detail_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `purchase_receipt_id` | BIGINT | NOT NULL | FK, composite UNIQUE |
| `product_id` | BIGINT | NOT NULL | FK, composite UNIQUE |
| `quantity` | INT | NOT NULL | — |
| `purchase_price` | DECIMAL(15,2) | NOT NULL | — |
| `line_total` | DECIMAL(15,2) | NULL | — |

- Relationship: thuộc một purchase receipt và một product.
- FR: FR-INV-02, FR-INV-03.
- Business Rule: số lượng nhập dương; giá nhập không âm; không lặp cùng sản phẩm trong một phiếu; Service sau này phải validation trước khi lưu.

## 10. `stock_transactions`

- Mục đích: nguồn lịch sử biến động tồn kho duy nhất.
- Primary Key: `stock_transaction_id`.
- Foreign Key: `product_id → products.product_id`; `created_by → users.user_id`; `purchase_receipt_id → purchase_receipts.purchase_receipt_id` (nullable); `order_id → sales_orders.order_id` (nullable).

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `stock_transaction_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `product_id` | BIGINT | NOT NULL | FK |
| `created_by` | BIGINT | NOT NULL | FK |
| `purchase_receipt_id` | BIGINT | NULL | FK |
| `order_id` | BIGINT | NULL | FK |
| `transaction_type` | VARCHAR(30) | NOT NULL | — |
| `quantity_change` | INT | NOT NULL | — |
| `quantity_before` | INT | NOT NULL | — |
| `quantity_after` | INT | NOT NULL | — |
| `reason` | VARCHAR(500) | NULL | — |
| `transaction_date` | DATETIME | NOT NULL | — |

- Relationship: thuộc product và người thực hiện; có thể tham chiếu phiếu nhập hoặc đơn hàng.
- FR: FR-INV-03 đến FR-INV-06; FR-SAL-07.
- Business Rule: mọi thay đổi tồn phải lưu loại, lượng trước/sau, thời gian, người thực hiện và lý do nếu có; không tạo `stock_histories`.

## 11. `invoices`

- Mục đích: lưu hóa đơn của giao dịch bán hàng.
- Primary Key: `invoice_id`.
- Foreign Key: `order_id → sales_orders.order_id`.
- Unique: `order_id`, `invoice_code`.

| Column | Datatype | Nullability | Constraint |
|---|---|---|---|
| `invoice_id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT |
| `order_id` | BIGINT | NOT NULL | FK, UNIQUE |
| `invoice_code` | VARCHAR(50) | NOT NULL | UNIQUE |
| `invoice_date` | DATETIME | NOT NULL | — |
| `total_amount` | DECIMAL(15,2) | NOT NULL | — |
| `payment_method` | VARCHAR(50) | NULL | — |
| `status` | VARCHAR(20) | NOT NULL | — |
| `created_at` | DATETIME | NULL | — |

- Relationship: một sales order có tối đa một invoice; chi tiết hóa đơn lấy qua `sales_orders → order_details`.
- FR: FR-BIL-01 đến FR-BIL-04; FR-RPT-01 đến FR-RPT-04.
- Business Rule: chỉ tạo từ đơn đã xác nhận; tổng hóa đơn bằng tổng đơn; mã hóa đơn duy nhất; không có `invoice_details`.

## Relationship đã khóa

1. `categories` 1–N `products`.
2. `users` 1–N `sales_orders`.
3. `customers` 1–N `sales_orders`, với FK phía đơn hàng cho phép NULL.
4. `sales_orders` 1–N `order_details`.
5. `products` 1–N `order_details`.
6. `suppliers` 1–N `purchase_receipts`.
7. `users` 1–N `purchase_receipts`.
8. `purchase_receipts` 1–N `purchase_receipt_details`.
9. `products` 1–N `purchase_receipt_details`.
10. `products` 1–N `stock_transactions`.
11. `users` 1–N `stock_transactions`.
12. `sales_orders` 1–N `stock_transactions`.
13. `purchase_receipts` 1–N `stock_transactions`.
14. `sales_orders` 1–0..1 `invoices` qua `invoices.order_id UNIQUE`.

## Ngoài baseline

Không tạo `roles`, `permissions`, `inventory`, `invoice_details`, `login_sessions`, `login_logs`, `stock_histories`, `payments`, `debts`, `discounts`, `promotions`, `THE_KHO`, `CHI_TIET_HOA_DON` hoặc bảng thứ 12.

