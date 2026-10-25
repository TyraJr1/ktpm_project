# Change Policy

## Nguyên tắc bắt buộc

```text
Tài liệu -> thiết kế -> code -> test phải đồng bộ
```

Tài liệu Word nguồn là nguồn đặc tả chính. Không được tự thêm chức năng, Business Rule, role, table, relationship, kiến trúc hoặc requirement còn thiếu.

## Khi một Requirement thay đổi

Phải thực hiện Impact Analysis và kiểm tra, cập nhật đồng bộ tất cả phần có liên quan:

1. Business Rule.
2. Use Case.
3. Activity Diagram.
4. Processing Design.
5. ERD.
6. Database Diagram và cấu trúc Database.
7. API và DTO/contract.
8. Service / Business Layer và transaction boundary.
9. Repository / Data Access Layer.
10. UI.
11. Test và regression test.

Không được coi thay đổi hoàn tất nếu một mắt xích liên quan chưa được xác minh.

## Kiểm soát phạm vi thay đổi

- Nếu một phần đang chạy tốt và không liên quan thì không được sửa.
- Chỉ sửa file và module cần thiết cho requirement đã được phê duyệt.
- Không refactor diện rộng, đổi tên field/table/API đang chạy tốt hoặc xóa migration cũ.
- Nếu buộc phải sửa shared component, phải xác định dependency và chạy regression test cho các chức năng phụ thuộc.
- Thay đổi contract phải được cập nhật ở toàn bộ nơi tiêu thụ contract.

## Xử lý conflict

- Nếu một conflict chưa được người dùng quyết định thì không được tự code theo bất kỳ phía nào.
- Không dùng suy đoán, thông lệ hoặc nội dung từ module khác để lấp requirement thiếu.
- Mọi conflict phải được ghi trong `docs/KNOWN_INCONSISTENCIES.md` với hai nguồn, tác động, đề xuất và trạng thái mở.
- Chỉ sau quyết định rõ ràng của người dùng mới được cập nhật source document/design artefact liên quan và lập change plan cho bước triển khai.

## An toàn database

Không tự đổi hoặc thêm table, column, datatype, PK, FK, UNIQUE, NOT NULL, DEFAULT hay relationship; không reset database; không xóa migration cũ. Mọi thay đổi database phải có Impact Analysis và truy vết về Requirement đã được quyết định.

## Kiến trúc bắt buộc

```text
Frontend
-> REST Controller
-> Service / Business Layer
-> Repository / Data Access Layer
-> MySQL
```

- Controller nhận request/DTO, validation cơ bản, gọi Service và trả response; không SQL, không gọi database trực tiếp, không chứa business rule phức tạp.
- Service xử lý business rule, validation nghiệp vụ, transaction, tính toán và điều phối Repository.
- Repository truy vấn, lưu, cập nhật và giao tiếp database.

## Điều kiện hoàn thành thay đổi

Không ghi `DONE` chỉ vì compile thành công. Tùy phạm vi, phải xác minh compile, backend, database, migration, API, validation, business rule, authorization, transaction, test, regression test và UI gọi API thật. Phần chưa xác minh phải ghi `PARTIAL` hoặc `NOT VERIFIED`.

## Phạm vi cấm tự mở rộng

Không tự thêm Microservices, Redis, Kafka, Elasticsearch, Docker, Kubernetes, AI, Recommendation, thanh toán online, shipping, voucher, promotion, multi-branch hoặc customer e-commerce portal.
