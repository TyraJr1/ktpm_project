# TASK-006 — Hoàn thiện chức năng trang Admin kết nối dữ liệu thật

## Mục tiêu

Hoàn thiện các chức năng của trang Quản lý/Admin theo đúng báo cáo Word, kiến trúc bắt buộc và database baseline đã khóa; thay dữ liệu trình bày tĩnh bằng dữ liệu lấy qua REST API từ MySQL; không bổ sung bảng, field, role, chức năng hoặc business rule ngoài báo cáo.

## Requirement

Phạm vi có thể truy vết và chưa có mâu thuẫn blocking:

- FR-PRO-01 đến FR-PRO-06: xem, tạo, cập nhật, tìm kiếm, đổi trạng thái sản phẩm và quản lý danh mục.
- FR-INV-01: xem số lượng tồn hiện tại của sản phẩm.
- FR-SUP-01 đến FR-SUP-03: tạo, cập nhật và tra cứu nhà cung cấp.
- FR-BIL-03, FR-BIL-04: tra cứu và xem chi tiết hóa đơn.

Phạm vi chưa được phép triển khai trước khi có quyết định:

- Quản lý tài khoản: có trong phạm vi actor nhưng không có nhóm Functional Requirement riêng (INC-005).
- Admin xem lịch sử kho: có trong mô tả chức năng actor nhưng FR-INV-05 và Use Case hiện chỉ truy vết tới Nhân viên kho.
- FR-RPT-01 đến FR-RPT-05: còn mâu thuẫn về actor, phạm vi báo cáo, mã FR và trạng thái hóa đơn hợp lệ (INC-007, INC-013, INC-014, INC-017).

## Business Rule

- Mã sản phẩm duy nhất và không rỗng; tên sản phẩm không rỗng; giá bán không âm; danh mục phải tồn tại và hoạt động.
- Chỉ sản phẩm hoạt động được bán; thay đổi trạng thái không xóa vật lý sản phẩm.
- Tên danh mục không rỗng và không trùng; danh mục gán cho sản phẩm phải tồn tại và hoạt động.
- Mã nhà cung cấp duy nhất; tên nhà cung cấp bắt buộc; phone/email đúng định dạng nếu có.
- Tồn hiện tại lấy duy nhất từ `products.stock_quantity` và không âm.
- Tra cứu hóa đơn theo mã hoặc ngày; ngày bắt đầu không lớn hơn ngày kết thúc; chi tiết hóa đơn đi qua `invoices → sales_orders → order_details → products`; không tạo `invoice_details`.
- Backend kiểm tra quyền; UI không truy cập database trực tiếp.

## Use Case

- Admin UC-04: Xem danh sách sản phẩm.
- Admin UC-05: Tạo sản phẩm.
- Admin UC-06: Cập nhật sản phẩm.
- Admin UC-07: Tìm kiếm sản phẩm.
- Admin UC-08: Thay đổi trạng thái sản phẩm.
- Admin UC-09: Quản lý danh mục sản phẩm.
- Admin UC-10: Xem tồn kho.
- Admin UC-11: Quản lý nhà cung cấp.
- Chức năng actor Admin: tra cứu hóa đơn, giới hạn ở FR-BIL-03 và FR-BIL-04.

Các Use Case quản lý tài khoản, lịch sử kho dành cho Admin và báo cáo chưa được tự bổ sung hoặc ánh xạ lại.

## Phạm vi ảnh hưởng

- Backend: Controller, DTO, Service, Repository và exception cho Product, Category, Supplier, Inventory read model và Invoice query.
- Frontend: routing, menu Admin, các trang Product, Category, Supplier, Inventory và Invoice lookup; Dashboard chỉ hiển thị dữ liệu có API/Requirement hợp lệ.
- Test: unit, repository/integration, API authorization, frontend build và browser/API integration.
- Documentation: Traceability Matrix cập nhật theo kết quả xác minh thực tế.

## File thay đổi

Danh sách cụ thể sẽ được khóa trước từng module. Dự kiến chỉ trong:

- `backend/src/main/java/com/ktpm/salesmanagement/controller/`
- `backend/src/main/java/com/ktpm/salesmanagement/dto/`
- `backend/src/main/java/com/ktpm/salesmanagement/service/`
- `backend/src/main/java/com/ktpm/salesmanagement/repository/`
- `backend/src/main/java/com/ktpm/salesmanagement/exception/`
- `backend/src/test/java/com/ktpm/salesmanagement/`
- `frontend/src/api/client.js`
- `frontend/src/components/AppShell.js`
- `frontend/src/pages/`
- `frontend/src/utils/router.js`
- `frontend/src/main.js`
- `frontend/src/styles/main.css`
- `frontend/tests/`
- `docs/TRACEABILITY_MATRIX.md`

Không sửa file Word nguồn trong task này.

## Entity

- Tái sử dụng `Product`, `Category`, `Supplier`, `Invoice`, `SalesOrder`, `OrderDetail`.
- Chỉ sửa mapping nếu kiểm tra cho thấy không khớp migration hiện tại.
- Không tạo entity/bảng ngoài 11 bảng đã khóa.

## DTO

- Tạo DTO request/response riêng cho từng API được xác nhận.
- Không trả Entity trực tiếp ra Controller.
- Không nhận `stock_quantity` từ Product CRUD nếu hành vi đó làm thay đổi tồn kho ngoài nghiệp vụ kho đã đặc tả.

## Service

