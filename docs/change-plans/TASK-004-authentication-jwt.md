# TASK-004 — Authentication, Authorization và JWT

## Mục tiêu

Triển khai chức năng nghiệp vụ đầu tiên của backend: đăng nhập bằng username/password, BCrypt, JWT stateless, phân quyền theo ba role chính thức, logout stateless và các endpoint kỹ thuật xác minh authorization.

## Source of Truth

- `docs/report_KTPM_chương_1-2-3 up(tam).docx` — không sửa và không đọc lại khi baseline đã đủ.
- `docs/REPORT_BASELINE.md`.
- `docs/TRACEABILITY_MATRIX.md`.
- `docs/KNOWN_INCONSISTENCIES.md`.
- `docs/CHANGE_POLICY.md`.
- `docs/DATABASE_BASELINE.md`.
- Yêu cầu BƯỚC 5 và các quyết định role/JWT/stateless do người dùng xác nhận.

## FR liên quan

- `FR-AUTH-01`: đăng nhập bằng tài khoản hợp lệ.
- `FR-AUTH-02`: đăng xuất.
- `FR-AUTH-03`: xác định quyền dựa trên vai trò.
- `FR-AUTH-04`: từ chối truy cập chức năng người dùng không được phép.

## Business Rule liên quan

- Username và password không được để trống.
- Tài khoản phải tồn tại và ở trạng thái hoạt động.
- Password được so sánh bằng BCrypt với `users.password_hash`; không lưu hoặc trả password plain text/hash.
- Thông báo sai username/password dùng thông báo chung.
- Role chỉ gồm `ADMIN`, `SALES_STAFF`, `WAREHOUSE_STAFF` và được lưu tại `users.role`.
- JWT có signature, identity, username, role, issued time và expiration.
- JWT secret lấy từ `JWT_SECRET`; expiration lấy từ `JWT_EXPIRATION` nếu cấu hình.
- Logout stateless không lưu session/blacklist; client xóa token.
- Không triển khai khóa 5 lần, khóa 15 phút, giới hạn phiên hoặc login audit.

## Use Case liên quan

- Admin UC-01, Nhân viên bán hàng UC-01, Nhân viên kho UC-01 — Đăng nhập.
- Admin UC-02, Nhân viên bán hàng UC-02, Nhân viên kho UC-02 — Đăng xuất; mapping FR chuẩn theo yêu cầu task là FR-AUTH-02.
- UC kiểm tra quyền của ba actor — FR-AUTH-03 và FR-AUTH-04.

## Activity Diagram liên quan

Hình 3.5 — Đăng nhập: nhận input, tìm `users`, kiểm tra trạng thái, BCrypt password, lấy role, tạo JWT và trả response.

## Phạm vi ảnh hưởng

- Security configuration chuyển từ foundation permit-all sang JWT stateless.
- Thêm Auth Controller, Security Verification Controller, DTO, Authentication Service, JWT Service/filter và UserDetails.
- Mở rộng `UserRepository` bằng lookup username.
- Thêm migration dữ liệu demo user, không thay đổi schema.
- Thêm unit, MVC/security và JWT tests.
- Cập nhật traceability cho FR-AUTH-01..04 theo mức đã xác minh.

## Entity liên quan

- `User` ánh xạ bảng `users`; không expose Entity qua API.
- Không tạo Role, Permission, LoginSession hoặc LoginLog entity.

## DTO liên quan

- `LoginRequest`: username, password với validation NotBlank.
- `LoginResponse`: accessToken, tokenType, userId, username, fullName, role.
- Response logout tối thiểu, không chứa token hoặc state server-side.

## Service liên quan

- `AuthenticationService`: điều phối UserRepository/UserDetails, trạng thái, BCrypt và JWT.
- `JwtService`: tạo, parse và xác thực JWT.
- `CustomUserDetailsService`: nạp user từ Repository cho security filter.

## Repository liên quan

- `UserRepository.findByUsername(String username)`.
- Không thêm query hoặc Repository ngoài phạm vi AUTH.

## API impact

- `POST /api/auth/login` — public.
- `POST /api/auth/logout` — yêu cầu JWT hợp lệ; stateless acknowledgement.
- `GET /api/admin/ping` — `ADMIN`.
- `GET /api/sales/ping` — `SALES_STAFF`.
- `GET /api/warehouse/ping` — `WAREHOUSE_STAFF`.

