# Traceability Matrix

## Quy ước

- Nguồn Requirement là bộ Functional Requirement chính tại Chương II.
- Cột Business Rule chỉ dùng quy tắc đã được ghi trong báo cáo; dấu `—` nghĩa là báo cáo không nêu quy tắc riêng ngoài nội dung FR.
- Các Use Case có ID trùng giữa actor được ghi kèm actor.
- `TODO` ở API, Service, Repository, UI và Test là bắt buộc tại BƯỚC 2.
- `SPECIFIED`: các nguồn chính đang đủ nhất quán để làm baseline.
- `INCONSISTENT`: có conflict mở; không được tự chọn một phía để triển khai.
- Cột `Database (LOCKED)` được khóa theo `docs/DATABASE_BASELINE.md` và các quyết định DB-DECISION-01..09. Việc khóa cột Database không thay đổi trạng thái nghiệp vụ; API, Service, Repository, UI và Test vẫn là `TODO`.
- Backend foundation BƯỚC 4: `PARTIAL — COMPILE/TEST/NON-DB STARTUP PASS; MYSQL/JPA/FLYWAY RUNTIME NOT VERIFIED`. Ghi chú infrastructure này không đánh dấu bất kỳ FR nghiệp vụ nào là DONE.
- Authentication BƯỚC 5: implementation, automated test và MySQL/Flyway/JPA/API integration thật đã PASS; FR-AUTH-01..04 đạt trạng thái `DONE`.