- Chứa validation nghiệp vụ, authorization bổ sung khi cần và transaction cho thao tác ghi.
- Không đưa business rule vào Controller.
- Không triển khai rule báo cáo/tài khoản đang mâu thuẫn.

## Repository

- Chỉ truy vấn 11 bảng baseline hiện có.
- Tìm kiếm sản phẩm theo mã/tên; nhà cung cấp theo mã/tên; hóa đơn theo mã/khoảng ngày.
- Chi tiết hóa đơn truy vấn qua đơn hàng và chi tiết đơn hàng.

## Database impact

`NONE EXPECTED`

- Không thêm/xóa/đổi bảng, cột, PK, FK, datatype, UNIQUE, NOT NULL hoặc DEFAULT.
- Không sửa/xóa migration cũ.
- Nếu implementation phát hiện schema hiện tại không đủ cho một Requirement, dừng và lập Impact Analysis mới.

## API impact

Dự kiến bổ sung REST API cho:

- Product list/search/create/update/status.
- Category list/create/update/status nếu status được dùng theo business rule hiện có.
- Supplier list/search/create/update.
- Inventory current-stock read.
- Invoice search/detail read.

Đường dẫn và DTO contract sẽ được khóa theo Requirement trước khi code; mọi endpoint Admin phải được backend bảo vệ bằng role `ADMIN` hoặc quyền actor đã được báo cáo xác nhận.

## UI impact

- Menu Admin chuyển từ nút placeholder sang route thật cho module đã xác nhận.
- Form và bảng gọi REST API thật; có trạng thái loading, empty, validation và error.
- Loại bỏ các KPI, biểu đồ, sản phẩm bán chạy, cảnh báo tồn và lối tắt đang dùng dữ liệu giả nếu chưa có API/Requirement hợp lệ.
- Không hiển thị số liệu không tồn tại trong database.

## Test

- Validation DTO và business rule ở Service.
- Repository query với MySQL/Flyway thực.
- API happy path, invalid input, not found, duplicate và authorization 401/403.
- Kiểm tra dữ liệu ghi/đọc khớp MySQL.
- Frontend production build và browser integration gọi API thật.

## Regression Test

- Authentication/login/logout/JWT/role guard hiện có.
- Entity mapping của toàn bộ 11 bảng.
- Flyway migration từ database sạch.
- Các module không liên quan không bị sửa hoặc hỏng.

## Những phần không được sửa

- Báo cáo Word nguồn.
- Tên 11 bảng, field, PK/FK và database decisions đã khóa.
- Ba role `ADMIN`, `SALES_STAFF`, `WAREHOUSE_STAFF`.
- Authentication/JWT contract đang hoạt động nếu không có dependency bắt buộc.
- Sales, Purchase Receipt, Customer và nghiệp vụ kho ghi dữ liệu ngoài phạm vi task đã xác nhận.
- Không thêm invoice details, inventory, roles, permissions, stock histories hoặc bảng thứ 12.

## Rủi ro

- Dashboard hiện hiển thị dữ liệu trình bày tĩnh nên có thể bị hiểu nhầm là dữ liệu vận hành.
- Quản lý tài khoản thiếu Requirement ID nên không thể đáp ứng traceability bắt buộc.
- Quyền Admin xem lịch sử kho chưa được ánh xạ nhất quán tới FR/Use Case.
- Báo cáo doanh thu còn mâu thuẫn về actor, loại báo cáo, export và tiêu chí hóa đơn hợp lệ; tự chọn sẽ thay đổi business rule.
- Trạng thái `status` là VARCHAR nhưng báo cáo chưa khóa toàn bộ tập giá trị cho Product, Category, Supplier, Order và Invoice; implementation chỉ được dùng giá trị đã có căn cứ hoặc phải xin quyết định.

## Kết quả

`PARTIAL — IMPLEMENTED; MYSQL/API/BROWSER RUNTIME NOT VERIFIED`

Đã thực hiện trong phạm vi không mâu thuẫn:

- Xóa dữ liệu dashboard tĩnh; Dashboard chỉ hiển thị số lượng đọc từ REST API.
- Hoàn thiện UI và REST API Admin cho danh mục, sản phẩm, nhà cung cấp, tồn hiện tại và tra cứu/chi tiết hóa đơn.
- Duy trì luồng Controller → Service → Repository → MySQL; không thêm bảng, role, migration hoặc thay đổi schema.
- Product CRUD không nhận số tồn; sản phẩm mới có tồn bằng `0`, tránh bỏ qua nghiệp vụ kho và lịch sử tồn.
- Toàn bộ `/api/admin/**` được backend giới hạn cho role `ADMIN`.
- Maven dùng local repository tại `D:/ktpm_project/.maven-cache`, không ghi dependency cache sang ổ C.

Xác minh đã PASS:

- Backend compile và 32 automated tests: entity mapping, application context, authentication/JWT, role authorization, request validation, business rule Admin và invoice query.
- Frontend production build bằng Vite.
- `git diff --check` không có whitespace error.

Chưa xác minh trong môi trường hiện tại:

- Flyway/JPA/query mới trên MySQL thật, vì không có MySQL lắng nghe tại cổng 3306.
- REST API Admin với dữ liệu MySQL thật và Edge browser integration; browser test đã được cập nhật nhưng cần backend + frontend + MySQL đang chạy và `DEMO_PASSWORD`.

Giữ ngoài phạm vi vì báo cáo còn mâu thuẫn/thiếu Requirement: quản lý tài khoản, Admin xem lịch sử kho và FR-RPT-01..05. Không hiển thị placeholder hoặc dữ liệu giả cho các phần này.

