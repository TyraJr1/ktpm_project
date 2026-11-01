# TASK-005A — Admin Dashboard Polish

## Mục tiêu

Tinh chỉnh giao diện sau BƯỚC 6 thành trang quản trị hệ thống bán hàng văn phòng phẩm chuyên nghiệp, phù hợp trình bày; giữ nguyên luồng đăng nhập, phân quyền, làm mới phiên và đăng xuất đã xác minh.

## Requirement / Business Rule / Use Case

- Kế thừa FR-AUTH-01..04 và các use case đăng nhập, truy cập theo vai trò, đăng xuất từ TASK-004/TASK-005.
- Hiển thị vai trò nghiệp vụ thân thiện; không thay đổi enum hoặc contract xác thực.
- Dữ liệu KPI, biểu đồ, sản phẩm bán chạy và cảnh báo tồn kho chỉ phục vụ trình bày Dashboard theo yêu cầu của task này.
- Các lối tắt và menu nghiệp vụ chưa có chức năng chỉ hiển thị thông báo nhẹ, không gọi API hoặc mở form giả.

## Phạm vi ảnh hưởng

- Frontend Admin shell, Dashboard, Login copy và trang khu vực theo vai trò.
- Browser regression test hiện có.

## File thay đổi

- `frontend/src/components/AppShell.js`
- `frontend/src/pages/AdminDashboardPage.js`
- `frontend/src/pages/LoginPage.js`
- `frontend/src/pages/RolePlaceholderPage.js`
- `frontend/src/styles/main.css`
- `frontend/tests/browser-auth.mjs`
- `frontend/src/data/dashboardPreviewData.js` (tạo mới)
- `frontend/src/utils/userDisplay.js` (tạo mới)

## Entity / DTO / Service / Repository

- Không thay đổi.

## Database impact

`NONE`

Logical schema: `NOT CHANGED`

## API impact

- Không thay đổi API contract.
- Tiếp tục dùng các endpoint xác thực/phân quyền hiện có cho regression.
- Không tạo API giả và không gọi API cho dữ liệu trình bày Dashboard.

## UI impact

- Nâng cấp sidebar, header, hero, KPI cards, biểu đồ 7 ngày, danh sách bán chạy, cảnh báo tồn kho và truy cập nhanh.
- Loại bỏ nội dung tiến độ phát triển và thuật ngữ kỹ thuật khỏi giao diện người dùng.
- Ánh xạ `ADMIN` thành `Quản trị viên` và không hiển thị tên demo tại Dashboard.

## Security impact

- Không thay đổi Authentication, JWT, Authorization hoặc cách lưu phiên.
- Route guard và xử lý 401/403 được giữ nguyên và kiểm thử hồi quy.

## Test

- `npm run build`.
- Browser regression: login Admin, Dashboard, refresh, logout, route protection, Sales/Warehouse authorization, 401, 403, console error.
- Kiểm tra menu/lối tắt chỉ hiện thông báo và dữ liệu preview không gọi API nghiệp vụ.

## Regression Test

- Login/logout và session persistence.
- Admin protected request.
- SALES_STAFF/WAREHOUSE_STAFF không truy cập Admin Dashboard.
- 401 xóa phiên; 403 giữ phiên và hiển thị đúng thông báo.

## Những phần không được sửa

- Word report, database schema, Flyway.
- Backend Authentication/JWT/Authorization/API contract.
- Category, Product, Supplier, Customer, Account, Inventory, Purchase, Sales, Invoice và Reporting CRUD/backend.
- BƯỚC 7.

## Rủi ro

- Dữ liệu trình bày có thể bị hiểu nhầm là dữ liệu vận hành; giảm thiểu bằng cách cô lập trong một module có chú thích rõ và không nối với API/database.
- Thay đổi shell có thể ảnh hưởng thao tác logout hoặc role pages; giảm thiểu bằng browser regression đầy đủ.

## Kết quả

`COMPLETED`

- Dashboard đã được polish theo phong cách navy/teal, desktop-first và responsive cơ bản.
- Dữ liệu trình bày được cô lập; không có API giả hoặc thay đổi database/backend.
- Production build và 10 browser integration scenarios đã PASS.
