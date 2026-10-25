# Database Conflict Decisions Required

## Trạng thái

`BLOCKING DATABASE CONFLICT`

Không được tạo `database/schema.sql`, `database/seed.sql`, `database/README.md` hoặc khóa `docs/DATABASE_BASELINE.md` cho đến khi người dùng xác nhận các quyết định blocking trong tài liệu này.

Source document đã được đối chiếu read-only với SHA-256:

`4696E2404518895D343B5C9E2C67D55DED669158B940A473E4E6693C917EF91B`

## Database baseline đã xác minh

ERD, Database Diagram và phần Cấu trúc bảng cùng xác nhận danh sách 11 bảng:

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

Các kết luận không cần tự thêm schema:

- Không có bảng `roles` hoặc `permissions`; role hiện nằm tại `users.role`.
- Không có bảng `inventory`; tồn hiện tại nằm tại `products.stock_quantity`.
- Không có bảng kiểm kê riêng trong ERD/Diagram/cấu trúc bảng.
- `stock_transactions` là bảng duy nhất trong baseline chính lưu lịch sử biến động kho.
- Không có bảng `invoice_details` trong baseline chính; chi tiết hàng bán nằm ở `order_details`, hóa đơn liên kết với `sales_orders`.
- Không có bảng `login_sessions`, `login_logs`, `stock_histories`, `payments`, `debts`, `discounts`, `promotions` hoặc `audit_logs` trong baseline chính.

Các kết luận trên không cho phép bỏ qua conflict ở Processing Design; những chỗ Processing Design yêu cầu bảng/field khác vẫn phải được người dùng quyết định trước khi khóa schema.

## Phân loại toàn bộ Known Inconsistencies

| ID | Phân loại | Lý do |
|---|---|---|
| INC-001 | NOT DATABASE RELATED | Nội dung/mã FR authorization; baseline đã có `users.role`, không trực tiếp quyết định cấu trúc mới. |
| INC-002 | NOT DATABASE RELATED | Mapping Use Case đăng xuất của Admin. |
| INC-003 | NOT DATABASE RELATED | Mã actor Nhân viên kho. |
| INC-004 | NON-BLOCKING DOCUMENT CONFLICT | Mapping sai FR cho cập nhật tồn; các bảng `products` và `stock_transactions` đã tồn tại. |
| INC-005 | NON-BLOCKING DOCUMENT CONFLICT | Thiếu nhóm FR Account Management; bảng `users` đã có, nhưng cách xóa tài khoản được tách thành quyết định blocking riêng bên dưới. |
| INC-006 | NON-BLOCKING DOCUMENT CONFLICT | Thiếu FR/Use Case kiểm kê; baseline không có bảng kiểm kê riêng và `stock_transactions` có loại/lý do/số lượng trước-sau. |
| INC-007 | NON-BLOCKING DOCUMENT CONFLICT | Phạm vi báo cáo rộng hơn FR; schema baseline có thể phục vụ phần báo cáo chính, còn field/bảng mở rộng thuộc INC-017. |
| INC-008 | NOT DATABASE RELATED | Spring Boot chưa được xác định. |
| INC-009 | BLOCKING DATABASE CONFLICT | `INT` và `BIGINT` làm thay đổi datatype của toàn bộ PK/FK. |
| INC-010 | BLOCKING DATABASE CONFLICT | `sales_orders.customer_id` NULL/NOT NULL và mô hình khách lẻ ảnh hưởng FK/nullability. |
| INC-011 | NOT DATABASE RELATED | Ranh giới transaction thuộc Service Layer; không dùng trigger/stored procedure. |
| INC-012 | NOT DATABASE RELATED | Thứ tự cập nhật tồn thuộc Service transaction; các bảng chính đã có. |
| INC-013 | NOT DATABASE RELATED | Actor của báo cáo doanh thu. |
| INC-014 | NON-BLOCKING DOCUMENT CONFLICT | Mã FR báo cáo tồn kho bị cắt; không làm đổi cấu trúc bảng hiện có. |
| INC-015 | NON-BLOCKING DOCUMENT CONFLICT | Hai hệ mã FR; ảnh hưởng traceability, còn schema mở rộng được xử lý ở INC-016/017. |
| INC-016 | BLOCKING DATABASE CONFLICT | Processing Design dùng bảng/tên bảng ngoài baseline 11 bảng. |
| INC-017 | BLOCKING DATABASE CONFLICT | Processing Design thêm field và nghiệp vụ có thể thay đổi table/column/relationship. |
| INC-018 | NOT DATABASE RELATED | Quyền nhập hàng của Admin thuộc authorization. |
| INC-019 | BLOCKING DATABASE CONFLICT | Gộp hay cấm sản phẩm trùng có thể yêu cầu UNIQUE trên chi tiết phiếu nhập. |
| INC-020 | BLOCKING DATABASE CONFLICT | Tiêu chí chống trùng khách hàng có thể yêu cầu UNIQUE cho phone/email/tổ hợp. |

