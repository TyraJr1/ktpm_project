# TASK-009 — Chạy dự án sau khi clone

## Mục tiêu

Cung cấp quy trình thiết lập máy mới để thành viên nhóm build, chạy backend/frontend và tiếp tục phát triển với MySQL cục bộ, không đưa mật khẩu vào Git.

## Requirement

- Yêu cầu người dùng ngày 05/11/2026: tạo cách chạy dự án sau khi clone; thành viên có thông tin đăng nhập MySQL có thể chạy và code tiếp.
- Tuân thủ kiến trúc và nguồn đặc tả trong `AGENTS.md`; đây là thay đổi về môi trường phát triển, không thêm chức năng nghiệp vụ.

## Business Rule

Không thay đổi.

## Use Case

Thành viên clone repository, chuẩn bị MySQL, nhập thông tin kết nối riêng, chạy Flyway/backend và frontend, đăng nhập bằng tài khoản demo để phát triển.

## Phạm vi ảnh hưởng

Tài liệu khởi chạy và script hỗ trợ chạy backend trên Windows PowerShell.

## File thay đổi

- `README.md`
- `backend/README.md`
- `database/README.md`
- `scripts/run-backend.ps1`
- `docs/change-plans/TASK-009-clone-and-run.md`

## Entity

Không thay đổi.

## DTO

Không thay đổi.

## Service

Không thay đổi.

## Repository

Không thay đổi.

## Database impact

Không thay đổi schema hoặc migration. Hướng dẫn tạo database rỗng để Flyway V1/V2 quản lý schema và tài khoản demo. Không chạy `database/schema.sql` cùng Flyway trên database mới.

## API impact

Không thay đổi.

## UI impact

Không thay đổi; tài liệu nêu URL và cấu hình API hiện có.

## Test

- Kiểm tra cú pháp PowerShell script.
- Build và test backend, build frontend.
- Kiểm tra các lệnh trong tài liệu khớp cấu hình thực tế.

## Regression Test

Không sửa mã nghiệp vụ; chạy lại test backend và build frontend. Chỉ xác nhận runtime/API/UI với MySQL khi có database thử nghiệm an toàn.

## Những phần không được sửa

Báo cáo Word, Entity/DTO/Controller/Service/Repository, schema, migration, business rule, role và API contract.

## Rủi ro

- Máy mới thiếu Java 25, Maven, Node/npm hoặc MySQL.
- Database đã có bảng nhưng chưa có lịch sử Flyway cần xử lý riêng, không tự baseline hay reset.
- Dùng chung mật khẩu MySQL qua Git làm lộ thông tin; script chỉ nhận mật khẩu trong phiên chạy.

## Kết quả

PARTIAL — quy trình clone và chạy đã được tài liệu hóa; runtime MySQL/API/UI trên database mới chưa xác minh trong task này.

- PowerShell script parse: PASS.
- Backend `mvn test`: PASS, 42 test; các test này không xác minh database MySQL thật.
- Frontend `npm run build`: PASS, Vite 7.3.7.
- `git diff --check`: PASS.
- Không thay đổi code nghiệp vụ, schema, migration hoặc API.
