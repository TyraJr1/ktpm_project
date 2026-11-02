# TASK-008 — Thiết kế lại giao diện Admin chuyên nghiệp

## Mục tiêu

Nâng cấp toàn bộ trải nghiệm thị giác frontend thành giao diện quản trị hiện đại, cao cấp, nhất quán và responsive; giữ nguyên toàn bộ API, route, dữ liệu, validation, business rule, authorization và database.

## Requirement

- Yêu cầu người dùng ngày 02/11/2026: thiết kế lại frontend đẹp và chuyên nghiệp như sản phẩm thực tế.
- Không thay đổi logic hệ thống hoặc thêm chức năng nghiệp vụ.
- Giữ khả năng truy cập và thao tác của các module Admin đã triển khai.

## Business Rule

- Không thay đổi bất kỳ business rule nào.
- Không thay đổi ba role, trạng thái, quy tắc tài khoản hoặc báo cáo.
- UI tiếp tục gọi API thật và không dùng dữ liệu giả.

## Use Case

- Giữ nguyên toàn bộ Use Case và route Admin hiện có.
- Thay đổi chỉ ở cách trình bày, phân cấp thị giác và responsive behavior.

## Phạm vi ảnh hưởng

- Login, App Shell, sidebar, topbar, Dashboard, trang dữ liệu, biểu mẫu, bảng, báo cáo và trạng thái lỗi/empty.
- CSS responsive cho desktop, tablet và mobile.

## File thay đổi

- `frontend/src/styles/main.css`
- `frontend/tests/task008-visual.mjs` (kiểm tra hình ảnh; dữ liệu giả lập chỉ tồn tại trong test).
- Không thay đổi component, page, API client hoặc event logic trong TASK-008.

## Entity

`NONE`

## DTO

`NONE`

## Service

`NONE`

## Repository

`NONE`

## Database impact

`NONE`

## API impact

`NONE`

## UI impact

- Hệ màu navy/indigo/teal cao cấp với nền sáng có chiều sâu.
- Sidebar, topbar, button, form, table, card và report được chuẩn hóa thành một design system.
- Cải thiện typography, spacing, hover/focus, shadow, trạng thái và khả năng đọc dữ liệu.
- Mobile chuyển sidebar thành thanh điều hướng cuộn ngang, không làm mất toàn bộ menu.

## Test

- `npm run build`.
- Browser regression khi backend/MySQL hoạt động.
- Kiểm tra trực quan Login, Dashboard, bảng dữ liệu, form tài khoản và báo cáo ở desktop/mobile.

## Regression Test

- Route guard, login/logout, refresh và 401/403.
- Menu và route của mọi module Admin.
- Form/table vẫn giữ selector và event hiện tại.
- Không thay đổi request/response contract.

## Những phần không được sửa

- Backend, database, migration, API contract và báo cáo Word.
- Business logic, validation, role và transaction.
- Không thêm biểu đồ hoặc số liệu giả.
- Không thay đổi module nghiệp vụ ngoài trình bày frontend.

## Rủi ro

- CSS diện rộng có thể làm vỡ layout bảng/form trên màn hình nhỏ; phải kiểm tra responsive.
- Thay đổi cấu trúc DOM có thể làm hỏng browser selector; ưu tiên giữ ID, label và text nghiệp vụ.
- Font web có thể không tải khi offline; phải có system fallback.

## Kết quả

`PARTIAL — VISUAL REDESIGN IMPLEMENTED AND VERIFIED`

- `npm run build`: PASS (Vite 7.3.7, 15 modules transformed).
- Visual test bằng Edge headless: PASS cho Login desktop, Dashboard desktop/mobile và trang Tài khoản desktop.
- `git diff --check`: PASS; chỉ có cảnh báo chuẩn hóa LF/CRLF từ các file có sẵn.
- Không thay đổi backend, database, migration, API contract, business rule hoặc authorization.
- Chưa chạy kiểm thử tích hợp UI với backend/MySQL thật trong TASK-008 vì các dịch vụ này chưa được khởi chạy; test hình ảnh dùng response giả lập chỉ trong môi trường test.