Ba endpoint ping là `SECURITY VERIFICATION ENDPOINT`, không phải API nghiệp vụ final.

## Database impact

`NO LOGICAL SCHEMA CHANGE`

- Không thêm table, column, datatype, PK, FK, UNIQUE, NULL constraint hoặc relationship.
- Không tạo `roles`, `permissions`, `login_sessions`, `login_logs` hoặc token blacklist.
- Thêm đúng ba bản ghi demo vào `users` bằng BCrypt hash qua data migration.

## UI impact

`NONE`

Không tạo frontend hoặc Login UI; client logout ở BƯỚC 6 sẽ xóa token.

## Test cần chạy

1. Login đúng username/password.
2. Username không tồn tại.
3. Sai password.
4. Tài khoản không hoạt động.
5. Thiếu username.
6. Thiếu password.
7. JWT hợp lệ.
8. JWT sai chữ ký.
9. JWT hết hạn.
10. ADMIN truy cập admin ping.
11. SALES_STAFF bị 403 tại admin ping.
12. WAREHOUSE_STAFF bị 403 tại admin ping.
13. SALES_STAFF truy cập sales ping.
14. WAREHOUSE_STAFF truy cập warehouse ping.
15. Không JWT vào protected endpoint bị 401.
16. Logout với JWT hợp lệ trả acknowledgement và không tạo server-side state.

## Regression Test

- Chạy toàn bộ `mvn test`, gồm 5 foundation tests.
- Compile/package backend.
- Startup thử tại port 8080.
- Kiểm tra không có HTTP session login state.
- Kiểm tra password hash/JWT secret không xuất hiện trong API response hoặc log.
- Kiểm tra migration V1 và logical database schema không đổi.
- Kiểm tra frontend, Product, Sales, Inventory, Invoice và Reporting không thay đổi.
- `git diff --check`, `git status`, `git diff` và Word checksum.

## Những phần không được sửa

- Source Word.
- Logical database schema 11 bảng và migration V1.
- Product, Category, Supplier, Customer, Inventory, Import, Sales, Invoice và Reporting business logic.
- Frontend.
- Không OAuth2, Keycloak, refresh token, session table, login log, blacklist hoặc role mới.
- Không triển khai BƯỚC 6.

## Rủi ro

- MySQL runtime hiện chưa khả dụng nên login integration và Flyway V2 có thể không xác minh được trên database thật.
- Giá trị `users.status` phải dùng chuẩn `ACTIVE` cho tài khoản demo; dữ liệu runtime dùng giá trị khác phải được báo conflict, không đổi schema.
- JWT secret ngắn làm HMAC không an toàn; cấu hình phải từ environment và đủ độ dài.
- Logout stateless không thu hồi token đã phát hành; token chỉ hết hiệu lực khi hết hạn hoặc client xóa token, đúng giới hạn baseline không có blacklist/session.
- Endpoint ping chỉ dùng xác minh security, không mở rộng nghiệp vụ.

## Kết quả

`COMPLETED — AUTHENTICATION & AUTHORIZATION READY`

- Compile: PASS.
- Automated tests: PASS (`25` tests, `0` failures, `0` errors, `0` skipped).
- Spring Context: PASS trong automated test và runtime với MySQL thật.
- Login/JWT/role authorization: PASS qua unit, MockMvc và API integration thật dùng `UserRepository`/MySQL.
- BCrypt demo hash: VERIFIED.
- MySQL 8.4.13, service `MySQL84` và port 3306: PASS.
- Default backend startup: PASS tại `http://localhost:8080`; JDBC, JPA và Tomcat khởi tạo thành công.
- Flyway: PASS; schema hiện hữu được baseline an toàn tại V1 và V2 seed demo users được áp dụng thành công.
- Runtime API: PASS cho login ba role, logout stateless, JWT hợp lệ, 401 và 403.
- Flyway integration dependency: dùng `spring-boot-starter-flyway` cùng `flyway-mysql` theo Spring Boot 4.1; không đổi migration hoặc logical schema.
- Word checksum giữ nguyên: `4696E2404518895D343B5C9E2C67D55DED669158B940A473E4E6693C917EF91B`.
- Logical schema V1 và `database/schema.sql`: NOT CHANGED.
