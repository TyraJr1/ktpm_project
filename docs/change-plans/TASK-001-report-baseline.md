# TASK 001 Report Baseline

## Mục tiêu

Đọc toàn bộ Chương I đến Chương III của tài liệu đặc tả chính và tạo baseline truy vết cho BƯỚC 2 mà không thay đổi source code, database, API, UI hoặc tài liệu Word nguồn.

## Source Document

`D:\ktpm_project\docs\report_KTPM_chương_1-2-3 up(tam).docx`

Tài liệu được sử dụng ở chế độ chỉ đọc và là nguồn đặc tả chính duy nhất của task này.

## Requirement

Trích xuất đúng các Requirement được ghi trong tài liệu nguồn. Không tự tạo, đổi mã, đổi tên hoặc bổ sung Requirement.

## Business Rule

Trích xuất đúng các Business Rule được ghi trong tài liệu nguồn. Mọi mâu thuẫn phải được ghi nhận để người dùng quyết định, không tự sửa.

## Use Case

Trích xuất các Use Case và ánh xạ theo đúng tài liệu nguồn; ghi nhận ánh xạ không nhất quán nếu có.

## Phạm vi ảnh hưởng

Chỉ tạo hoặc cập nhật các tài liệu sau trong `docs`:

- `docs/change-plans/TASK-001-report-baseline.md`
- `docs/REPORT_BASELINE.md`
- `docs/TRACEABILITY_MATRIX.md`
- `docs/KNOWN_INCONSISTENCIES.md`
- `docs/CHANGE_POLICY.md`

## File thay đổi

Chỉ các file tài liệu Markdown được liệt kê trong mục Phạm vi ảnh hưởng.

## Entity

NONE

## DTO

NONE

## Service

NONE

## Repository

NONE

## Source Code Impact

NONE

## Database Impact

NONE

Không tạo database, migration hoặc chạy SQL.

## API Impact

NONE

## UI Impact

NONE

## Test

Kiểm tra nội dung tài liệu baseline và traceability với tài liệu Word nguồn; kiểm tra Git diff và phạm vi file sau khi hoàn tất.

## Regression Test

Xác nhận không tạo hoặc thay đổi `backend`, `frontend`, `database`; xác nhận file Word nguồn không thay đổi.

## Những phần không được sửa

- `backend`
- `frontend`
- `database`
- `D:\ktpm_project\docs\report_KTPM_chương_1-2-3 up(tam).docx`
- Mọi nội dung, bảng, hình, Use Case, Activity Diagram và cấu trúc database trong file Word nguồn

## Rủi ro

- Hình vẽ hoặc bảng trong DOCX có thể chứa thông tin không xuất hiện đầy đủ khi chỉ trích xuất text.
- Các phần trong báo cáo có thể mâu thuẫn về mã Requirement, actor, kiểu dữ liệu hoặc trình tự nghiệp vụ.
- Không được tự quyết định phía đúng của một mâu thuẫn.

## Kết quả

`HOÀN THÀNH — BASELINE EXTRACTED`

- Đã đọc toàn bộ Chương I đến Chương III ở chế độ chỉ đọc, gồm text, bảng, Use Case, sáu Activity Diagram, ERD, Database Diagram và Processing Design.
- Đã tạo `REPORT_BASELINE.md`, `TRACEABILITY_MATRIX.md`, `KNOWN_INCONSISTENCIES.md` và `CHANGE_POLICY.md`.
- Đã xác minh matrix có 39 Functional Requirement; các cột API, Service, Repository, UI và Test đều là `TODO`; Status chỉ dùng `SPECIFIED` hoặc `INCONSISTENT`.
- Đã ghi nhận conflict mở, không tự thay đổi Requirement, Business Rule, role, table hoặc kiến trúc.
- Source code impact, database impact, API impact và UI impact: `NONE`.
