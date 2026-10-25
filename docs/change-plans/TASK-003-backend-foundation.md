# TASK-003 — Backend Foundation

## Mục tiêu

Tạo Spring Boot Maven backend Java chạy được tại `backend/`, tích hợp JPA, Flyway, Spring Security foundation và MySQL theo database baseline đã khóa, nhưng chưa triển khai bất kỳ chức năng nghiệp vụ hoặc API nghiệp vụ nào.

## Source of Truth

- `docs/report_KTPM_chương_1-2-3 up(tam).docx` — chỉ đọc, không sửa.
- `docs/REPORT_BASELINE.md`.
- `docs/TRACEABILITY_MATRIX.md`.
- `docs/KNOWN_INCONSISTENCIES.md`.
- `docs/CHANGE_POLICY.md`.
- `docs/DATABASE_BASELINE.md`.
- `database/schema.sql`.

## Requirement

Backend foundation hỗ trợ kỹ thuật cho bộ FR đã đặc tả nhưng không triển khai hoặc đánh dấu hoàn thành bất kỳ FR nghiệp vụ nào trong BƯỚC 4.

## Business Rule

Không triển khai business rule trong BƯỚC 4. Entity mapping chỉ phản ánh database baseline đã khóa; rule nghiệp vụ thuộc Service ở các bước sau.

## Use Case

NONE IMPLEMENTED. Không tạo endpoint hoặc mock Use Case.

## Architecture

```text
REST Controller
-> Service / Business Layer
-> Repository / Data Access Layer
-> MySQL
```

- Package root: `com.ktpm.salesmanagement`.
- Controller không SQL, không gọi database trực tiếp và không chứa business rule chính.
- Service chịu trách nhiệm business rule, validation nghiệp vụ và transaction khi được triển khai ở bước sau.
- Repository chỉ thực hiện data access.

## Phạm vi ảnh hưởng

- Tạo Maven project trực tiếp tại `backend/`, không tạo project lồng.
- Tạo Spring Boot application và cấu hình datasource bằng environment variables.
- Tạo Flyway V1 tương đương logical schema đã khóa.
- Tạo 11 JPA Entity và 11 Spring Data JPA Repository.
- Tạo exception foundation và Spring Security foundation.
- Tạo context load test.
- Ghi nhận yêu cầu đồng bộ Spring Boot vào báo cáo final.

## File dự kiến tạo

- `backend/pom.xml`.
- Maven Wrapper nếu môi trường/tooling hỗ trợ tạo an toàn.
- `backend/src/main/java/com/ktpm/salesmanagement/SalesManagementApplication.java`.
- `backend/src/main/java/com/ktpm/salesmanagement/config/SecurityConfig.java`.
- Các Entity trong `backend/src/main/java/com/ktpm/salesmanagement/entity/`.
- Các Repository trong `backend/src/main/java/com/ktpm/salesmanagement/repository/`.
- Exception foundation trong `backend/src/main/java/com/ktpm/salesmanagement/exception/`.
- `backend/src/main/resources/application.yml`.
- `backend/src/main/resources/db/migration/V1__create_initial_schema.sql`.
- `backend/src/test/java/com/ktpm/salesmanagement/SalesManagementApplicationTests.java`.
- `docs/REPORT_SYNC_REQUIRED.md`.

## File dự kiến sửa

- `docs/TRACEABILITY_MATRIX.md` chỉ khi cần ghi chú infrastructure; không đổi trạng thái FR.
- `docs/change-plans/TASK-003-backend-foundation.md` để ghi kết quả verification.

## Entity

Tạo đúng 11 Entity: `User`, `Category`, `Product`, `Supplier`, `Customer`, `SalesOrder`, `OrderDetail`, `PurchaseReceipt`, `PurchaseReceiptDetail`, `StockTransaction`, `Invoice`.

Không tạo Role, Permission, Inventory, InvoiceDetail, LoginSession, LoginLog hoặc StockHistory.

