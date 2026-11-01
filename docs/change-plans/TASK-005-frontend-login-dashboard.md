# TASK-005 — Frontend Foundation, Login UI và Admin Dashboard

## Mục tiêu

Xây dựng frontend local bằng Vite Vanilla JavaScript tại `frontend/`, kết nối trực tiếp backend hiện có để đăng nhập, lưu trạng thái JWT cho môi trường development, điều hướng theo role, bảo vệ route, hiển thị Admin Dashboard và đăng xuất stateless.

## Requirement liên quan

- `FR-AUTH-01`: đăng nhập bằng username/password hợp lệ.
- `FR-AUTH-02`: đăng xuất và xóa trạng thái xác thực phía client.
- `FR-AUTH-03`: điều hướng và giới hạn giao diện theo role `ADMIN`, `SALES_STAFF`, `WAREHOUSE_STAFF`.
- `FR-AUTH-04`: từ chối truy cập route không đúng quyền; xử lý riêng HTTP 401 và 403.

## Business Rule liên quan

- Username và password không được để trống.
- Không lưu password trong auth state hoặc source.
- JWT được gửi bằng `Authorization: Bearer <token>` cho protected request.
- 401 xóa auth state và chuyển về Login; 403 giữ auth state và hiển thị thông báo không có quyền.
- Authorization backend vẫn là lớp bảo vệ chính; route guard frontend chỉ hỗ trợ UX.
- Logout stateless gọi backend rồi xóa token phía client; không tạo blacklist/session.

## Use Case liên quan

- Admin UC-01, Bán hàng UC-01, Kho UC-01 — Đăng nhập.
- Admin UC-02, Bán hàng UC-02, Kho UC-02 — Đăng xuất.
- Use Case kiểm tra quyền của ba actor — FR-AUTH-03 và FR-AUTH-04.

## Activity Diagram liên quan

Hình 3.5 — Đăng nhập: nhập thông tin, validation cơ bản, gọi backend, nhận role/JWT và điều hướng giao diện phù hợp.

## Phạm vi ảnh hưởng

- Tạo frontend foundation tại chính thư mục `frontend/`.
- Tạo API client tập trung, auth store, router/route guard, Login page, Admin Dashboard và placeholder theo role.
- Cập nhật cột UI/Test của `FR-AUTH-01..04` sau khi browser integration test thật PASS.
- Không triển khai module nghiệp vụ.

## File dự kiến tạo/sửa

- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/index.html`
- `frontend/vite.config.js`
- `frontend/src/main.js`
- `frontend/src/api/client.js`
- `frontend/src/auth/authStore.js`
- `frontend/src/auth/routeGuard.js`
- `frontend/src/components/AppShell.js`
- `frontend/src/pages/LoginPage.js`
- `frontend/src/pages/AdminDashboardPage.js`
- `frontend/src/pages/RolePlaceholderPage.js`
- `frontend/src/styles/main.css`
- `frontend/src/utils/router.js`
- `frontend/tests/browser-auth.mjs`
- `docs/TRACEABILITY_MATRIX.md`
- `docs/change-plans/TASK-005-frontend-login-dashboard.md`

## Entity

`NONE` — frontend không tạo hoặc sửa backend Entity.

## DTO

Không sửa backend DTO. Frontend dùng đúng contract `LoginRequest(username, password)` và `LoginResponse(accessToken, tokenType, userId, username, fullName, role)`.

## Service

`NONE` — không sửa backend Service/business rule.

## Repository

`NONE` — không sửa Repository hoặc truy cập database trực tiếp.

## API liên quan

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/admin/ping`
- `GET /api/sales/ping`
- `GET /api/warehouse/ping`

Không đổi request/response hoặc authorization contract.

## UI impact

