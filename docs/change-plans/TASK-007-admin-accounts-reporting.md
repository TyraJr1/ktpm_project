# TASK-007 — Hoàn thiện quản lý tài khoản và báo cáo Admin

## Mục tiêu

Hoàn thiện các phần Admin còn thiếu đã được người dùng quyết định ngày 02/11/2026: quản lý tài khoản trong ba role hiện có và báo cáo quản trị theo FR-RPT-01..05, kết nối qua REST API tới MySQL, không mở rộng schema hoặc nghiệp vụ ngoài phạm vi đã xác nhận.

## Requirement

- `USR-ADM-ACC-01`: Admin xem và tìm kiếm danh sách tài khoản.
- `USR-ADM-ACC-02`: Admin tạo tài khoản với username duy nhất, mật khẩu hợp lệ và một trong ba role hiện có.
- `USR-ADM-ACC-03`: Admin cập nhật thông tin, role và trạng thái tài khoản.
- `USR-ADM-ACC-04`: Admin đổi mật khẩu tài khoản.
- `USR-ADM-ACC-05`: Admin xóa tài khoản chưa phát sinh giao dịch và không phải tài khoản đang đăng nhập.
- `FR-RPT-01..05`: chọn khoảng thời gian, tổng doanh thu, số hóa đơn, sản phẩm bán chạy và báo cáo tồn kho.
- Quyết định người dùng: báo cáo chỉ dành cho `ADMIN`; hóa đơn hợp lệ có trạng thái `COMPLETED`; không thêm báo cáo lợi nhuận, theo khách hàng/nhân viên hoặc xuất PDF/Excel.

## Business Rule

- Chỉ dùng ba role `ADMIN`, `SALES_STAFF`, `WAREHOUSE_STAFF`.
- Username bắt buộc và duy nhất; mật khẩu tối thiểu 8 ký tự, có chữ hoa, chữ thường và số; chỉ lưu BCrypt hash.
- Không cho khóa, hạ quyền hoặc xóa tài khoản đang đăng nhập.
- Không xóa tài khoản đã được tham chiếu bởi đơn bán, phiếu nhập hoặc giao dịch kho.
- Ngày bắt đầu báo cáo không lớn hơn ngày kết thúc.
- Chỉ hóa đơn `COMPLETED` được tính doanh thu, số lượng hóa đơn và sản phẩm bán chạy.
- Doanh thu là tổng `invoices.total_amount`; sản phẩm bán chạy xếp giảm dần theo tổng `order_details.quantity` của các hóa đơn hợp lệ.
- Báo cáo tồn kho đọc `products.stock_quantity`, không cập nhật tồn.

## Use Case

- Chức năng Quản lý tại Bảng 2.1: tạo, sửa, xóa tài khoản và phân quyền.
- Admin UC-12 đến UC-16: chọn thời gian, doanh thu, số hóa đơn, sản phẩm bán chạy và báo cáo tồn kho.
- Không triển khai lịch sử kho cho Admin trong task này.

## Phạm vi ảnh hưởng

- Backend: DTO, Controller, Service, Repository cho tài khoản và báo cáo.
- Frontend: menu, routing, trang tài khoản, trang báo cáo, API client và Dashboard.
- Test: unit Service, authorization/API, frontend build và browser integration khi runtime sẵn sàng.
- Documentation: traceability và kết quả change plan.

## File thay đổi

- `backend/src/main/java/com/ktpm/salesmanagement/controller/`
- `backend/src/main/java/com/ktpm/salesmanagement/dto/`
- `backend/src/main/java/com/ktpm/salesmanagement/repository/`
- `backend/src/main/java/com/ktpm/salesmanagement/service/`
- `backend/src/test/java/com/ktpm/salesmanagement/`
- `frontend/src/api/client.js`
- `frontend/src/components/AppShell.js`
- `frontend/src/main.js`
- `frontend/src/pages/`
- `frontend/src/styles/main.css`
- `frontend/tests/browser-auth.mjs`
- `docs/TRACEABILITY_MATRIX.md`

## Entity

- Tái sử dụng `User`, `Invoice`, `SalesOrder`, `OrderDetail`, `Product`.
- Không đổi mapping trừ khi phát hiện sai với schema hiện hữu.

## DTO

- Request tạo/cập nhật/đổi mật khẩu tài khoản.
- Response tài khoản không chứa `password_hash`.
- Response tổng quan báo cáo, sản phẩm bán chạy và tồn kho.

## Service

- `AdminAccountService`: validation tài khoản, mã hóa mật khẩu, bảo vệ tài khoản hiện tại, kiểm tra tham chiếu trước khi xóa và transaction ghi.
- `AdminReportService`: validation khoảng ngày và điều phối truy vấn báo cáo chỉ với trạng thái `COMPLETED`.