| Requirement | Business Rule | Use Case | Activity Diagram | Actor | Database (LOCKED) | API | Service | Repository | UI | Test | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| FR-AUTH-01 | Username/password không rỗng; tài khoản tồn tại, hoạt động; password khớp; password không lưu plain text | Admin UC-01; Bán hàng UC-01; Kho UC-01 | Đăng nhập Hình 3.5 | Cả ba actor | `users` | `POST /api/auth/login` | `AuthenticationService` | `UserRepository.findByUsername` | `LoginPage`, API client, `authStore` | PASS (unit + MockMvc + MySQL API + Edge browser integration) | DONE |
| FR-AUTH-02 | Kết thúc phiên đăng nhập | Admin UC-02; Bán hàng UC-02; Kho UC-02 | — | Cả ba actor | `users`; không session/blacklist | `POST /api/auth/logout` | Stateless acknowledgement; client xóa token | N/A (stateless) | `AppShell` logout + xóa `authStore` | PASS (unit + MockMvc + MySQL API + Edge browser integration) | DONE |
| FR-AUTH-03 | Quyền được xác định theo role | Admin UC-03; Bán hàng UC-03; Kho UC-03; Admin UC-02 bị map nhầm | Đăng nhập Hình 3.5 | Cả ba actor | `users.role` (lưu trực tiếp; không có bảng role) | JWT + role verification endpoints | `JwtService`, `CustomUserDetailsService` | `UserRepository.findByUsername` | Role routing, Admin Dashboard, Sales/Warehouse placeholder | PASS (unit + MockMvc + MySQL API + Edge browser integration) | DONE |
| FR-AUTH-04 | Backend từ chối chức năng không được cấp quyền | Admin UC-03; Bán hàng UC-03; Kho UC-03; Admin UC-02 bị map nhầm | Đăng nhập Hình 3.5 | Cả ba actor | `users.role`, `users.status` (không DELETE vật lý) | Security filter chain; HTTP 401/403 | JWT filter + role authorization | `UserRepository.findByUsername` | `routeGuard` + API client xử lý 401/403 | PASS (unit + MockMvc + MySQL API + Edge browser integration) | DONE |
| FR-PRO-01 | Chỉ hiển thị sản phẩm tồn tại; dữ liệu hiện tại | Admin UC-04; Bán hàng UC-04; Kho UC-18 | Bán hàng Hình 3.7; Quản lý kho Hình 3.9 | Cả ba actor | `products`, `categories` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-PRO-02 | Mã duy nhất/không rỗng; tên không rỗng; giá `>= 0`; danh mục tồn tại | Admin UC-05 | — | Quản lý/Admin | `products`, `categories` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-PRO-03 | Sản phẩm phải tồn tại; dữ liệu sản phẩm tuân theo rule sản phẩm | Admin UC-06 | — | Quản lý/Admin | `products`, `categories` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-PRO-04 | Tìm theo mã/tên; kết quả phản ánh dữ liệu hiện tại | Admin UC-07; Bán hàng UC-05; Kho UC-19 | Bán hàng Hình 3.7; Quản lý kho Hình 3.9 | Cả ba actor | `products`, `categories` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-PRO-05 | Chỉ sản phẩm hoạt động được bán | Admin UC-08 | Bán hàng Hình 3.7 | Quản lý/Admin | `products.status` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-PRO-06 | Tên danh mục không rỗng; mã nếu có duy nhất; danh mục gán phải tồn tại/hoạt động | Admin UC-09 | — | Quản lý/Admin | `categories`, `products.category_id` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-INV-01 | Tồn là dữ liệu hiện tại và không âm | Admin UC-10; Kho UC-20 | Quản lý kho Hình 3.9 | Quản lý/Admin; Nhân viên kho | `products.stock_quantity` (nguồn tồn hiện tại) | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-INV-02 | Phiếu có NCC hợp lệ, ít nhất một sản phẩm; phiếu nháp không đổi tồn | Kho UC-21 | Nhập hàng Hình 3.6 | Nhân viên kho | `purchase_receipts`, `purchase_receipt_details` với UNIQUE `(purchase_receipt_id, product_id)`, `suppliers`, `products` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-INV-03 | Phiếu chỉ xác nhận một lần; `Tồn mới = Tồn hiện tại + Số lượng nhập` | Kho UC-22 | Nhập hàng Hình 3.6 | Nhân viên kho | `purchase_receipts`, `purchase_receipt_details`, `products`, `stock_transactions` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-INV-04 | Xuất/điều chỉnh cần quyền và lý do; tồn sau thay đổi không âm | Kho UC-23; Kho UC-24 | Quản lý kho Hình 3.9 | Nhân viên kho | `products`, `stock_transactions` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-INV-05 | Lưu sản phẩm, loại, lượng thay đổi, thời gian, người thực hiện, lý do nếu có | Kho UC-24; Kho UC-25 | Quản lý kho Hình 3.9 | Nhân viên kho | `stock_transactions` (nguồn lịch sử duy nhất), `users`, `products` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-INV-06 | `0 < Số lượng xuất <= Tồn hiện tại`; không âm kho | Kho UC-23 | Quản lý kho Hình 3.9 | Nhân viên kho | `products`, `stock_transactions` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SUP-01 | Tên không rỗng; mã nếu có duy nhất; phone/email đúng định dạng | Admin UC-11 | — | Quản lý/Admin | `suppliers` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SUP-02 | Nhà cung cấp phải tồn tại; dữ liệu tuân theo rule nhà cung cấp | Admin UC-11 | — | Quản lý/Admin | `suppliers` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SUP-03 | NCC dùng trong phiếu nhập phải tồn tại và hoạt động | Admin UC-11; Kho UC-26 | Nhập hàng Hình 3.6 | Quản lý/Admin; Nhân viên kho | `suppliers`, `purchase_receipts` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-CUS-01 | Tên theo yêu cầu không rỗng; phone/email đúng định dạng; không trùng tiêu chí định danh chưa chốt | Bán hàng UC-06 | Bán hàng Hình 3.7 | Nhân viên bán hàng | `customers` (phone/email chưa có UNIQUE) | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-CUS-02 | Khách hàng phải tồn tại; thông tin hiện tại | Bán hàng UC-07 | — | Nhân viên bán hàng | `customers` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-CUS-03 | Chỉ trả dữ liệu khách hàng tồn tại | Bán hàng UC-08 | — | Nhân viên bán hàng | `customers` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-CUS-04 | Hiển thị lịch sử nếu khách hàng tồn tại và có dữ liệu mua | Bán hàng UC-09 | — | Nhân viên bán hàng | `customers`, `sales_orders`, `order_details` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SAL-01 | Đơn có ít nhất một sản phẩm, gắn nhân viên; khách hàng có thể không chọn với khách lẻ | Bán hàng UC-10 | Bán hàng Hình 3.7 | Nhân viên bán hàng | `sales_orders.customer_id NULL`, `order_details`, `users`, `customers` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-SAL-02 | Sản phẩm tồn tại/hoạt động; số lượng nguyên dương và không vượt tồn | Bán hàng UC-10 | Bán hàng Hình 3.7 | Nhân viên bán hàng | `order_details`, `products` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SAL-03 | `Quantity > 0` và `Quantity <= StockQuantity` | Bán hàng UC-11 | Bán hàng Hình 3.7 | Nhân viên bán hàng | `products.stock_quantity`, `order_details.quantity` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SAL-04 | Thành tiền dòng = Đơn giá × Số lượng | Bán hàng UC-12 | Bán hàng Hình 3.7 | Nhân viên bán hàng | `order_details.unit_price`, `quantity`, `line_total` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SAL-05 | Tổng đơn = tổng thành tiền; người dùng không nhập trực tiếp | Bán hàng UC-12 | Bán hàng Hình 3.7 | Nhân viên bán hàng | `sales_orders.total_amount`, `order_details.line_total` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-SAL-06 | Tạo đơn, tạo hóa đơn và cập nhật tồn phải cùng thành công; lỗi phải rollback | Bán hàng UC-13 | Bán hàng Hình 3.7; Lập hóa đơn Hình 3.8 | Nhân viên bán hàng | `sales_orders`, `order_details`, `invoices`, `products`, `stock_transactions` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-SAL-07 | Chỉ cập nhật tồn sau giao dịch thành công | Bán hàng UC-14 nhưng bảng ghi nhầm FR-BIL-07 | Bán hàng Hình 3.7 không thể hiện bước cập nhật | Nhân viên bán hàng | `products`, `stock_transactions`, `sales_orders` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-BIL-01 | Chỉ tạo từ đơn đã xác nhận; đúng đơn; tổng hóa đơn bằng tổng đơn; lỗi rollback | Bán hàng UC-15 | Lập hóa đơn Hình 3.8 | Nhân viên bán hàng; phần quy trình còn liệt kê Quản lý/Admin | `invoices`, `sales_orders` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-BIL-02 | Hóa đơn phải được lưu cùng giao dịch hợp lệ | Bán hàng UC-16 được map cả FR-BIL-02 và FR-BIL-03 | Lập hóa đơn Hình 3.8 | Nhân viên bán hàng | `invoices` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-BIL-03 | Tra cứu theo mã/ngày; khoảng ngày hợp lệ; theo quyền | Bán hàng UC-16 | Lập hóa đơn Hình 3.8 | Nhân viên bán hàng; Quản lý/Admin có chức năng tra cứu | `invoices`, `sales_orders` | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-BIL-04 | Hóa đơn phải tồn tại | Bán hàng UC-17 | Lập hóa đơn Hình 3.8 | Nhân viên bán hàng | `invoices → sales_orders → order_details → products` (không có `invoice_details`) | TODO | TODO | TODO | TODO | TODO | SPECIFIED |
| FR-RPT-01 | Ngày bắt đầu `<=` ngày kết thúc; người dùng có quyền báo cáo | Admin UC-12 | Báo cáo doanh thu Hình 3.10 | Quản lý/Admin theo quy trình; tiêu đề hình ghi Nhân viên bán hàng | `invoices`, `sales_orders` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-RPT-02 | Chỉ hóa đơn hợp lệ/hoàn thành; doanh thu là tổng tiền trong khoảng | Admin UC-13 | Báo cáo doanh thu Hình 3.10 | Quản lý/Admin | `invoices.total_amount`, `invoice_date`, `status` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-RPT-03 | Đếm hóa đơn hợp lệ/hoàn thành trong khoảng | Admin UC-14 | Báo cáo doanh thu Hình 3.10 | Quản lý/Admin | `invoices` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-RPT-04 | Tổng số lượng bán giảm dần từ đơn/hóa đơn hợp lệ | Admin UC-15 | Báo cáo doanh thu Hình 3.10 | Quản lý/Admin | `invoices`, `sales_orders`, `order_details`, `products` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |
| FR-RPT-05 | Tồn báo cáo là tồn hiện tại, không âm | Admin UC-16 nhưng mã FR trong bảng bị cắt thành `FR-RPT-` | Báo cáo doanh thu Hình 3.10 không có nhánh báo cáo tồn kho | Quản lý/Admin | `products.stock_quantity` | TODO | TODO | TODO | TODO | TODO | INCONSISTENT |

## Các khoảng trống truy vết không được tự lấp

- Quản lý tài khoản có trong phạm vi và bảng chức năng actor nhưng không có nhóm FR riêng.
- Kiểm kê có trong nghiệp vụ Nhân viên kho nhưng không có FR-INV và Use Case riêng.
- Các FR/BR và bảng trong phần Thiết kế xử lý dùng hệ mã/tên khác bộ baseline chính; không đưa vào matrix như requirement mới khi chưa có quyết định của người dùng.