## DTO

Không tạo DTO nghiệp vụ. Package `dto.request` và `dto.response` chỉ được tạo khi có class thực tế ở bước sau.

## Service

Không tạo Service nghiệp vụ hoặc class rỗng. Package `service`/`service.impl` được dành cho bước sau.

## Repository

Tạo đúng 11 repository tương ứng 11 Entity; không thêm query nghiệp vụ phức tạp.

## Database impact

`NO LOGICAL SCHEMA CHANGE`

- Flyway V1 phải tương đương `database/schema.sql` ở mức table, column, datatype, PK, FK, UNIQUE và NULL/NOT NULL.
- Không DROP, reset hoặc thay đổi database baseline.

## API impact

`NONE — NO BUSINESS ENDPOINT`

Không tạo Login, Product, Category, Supplier, Customer, Inventory, Import, Sales, Invoice hoặc Reporting API.

## UI impact

`NONE`

Không tạo hoặc sửa frontend.

## Security foundation

- Thêm Spring Security dependency và cấu hình tối thiểu để application startup.
- `FOUNDATION ONLY — AUTHENTICATION NOT IMPLEMENTED`.
- Không JWT, login giả, hard-code Admin hoặc in-memory user nghiệp vụ.

## Test cần chạy

- Kiểm tra Java, Maven và MySQL environment.
- Maven compile.
- Spring Boot context load test.
- Kiểm tra mapping đủ 11 Entity và 11 Repository.
- Kiểm tra migration tương đương database baseline.
- Chạy toàn bộ Maven test.
- Chạy backend startup tại port 8080.
- Nếu MySQL không khả dụng: ghi `MYSQL CONNECTION: NOT VERIFIED`; không giả lập kết quả.

## Regression Test

- `git diff --check`.
- `git diff -- database docs backend frontend`.
- Xác minh hash Word không đổi.
- Xác minh `database/schema.sql` và logical schema không thay đổi.
- Xác minh frontend không thay đổi.
- Xác minh không có business endpoint, JWT hoặc business Service.
- Xác minh API, Service, UI và Business Test trong traceability vẫn là `TODO`.

## Những phần không được sửa

- Source Word.
- Logical database schema và database decision đã khóa.
- `database/schema.sql`, `database/seed.sql` và `database/README.md`.
- Frontend.
- Requirement, Business Rule, Use Case hoặc role.
- Không triển khai BƯỚC 5.

## Rủi ro

- MySQL runtime/credential có thể không sẵn có, khiến Flyway, JPA mapping và startup với datasource thật không thể xác minh.
- Spring Boot version phải tương thích Java/Maven cài trên máy.
- Quan hệ JPA hai chiều có thể gây recursion nếu expose Entity; BƯỚC 4 không expose Entity qua API.
- Migration lệch baseline sẽ vi phạm database safety; phải dừng và báo conflict thay vì sửa schema.

## Kết quả

`PARTIAL — MYSQL ENVIRONMENT REQUIRED`

- Java: OpenJDK 25.0.4 LTS.
- Maven: 3.10.0.
- Spring Boot: 4.1.2.
- MySQL CLI/service: NOT AVAILABLE.
- Compile: PASS.
- Test: PASS — 5 tests, 0 failure, 0 error, 0 skipped.
- Spring Context: PASS với database auto-configuration được loại khỏi context test do MySQL không khả dụng.
- Entity mapping: PASS qua 4 metadata tests; runtime validation với MySQL chưa xác minh.
- Flyway migration: configured; V1 khớp tuyệt đối logical schema của `database/schema.sql`; runtime chưa xác minh.
- Default startup: FAIL tại MySQL `Connection refused`; không phát hiện schema conflict trước điểm kết nối.
- Non-database startup: PASS tại port 8080, sau đó graceful shutdown.
- MySQL connection: NOT VERIFIED.
- Database impact: `NO LOGICAL SCHEMA CHANGE`.