## Quyết định 1 Kiểu dữ liệu Primary Key và Foreign Key

**CONFLICT:** Tất cả ID phải dùng `INT` hay `BIGINT`.

**SOURCE A:** Hình 3.11 ERD và phần Cấu trúc bảng dùng `BIGINT` cho các PK/FK.

**SOURCE B:** Hình 3.12 Database Diagram dùng `INT` cho các PK/FK.

**PHƯƠNG ÁN A:** Dùng `BIGINT` thống nhất cho toàn bộ PK/FK.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Khớp ERD và bảng cấu trúc chi tiết; schema, Entity và API sau này dùng miền giá trị 64-bit.

**PHƯƠNG ÁN B:** Dùng `INT` thống nhất cho toàn bộ PK/FK.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Khớp Database Diagram; schema, Entity và API sau này dùng miền giá trị 32-bit; phải coi ERD/cấu trúc bảng là cần sửa.

**KHUYẾN NGHỊ:** Phương án A (`BIGINT`) vì được thể hiện ở ERD và toàn bộ bảng cấu trúc chi tiết, nhưng không được áp dụng khi chưa có xác nhận.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Chọn `BIGINT` hay `INT` cho tất cả PK/FK.

## Quyết định 2 sales_orders customer_id và khách mua lẻ

**CONFLICT:** `sales_orders.customer_id` có cho NULL hay không.

**SOURCE A:** Business Rule cho phép khách mua lẻ không chọn khách hàng; Processing Design ghi `Customer ID hoặc khách lẻ`; Database Diagram không hiển thị NN cho `customer_id`.

**SOURCE B:** ERD và bảng Cấu trúc `sales_orders` ghi `customer_id` không cho NULL.

**PHƯƠNG ÁN A:** Cho `sales_orders.customer_id` NULL.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Lưu được đơn khách lẻ mà không tạo customer giả; FK chỉ được kiểm tra khi có giá trị; cần sửa biểu diễn NOT NULL trong ERD/cấu trúc bảng.

**PHƯƠNG ÁN B:** Giữ `sales_orders.customer_id NOT NULL`.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Mọi đơn phải có customer; cần Business Rule chính thức cho khách lẻ, ví dụ bắt buộc tạo customer, nhưng báo cáo hiện không quy định customer mặc định.

**KHUYẾN NGHỊ:** Phương án A (NULL) để khớp rule khách mua lẻ và tránh tự tạo customer mặc định.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Chọn NULL hay NOT NULL và xác nhận cách biểu diễn khách lẻ.

## Quyết định 3 invoice_details

**CONFLICT:** Có tạo bảng chi tiết hóa đơn riêng hay không.

**SOURCE A:** Baseline chính có đúng 11 bảng, không có `invoice_details`; `order_details` lưu các dòng hàng và `invoices.order_id` liên kết hóa đơn với đơn hàng.

**SOURCE B:** Processing Design ghi `CHI_TIET_HOA_DON`, `Invoice_Detail`, `Invoice_Details` và truy vấn `Invoices` cùng `Invoice_Details`.

**PHƯƠNG ÁN A:** Không tạo `invoice_details`; hóa đơn lấy chi tiết qua `invoices -> sales_orders -> order_details`.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Giữ đúng 11 bảng và cardinality hiện tại; Processing Design phải được sửa để dùng `order_details`.

**PHƯƠNG ÁN B:** Thêm `invoice_details`.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Thành bảng thứ 12, cần định nghĩa PK/FK/column/UNIQUE/cardinality và sửa ERD, Database Diagram, bảng cấu trúc, FR và traceability.

**KHUYẾN NGHỊ:** Phương án A; không thêm bảng ngoài baseline 11 bảng.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Xác nhận bỏ `invoice_details` khỏi schema hay mở change request sửa toàn bộ tài liệu.

## Quyết định 4 login sessions và login logs

