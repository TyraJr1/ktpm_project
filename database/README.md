# MySQL Database Baseline

## Nền tảng

- DBMS: MySQL.
- Database name: `ktpm_sales_db`.
- Logical schema: đúng 11 bảng đã khóa trong `docs/DATABASE_BASELINE.md`.
- Character set: `utf8mb4`.
- Storage engine: `InnoDB`.

Không sử dụng SQL Server và không có cú pháp `IDENTITY(1,1)`.

## Thứ tự chạy

1. Chạy `schema.sql` để tạo database và 11 bảng.
2. Chạy `seed.sql` để thêm category, product, supplier và customer demo tối thiểu.

Ví dụ bằng MySQL CLI từ thư mục project:

```cmd
mysql -u USERNAME -p < database\schema.sql
mysql -u USERNAME -p < database\seed.sql
```

Không ghi password trực tiếp vào command history hoặc source code.

## Kiểm tra 11 bảng

Sau khi kết nối MySQL:

```sql
USE ktpm_sales_db;
SHOW TABLES;
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'ktpm_sales_db'
  AND table_type = 'BASE TABLE';
```

Kết quả `table_count` phải bằng `11`, gồm:

1. `users`
2. `categories`
3. `products`
4. `suppliers`
5. `customers`
6. `sales_orders`
7. `order_details`
8. `purchase_receipts`
9. `purchase_receipt_details`
10. `stock_transactions`
11. `invoices`

## Kiểm tra constraint

```sql
SHOW CREATE TABLE sales_orders;
SHOW CREATE TABLE purchase_receipt_details;
SHOW CREATE TABLE invoices;
```

Các điểm bắt buộc:

- Mọi PK/FK ID dùng `BIGINT`.
- `sales_orders.customer_id` cho phép NULL.
- `purchase_receipt_details` có UNIQUE `(purchase_receipt_id, product_id)`.
- `invoices.order_id` là FK và UNIQUE.
- Không có UNIQUE trên `customers.phone` hoặc `customers.email`.

## An toàn

- Các script không chứa `DROP DATABASE`, `DROP TABLE`, DELETE hoặc thao tác reset dữ liệu.
- Không chạy script vào database khác nếu chưa kiểm tra connection hiện tại.
- Không sửa logical schema nếu chưa có Impact Analysis và quyết định được phê duyệt.
- `seed.sql` không tạo user và không lưu password plain text; tài khoản/BCrypt thuộc BƯỚC 5.

