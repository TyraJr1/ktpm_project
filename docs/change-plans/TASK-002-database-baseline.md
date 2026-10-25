# TASK 002 Database Baseline

## Mục tiêu

Phân tích và khóa một database baseline vật lý duy nhất cho hệ thống quản lý bán hàng, chỉ khi mọi quyết định ảnh hưởng schema đã được xác định chắc chắn từ source document hoặc được người dùng xác nhận.

## Source of Truth

`D:\ktpm_project\docs\report_KTPM_chương_1-2-3 up(tam).docx`

Tài liệu Word chỉ được đọc để đối chiếu; không sửa, đổi tên, ghi đè hoặc lưu lại.

## Requirement liên quan

- FR-AUTH-01 đến FR-AUTH-04.
- FR-PRO-01 đến FR-PRO-06.
- FR-INV-01 đến FR-INV-06.
- FR-SUP-01 đến FR-SUP-03.
- FR-CUS-01 đến FR-CUS-04.
- FR-SAL-01 đến FR-SAL-07.
- FR-BIL-01 đến FR-BIL-04.
- FR-RPT-01 đến FR-RPT-05.

## Business Rule liên quan

- Username, product code, category name, supplier code, order code, receipt code và invoice code tuân theo các quy tắc duy nhất đã nêu trong báo cáo.
- Password chỉ lưu dưới dạng `password_hash`, không lưu plain text.
- Số lượng tồn kho không âm; số lượng nghiệp vụ phải dương và không vượt tồn hiện tại khi xuất/bán.
- Tiền sử dụng DECIMAL.
- Phiếu nhập, đơn hàng, hóa đơn và biến động tồn kho phải liên kết tới bản ghi tồn tại.
- Tăng/giảm tồn kho do Service Layer điều phối trong transaction; không dùng trigger hoặc stored procedure cho business rule.
- `products.stock_quantity` lưu tồn hiện tại; `stock_transactions` lưu lịch sử biến động.
- Hóa đơn liên kết đúng đơn hàng; báo cáo mô tả quan hệ một đơn hàng/một hóa đơn qua `invoices.order_id UNIQUE`.

## Các bảng liên quan

Database baseline dự kiến có đúng 11 bảng, chờ kết quả phân tích conflict:

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

Không tự thêm bảng thứ 12.

## Các conflict database cần kiểm tra

- PK/FK dùng `INT` hay `BIGINT`.
- `sales_orders.customer_id` là NULL hay NOT NULL và cách biểu diễn khách mua lẻ.
- Có hay không bảng `roles`, `permissions` riêng.
- Có hay không bảng `inventory` riêng.
- Có hay không `invoice_details`.
- Có hay không `login_sessions`, `login_logs`.
- Có hay không `stock_histories`; quan hệ với `stock_transactions`.
- Kiểm kê dùng bảng riêng hay ghi vào `stock_transactions`.
- Xóa tài khoản vật lý hay đổi `users.status`.
- Field xuất hiện ở Activity/Processing Design nhưng không có trong cấu trúc 11 bảng.
- Unique constraint cho tiêu chí chống trùng khách hàng.
- Có áp dụng unique theo dòng chi tiết phiếu nhập hay xử lý trùng ở Service Layer.

## Phạm vi ảnh hưởng

- Phân loại toàn bộ conflict trong `docs/KNOWN_INCONSISTENCIES.md` theo ảnh hưởng database.
- Nếu có blocking conflict chưa được quyết định: chỉ tạo `docs/DB_CONFLICT_DECISIONS_REQUIRED.md` và dừng trước schema.
- Nếu không còn blocking conflict: tạo `database/schema.sql`, `database/seed.sql`, `database/README.md`, `docs/DATABASE_BASELINE.md` và cập nhật cột Database trong `docs/TRACEABILITY_MATRIX.md`.

## File dự kiến thay đổi

- `docs/change-plans/TASK-002-database-baseline.md`
- `docs/DB_CONFLICT_DECISIONS_REQUIRED.md` nếu bị chặn
- Hoặc các file database baseline được phép nếu không còn conflict chặn

## Entity

NONE tại BƯỚC 3.

## DTO

NONE tại BƯỚC 3.

## Service

NONE tại BƯỚC 3.

## Repository

NONE tại BƯỚC 3.

## Database Impact

Chưa thực hiện. Không tạo schema cho đến khi hoàn tất Impact Analysis và không còn blocking database conflict chưa được người dùng quyết định.

## Backend Impact

NONE tại bước này.

## API Impact

NONE.

## UI Impact

NONE.

## Test cần thực hiện

- Đối chiếu 11 bảng với REPORT_BASELINE, ERD, Database Diagram và cấu trúc bảng trong Word.
- Kiểm tra table/column/datatype/PK/FK/UNIQUE/NULL/cardinality.
- Kiểm tra dependency và thứ tự tạo bảng nếu được phép tạo schema.
- Kiểm tra tĩnh SQL; runtime MySQL chỉ thực hiện an toàn nếu môi trường có sẵn.
- Xác minh Word, backend và frontend không thay đổi.
- Chạy `git status` và `git diff -- database docs`.

## Regression Test

- Xác minh các tài liệu baseline BƯỚC 2 không bị thay đổi ngoài phần được yêu cầu.
- Nếu matrix được cập nhật, API, Service, Repository, UI và Test vẫn phải là `TODO`.

## Các phần tuyệt đối không sửa

- `D:\ktpm_project\docs\report_KTPM_chương_1-2-3 up(tam).docx`
- `backend/`
- `frontend/`
- Requirement, Business Rule, Use Case, Activity Diagram, ERD hoặc Database Diagram chưa có quyết định của người dùng
- Migration cũ hoặc database hiện có

## Rủi ro

- Chọn sai datatype ID hoặc nullability sẽ khóa contract sai cho toàn bộ backend.
- Tự hợp nhất Processing Design với cấu trúc bảng chính có thể tạo bảng/field ngoài baseline.
- Tự chọn cách lưu khách mua lẻ, kiểm kê hoặc xóa tài khoản sẽ thay đổi Business Rule.

## Kết quả

`BLOCKED — DATABASE DECISION REQUIRED`

- Đã đối chiếu trực tiếp source Word, REPORT_BASELINE, TRACEABILITY_MATRIX và KNOWN_INCONSISTENCIES.
- Đã xác minh baseline chính có đúng 11 bảng.
- Đã phân loại toàn bộ 20 known inconsistencies theo ảnh hưởng database.
- Đã xác định blocking conflict về datatype ID, nullability của `sales_orders.customer_id`, các bảng/field ngoài baseline trong Processing Design, UNIQUE cho dữ liệu trùng và chiến lược xóa tài khoản.
- Đã tạo `docs/DB_CONFLICT_DECISIONS_REQUIRED.md` với các phương án và tác động.
- Không tạo schema, seed, database README hoặc DATABASE_BASELINE khi chưa có quyết định của người dùng.
- Backend impact, API impact và UI impact: `NONE`.