**CONFLICT:** Có tạo bảng phiên và nhật ký đăng nhập hay không.

**SOURCE A:** Baseline 11 bảng chỉ có `users`; cấu trúc `users` không có `so_lan_sai`, `khoa_den`, `lan_dang_nhap_cuoi`; không có bảng session/log.

**SOURCE B:** Processing Design yêu cầu `PHIEN_DANG_NHAP`, `NHAT_KY_DANG_NHAP`, token hash, hết hạn phiên, IP, số lần sai và thời gian khóa.

**PHƯƠNG ÁN A:** Không tạo `login_sessions`/`login_logs` và không thêm các field khóa phiên ở BƯỚC 3.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Giữ 11 bảng; các rule khóa phiên/audit chưa được triển khai cho đến khi có FR/schema chính thức.

**PHƯƠNG ÁN B:** Thêm bảng và field theo Processing Design.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Tăng số bảng/column, cần tên tiếng Anh chuẩn, datatype, PK/FK, retention và security rule chưa có trong cấu trúc chính.

**KHUYẾN NGHỊ:** Phương án A; không thêm `login_sessions`, `login_logs` hoặc field chưa thuộc baseline.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Giữ strict 11-table baseline hay mở change request cho session/login audit.

## Quyết định 5 stock_transactions hay stock_histories THE_KHO

**CONFLICT:** Bảng nào là nguồn lịch sử biến động kho duy nhất.

**SOURCE A:** ERD/Diagram/cấu trúc bảng có `stock_transactions` với product, người thực hiện, phiếu nhập/đơn hàng, loại giao dịch, lượng thay đổi, tồn trước/sau, lý do và thời gian.

**SOURCE B:** Processing Design ghi `THE_KHO` và `Stock_Histories` với tên/field khác.

**PHƯƠNG ÁN A:** Chỉ dùng `stock_transactions`; không tạo `stock_histories`/`THE_KHO`.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Giữ 11 bảng và dùng một nguồn lịch sử thống nhất; Processing Design phải đổi tên/mapping.

**PHƯƠNG ÁN B:** Thêm hoặc thay `stock_transactions` bằng bảng lịch sử khác.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Thay đổi số lượng/tên bảng, field, FK và traceability; có nguy cơ hai nguồn lịch sử trùng lặp.

**KHUYẾN NGHỊ:** Phương án A vì `stock_transactions` đã đủ dữ liệu cho nhập, bán, xuất, điều chỉnh và kiểm kê.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Xác nhận `stock_transactions` là bảng lịch sử kho duy nhất.

## Quyết định 6 Các field nghiệp vụ chỉ có trong Processing Design

**CONFLICT:** Có thêm field cho VAT, discount, debt, average cost, payment/credit, login lock/session và profit hay không.

**SOURCE A:** Cấu trúc 11 bảng chính không có `discount`, `vat`, `cong_no`, `gia_von_bq`, `so_lan_sai`, `khoa_den`, `lan_dang_nhap_cuoi`, token/session fields hoặc COGS/profit fields.

**SOURCE B:** Processing Design sử dụng các field/công thức trên trong đăng nhập, nhập hàng, bán hàng và báo cáo.

**PHƯƠNG ÁN A:** Không thêm bất kỳ field/bảng nào chỉ xuất hiện trong Processing Design.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Schema giữ đúng cấu trúc bảng chính; các nghiệp vụ mở rộng không được triển khai cho đến khi tài liệu đồng bộ.

**PHƯƠNG ÁN B:** Bổ sung field/bảng tương ứng.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Thay đổi schema và business scope; cần định nghĩa chính xác datatype, NULL/default, constraint, quan hệ, FR và test cho từng field.

**KHUYẾN NGHỊ:** Phương án A tại baseline hiện tại.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Xác nhận loại khỏi schema BƯỚC 3 hay cung cấp change request cụ thể cho từng field.

## Quyết định 7 Xử lý sản phẩm trùng trong một phiếu nhập

**CONFLICT:** Gộp dòng trùng ở Service hay cấm trùng bằng constraint.

**SOURCE A:** Business Rule nói hệ thống “cần gộp hoặc không cho phép trùng tùy thiết kế đã chọn”.

**SOURCE B:** Cấu trúc `purchase_receipt_details` không ghi UNIQUE trên `(purchase_receipt_id, product_id)` và không chốt phương án.