## Repository

- Mở rộng `UserRepository` cho tìm kiếm và kiểm tra username.
- Dùng các repository giao dịch để kiểm tra tài khoản đã phát sinh dữ liệu.
- Thêm aggregate query trên `InvoiceRepository` và `OrderDetailRepository`; dùng `ProductRepository` cho tồn kho.

## Database impact

`NONE`

- Không đổi bảng, cột, kiểu dữ liệu, PK/FK, constraint hoặc migration.
- Không thêm bảng role, permission, report hoặc invoice detail.

## API impact

- `/api/admin/accounts`: danh sách, tạo, cập nhật, đổi mật khẩu và xóa.
- `/api/admin/reports/summary`: doanh thu và số hóa đơn theo khoảng ngày.
- `/api/admin/reports/top-products`: sản phẩm bán chạy theo khoảng ngày.
- `/api/admin/reports/inventory`: báo cáo tồn kho hiện tại.
- Tất cả endpoint được bảo vệ bằng role `ADMIN`.

## UI impact

- Thêm menu và route `Tài khoản`, `Báo cáo`.
- Form tài khoản không hiển thị hoặc trả password hash.
- Báo cáo dùng dữ liệu API thật, có lọc ngày, loading/empty/error; không dùng số liệu giả.

## Test

- Validation mật khẩu, role, username, self-protection và xóa tài khoản có tham chiếu.
- Aggregate báo cáo chỉ tính hóa đơn `COMPLETED`, kiểm tra khoảng ngày và thứ tự sản phẩm bán chạy.
- API 401/403 và Admin happy path.
- Frontend build; browser test gọi API thật khi MySQL/backend/frontend hoạt động.

## Regression Test

- Toàn bộ `mvn test`.
- Authentication/JWT/role guard.
- Admin category/product/supplier/inventory/invoice hiện có.
- `npm run build` và browser integration hiện có.
- Flyway/JPA validation với MySQL khi runtime sẵn sàng.

## Những phần không được sửa

- Báo cáo Word nguồn và migration cũ.
- Logical schema 11 bảng và ba role hiện có.
- Nghiệp vụ bán hàng, nhập hàng và cập nhật kho.
- Không thêm lịch sử kho cho Admin, lợi nhuận, báo cáo theo khách hàng/nhân viên hoặc export.
- Không sửa module Sales/Warehouse ngoài regression cần thiết.

## Rủi ro

- Xóa tài khoản có thể vi phạm FK nếu bỏ sót bảng tham chiếu; Service phải kiểm tra cả ba quan hệ và database vẫn là lớp bảo vệ cuối.
- Thay role/status tài khoản hiện tại có thể làm JWT còn quyền cũ; task cấm thao tác này với tài khoản đang đăng nhập.
- Dữ liệu hóa đơn hiện hữu không dùng `COMPLETED` sẽ không xuất hiện trong báo cáo theo quyết định đã khóa.
- MySQL không chạy sẽ giới hạn kết quả ở mức automated test/build và phải ghi `PARTIAL`.

## Kết quả

`PARTIAL — IMPLEMENTED; MYSQL/API/BROWSER RUNTIME NOT VERIFIED`

Đã triển khai:

- Backend và UI quản lý tài khoản: danh sách/tìm kiếm, tạo, sửa thông tin/role/status, đổi mật khẩu và xóa có điều kiện.
- Mật khẩu được kiểm tra ở DTO và Service, lưu BCrypt; response không chứa hash.
- Không cho hạ quyền/khóa/xóa tài khoản đang đăng nhập; không xóa tài khoản đã có đơn bán, phiếu nhập hoặc giao dịch kho.
- Backend và UI báo cáo: lọc khoảng ngày, doanh thu, số hóa đơn, sản phẩm bán chạy và tồn kho; chỉ invoice `COMPLETED` được tính.
- Toàn bộ endpoint mới nằm dưới `/api/admin/**` và chỉ role `ADMIN` truy cập.
- Không thay đổi database schema, migration, bảng, cột hoặc role.

Đã xác minh:

- Maven compile và 42 automated tests: PASS.
- Unit test business rule tài khoản/báo cáo và MockMvc authorization/API: PASS.
- Frontend production build: PASS.
- `git diff --check`: PASS.

Chưa xác minh:

- Flyway/JPA aggregate query và CRUD tài khoản trên MySQL thật vì không có listener tại cổng 3306.
- REST API và browser integration end-to-end vì backend/frontend/MySQL không chạy trong lần xác minh cuối.