- Login page có validation, loading và lỗi phù hợp.
- Admin Dashboard có sidebar/header/content; chỉ Dashboard và Logout hoạt động.
- Sales/Warehouse chỉ có placeholder đúng phạm vi.
- Menu nghiệp vụ chưa triển khai ở trạng thái disabled và ghi rõ `Chưa triển khai`.
- Responsive cơ bản cho desktop và màn hình hẹp.

## Security impact

- JWT và metadata user được lưu trong `localStorage` cho chiến lược development; password không được lưu.
- API client tự gắn Bearer token cho protected request.
- 401 xóa auth state và điều hướng Login.
- 403 không biến thành 401; hiển thị `Bạn không có quyền truy cập chức năng này.`.
- Không thay đổi backend Authorization, JWT lifecycle, blacklist hoặc session.
- CORS backend hiện đã giới hạn localhost và có `http://localhost:5173`; chỉ sửa nếu browser test chứng minh có lỗi.

## Database impact

`NONE`

## Logical schema

`NOT CHANGED`

## Test plan

1. `npm install` và `npm run build` PASS.
2. Backend/MySQL đang chạy; frontend chạy tại `http://localhost:5173`.
3. Browser mở Login page và validation hai field.
4. Admin login nhận JWT thật, vào Dashboard, hiển thị đúng fullName/role và refresh giữ auth state.
5. Logout gọi backend, xóa auth state và không quay lại Dashboard được.
6. Không token không vào Dashboard.
7. SALES_STAFF và WAREHOUSE_STAFF vào đúng placeholder, không vào Admin Dashboard.
8. Sai password hiển thị lỗi.
9. Token sai/401 xóa state và về Login.
10. Backend 403 giữ state và hiển thị đúng thông báo.
11. Không mock API và không có fake business data.
12. `git diff --check`, `git status`, `git diff` và checksum Word.

## Regression Test

- Không cần sửa backend nếu CORS browser test PASS.
- Nếu bắt buộc sửa riêng CORS, chạy lại toàn bộ `mvn test` và Auth API verification.
- Kiểm tra các FR ngoài `FR-AUTH-01..04` không bị thay đổi.

## Những phần không được sửa

- File Word nguồn.
- Logical database schema và migration.
- Backend Authentication/Authorization contract và business logic, trừ CORS nếu có bằng chứng cần thiết.
- Category, Product, Supplier, Customer, Account, Inventory, Purchase/Import, Sales, Invoice và Reporting.
- Không dùng React, Vue, Angular hoặc Next.js.
- Không thực hiện BƯỚC 7.

## Rủi ro

- Backend không chạy hoặc CORS sai sẽ chặn browser integration; phải xác minh nguyên nhân trước khi sửa.
- `localStorage` phù hợp development nhưng chịu rủi ro XSS; không lưu password và không ghi token vào source/log.
- Client-side route guard có thể bị sửa ở browser nên không thay thế authorization backend.
- Refresh trực tiếp route SPA cần Vite history fallback trong development.

## Kết quả

`COMPLETED — LOGIN UI & ADMIN DASHBOARD READY`

- Node.js `v24.14.0`, npm `11.9.0`, Vite `7.3.7`.
- `npm install`: PASS, `0` vulnerabilities.
- `npm run build`: PASS.
- Backend/MySQL runtime: PASS tại `http://localhost:8080`.
- Frontend runtime: PASS tại `http://localhost:5173`.
- Browser integration bằng Microsoft Edge: PASS `10/10` kịch bản.
- Login Admin, JWT thật, Dashboard, refresh và logout: PASS.
- SALES_STAFF và WAREHOUSE_STAFF placeholder/route protection: PASS.
- 401 xóa auth state và chuyển Login: PASS.
- 403 giữ auth state và hiển thị thông báo không có quyền: PASS.
- CORS: PASS với cấu hình backend hiện hữu; không sửa backend.
- Không có mock API, password lưu phía client hoặc fake business data.
- Database logical schema, Word và backend Auth: NOT CHANGED.