**PHƯƠNG ÁN A:** Gộp sản phẩm trùng ở Service; không thêm composite UNIQUE.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Schema cho phép nhiều dòng cùng sản phẩm nếu Service sai; rule phụ thuộc validation/transaction ở backend.

**PHƯƠNG ÁN B:** Cấm trùng bằng `UNIQUE (purchase_receipt_id, product_id)` và validation Service.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Thêm UNIQUE chưa có trong bảng cấu trúc chính; bảo vệ dữ liệu ở database nhưng thay đổi constraint baseline.

**KHUYẾN NGHỊ:** Cần người dùng chọn; không thể tự thêm hoặc bỏ composite UNIQUE.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Chọn gộp hay cấm trùng.

## Quyết định 8 Tiêu chí chống trùng khách hàng

**CONFLICT:** Phone, email hay tổ hợp nào xác định khách hàng trùng và có cần UNIQUE hay không.

**SOURCE A:** Business Rule yêu cầu không tạo khách hàng trùng theo tiêu chí do hệ thống quy định, ví dụ phone hoặc email.

**SOURCE B:** Cấu trúc `customers` cho phone/email NULL và không đánh dấu UNIQUE.

**PHƯƠNG ÁN A:** Không thêm UNIQUE ở database; Service kiểm tra theo tiêu chí sẽ được quyết định sau.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Khớp cấu trúc bảng hiện tại nhưng database không tự ngăn trùng; chưa đủ để khóa rule chống trùng.

**PHƯƠNG ÁN B:** Thêm UNIQUE cho phone, email hoặc một tổ hợp cụ thể.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Thay đổi constraint; cần quyết định cách xử lý nhiều NULL, chuẩn hóa chuỗi và dữ liệu khách không có phone/email.

**KHUYẾN NGHỊ:** Người dùng phải xác định tiêu chí trước; không tự thêm UNIQUE.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Chọn field/tổ hợp chống trùng và chính sách NULL, hoặc xác nhận chỉ kiểm tra ở Service.

## Quyết định 9 Xóa tài khoản vật lý hay thay đổi status

**CONFLICT:** Hành vi “Xóa tài khoản” dùng DELETE vật lý hay vô hiệu hóa qua `users.status`.

**SOURCE A:** Bảng chức năng Quản lý gọi nghiệp vụ “Xóa tài khoản”, không cho xóa tài khoản đang đăng nhập hoặc đã phát sinh giao dịch.

**SOURCE B:** Bảng `users` có cột `status`; nhiều bảng có FK `created_by` tới `users`; báo cáo không quy định ON DELETE hoặc soft-delete rõ ràng.

**PHƯƠNG ÁN A:** Xóa vật lý khi tài khoản chưa có giao dịch; các FK dùng RESTRICT/NO ACTION.

**ẢNH HƯỞNG PHƯƠNG ÁN A:** Bản ghi chưa được tham chiếu có thể bị xóa; cần Service kiểm tra phiên và dependency; lịch sử tài khoản bị mất.

**PHƯƠNG ÁN B:** Không DELETE; đổi `users.status` để vô hiệu hóa.

**ẢNH HƯỞNG PHƯƠNG ÁN B:** Giữ audit/history và FK ổn định nhưng thay đổi ý nghĩa nghiệp vụ “Xóa” thành soft delete.

**KHUYẾN NGHỊ:** Phương án B về an toàn/audit, nhưng đây là thay đổi Business Rule nếu chưa được xác nhận.

**QUYẾT ĐỊNH CẦN NGƯỜI DÙNG XÁC NHẬN:** Chọn xóa vật lý có điều kiện hay thay đổi status.

## Các kiểm tra đặc biệt không tạo thêm decision

- **Role:** `users.role`; không có căn cứ tạo bảng `roles`/`permissions`.
- **Inventory:** `products.stock_quantity`; không có căn cứ tạo bảng `inventory`.
- **Kiểm kê:** không có bảng riêng; cấu trúc `stock_transactions` đủ trường để ghi loại điều chỉnh, tồn trước/sau, lý do, người thực hiện và thời gian. Conflict FR/Use Case vẫn mở nhưng không tự tạo bảng.
- **Invoice relationship:** `invoices.order_id` là FK và được ghi UNIQUE trong cấu trúc chính, thể hiện một đơn hàng tối đa một hóa đơn.
- **Trigger/stored procedure:** không tạo; cập nhật tồn kho phải do Service Layer điều phối trong transaction ở bước backend.

## Trạng thái quyết định

`OPEN — REQUIRE USER DECISION`
