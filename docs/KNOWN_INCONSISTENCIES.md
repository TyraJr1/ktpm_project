# Known Inconsistencies

Các mục dưới đây chỉ ghi nhận mâu thuẫn trong source document. Không mục nào được xem là đã giải quyết và không được dùng để tự chọn logic triển khai.

## INC-001 FR-AUTH-03 bị đổi nội dung trong bảng Requirement theo actor

**SOURCE A:** Chương II, Yêu cầu chức năng hệ thống: `FR-AUTH-03: Hệ thống xác định quyền dựa trên vai trò người dùng.`

**SOURCE B:** Chương III, Bảng 3.5, 3.6 và 3.7 theo ba actor: `FR-AUTH-03: Hệ thống cho phép người dùng đăng xuất.`

**CONFLICT:** Cùng một mã FR được dùng cho hai nội dung khác nhau; nội dung đăng xuất đã thuộc FR-AUTH-02.

**IMPACT IF IMPLEMENTED:** Authorization có thể bị thiếu hoặc FR-AUTH-02/03 bị triển khai, test và truy vết sai.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận nội dung chuẩn của FR-AUTH-03 và sửa đồng bộ Requirement theo actor, Use Case, thiết kế, code và test ở bước sau.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-002 Use Case đăng xuất của Admin map sai FR

**SOURCE A:** Chương II: FR-AUTH-02 là đăng xuất; FR-AUTH-03/04 là xác định và kiểm soát quyền.

**SOURCE B:** Chương III, Bảng 3.2: Admin UC-02 “Đăng xuất” map `FR-AUTH-03, FR-AUTH-04`.

**CONFLICT:** UC đăng xuất của Admin không map FR-AUTH-02 như UC đăng xuất của hai actor còn lại.

**IMPACT IF IMPLEMENTED:** Thiếu traceability và test đăng xuất cho Admin; authorization có thể bị gắn nhầm vào logout.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận UC-02 Admin phải map FR-AUTH-02 hay có thêm hành vi authorization nào khác.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-003 Mã actor Nhân viên kho

**SOURCE A:** Bảng 3.1 ghi `ACT-01` cho Quản lý và `ACT-02` cho Nhân viên bán hàng.

**SOURCE B:** Cùng bảng ghi `ACT-0` cho Nhân viên kho.

**CONFLICT:** Mã actor thứ ba bị thiếu chữ số và không theo chuỗi mã hiện có.

**IMPACT IF IMPLEMENTED:** Tài liệu, authorization, test và traceability có thể tham chiếu hai mã khác nhau.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận có sửa thành `ACT-03` hay giữ `ACT-0`.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-004 Use Case cập nhật tồn kho sau bán map FR không tồn tại

**SOURCE A:** Chương II định nghĩa `FR-SAL-07: Hệ thống cập nhật tồn kho sau giao dịch thành công.`

**SOURCE B:** Chương III, Bảng 3.3, UC-14 “Cập nhật tồn kho sau bán hàng” map `FR-BIL-07`.

**CONFLICT:** Không có FR-BIL-07 trong nhóm Invoice; chức năng khớp nội dung FR-SAL-07.

**IMPACT IF IMPLEMENTED:** Logic trừ kho và test có thể bị bỏ sót hoặc gắn sai module hóa đơn.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận mã FR đúng cho UC-14 và cập nhật tất cả artefact phụ thuộc.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-005 Quản lý tài khoản chưa có nhóm FR riêng

**SOURCE A:** Chương I và Bảng 2.1 xác định tạo, sửa, xóa tài khoản và phân quyền là chức năng của Quản lý.

**SOURCE B:** Bộ Functional Requirement chính chỉ có Authentication/Authorization, Product, Inventory, Supplier, Customer, Sales, Invoice và Reporting; không có nhóm Account Management cho CRUD tài khoản.

**CONFLICT:** Chức năng nằm trong phạm vi nghiệp vụ nhưng thiếu Requirement ID để truy vết.

**IMPACT IF IMPLEMENTED:** Không thể truy vết đầy đủ từ requirement đến API/service/UI/test; tự tạo mã FR sẽ vi phạm quy tắc dự án.

**RECOMMENDED RESOLUTION:** Người dùng quyết định bổ sung/sửa bộ FR trong tài liệu nguồn trước khi triển khai quản lý tài khoản.

**USER DECISION 02/11/2026:** Triển khai danh sách/tìm kiếm, tạo, sửa thông tin/role/status, đổi mật khẩu và xóa có điều kiện; chỉ dùng ba role hiện có; mật khẩu tối thiểu 8 ký tự có chữ hoa, chữ thường và số; không đổi schema. Trace bằng `USR-ADM-ACC-01..05` thay vì tự thêm mã FR vào Word.

**STATUS = RESOLVED FOR IMPLEMENTATION — WORD SYNC STILL REQUIRED**

## INC-006 Kiểm kê kho chưa có FR và Use Case riêng

**SOURCE A:** Bảng 2.3 và mô tả vai trò Nhân viên kho có nghiệp vụ Kiểm kê, công thức chênh lệch và rule ghi nhận.

**SOURCE B:** Nhóm Inventory chỉ có FR-INV-01 đến FR-INV-06; đặc tả Use Case Nhân viên kho không có Use Case Kiểm kê riêng.

**CONFLICT:** Nghiệp vụ kiểm kê có trong phạm vi actor nhưng thiếu FR/Use Case trực tiếp.

**IMPACT IF IMPLEMENTED:** Không có contract và test chuẩn cho kiểm kê; dễ bị gộp tùy ý vào điều chỉnh kho.

**RECOMMENDED RESOLUTION:** Người dùng quyết định kiểm kê là Use Case/FR riêng hay là một phần được đặc tả rõ của FR-INV-04.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-007 Phạm vi báo cáo doanh thu rộng hơn nhóm FR-RPT

**SOURCE A:** FR-RPT-01..05 chỉ nêu khoảng thời gian, tổng doanh thu, số hóa đơn, sản phẩm bán chạy và tồn kho.

**SOURCE B:** Quy trình và Hình 3.10 có báo cáo theo thời gian, sản phẩm, khách hàng, nhân viên; có in/tải file. Thiết kế xử lý còn nêu lợi nhuận, filter theo nhân viên/danh mục và export PDF/Excel.

**CONFLICT:** Activity/Processing Design mô tả chiều phân tích và output không có FR tương ứng.

**IMPACT IF IMPLEMENTED:** Có thể tự thêm chức năng báo cáo, export hoặc dữ liệu giá vốn ngoài phạm vi FR đã phê duyệt.

**RECOMMENDED RESOLUTION:** Người dùng xác định chính xác các loại báo cáo, filter và quyền export được giữ trong scope, rồi đồng bộ FR/Use Case/Activity/Database.

**USER DECISION 02/11/2026:** Chỉ triển khai FR-RPT-01..05: khoảng ngày, tổng doanh thu, số hóa đơn, sản phẩm bán chạy và tồn kho. Không triển khai lợi nhuận, báo cáo theo khách hàng/nhân viên hoặc export PDF/Excel.

**STATUS = RESOLVED FOR IMPLEMENTATION — WORD SYNC STILL REQUIRED**

## INC-008 Spring Security có nhưng Spring Boot chưa được xác định

**SOURCE A:** Chương I ghi Java, Spring Security, JWT và REST API.

**SOURCE B:** Chương I–III không ghi rõ Spring Boot là nền tảng backend.

**CONFLICT:** Framework bảo mật được nêu nhưng nền tảng runtime/application framework không được chốt.

**IMPACT IF IMPLEMENTED:** Tự tạo Spring Boot project hoặc `pom.xml` sẽ là quyết định kiến trúc/công nghệ chưa có căn cứ rõ trong báo cáo.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận có sử dụng Spring Boot và phiên bản/cấu hình ở bước đặc tả tiếp theo.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-009 Kiểu dữ liệu ID không đồng nhất

**SOURCE A:** Hình 3.11 ERD và phần Cấu trúc bảng dùng `BIGINT` cho PK/FK ID.

**SOURCE B:** Hình 3.12 Database Diagram dùng `INT` cho PK/FK ID.

**CONFLICT:** Hai sơ đồ database của cùng báo cáo dùng kiểu ID khác nhau.

**IMPACT IF IMPLEMENTED:** Migration, Entity, FK, DTO và API có thể không tương thích hoặc phải đổi datatype về sau.

**RECOMMENDED RESOLUTION:** Người dùng chọn `INT` hoặc `BIGINT` làm chuẩn rồi cập nhật ERD, Diagram và cấu trúc bảng đồng bộ.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-010 sales_orders customer_id và khách mua lẻ

**SOURCE A:** Business Rule bán hàng nói khách hàng có thể có hoặc không đối với khách mua lẻ; Processing Design cũng cho `Customer ID hoặc khách lẻ`.

**SOURCE B:** ERD và bảng cấu trúc `sales_orders.customer_id` ghi không cho NULL; Database Diagram không hiện ký hiệu NN ở trường này.

**CONFLICT:** Nghiệp vụ cho phép không chọn khách nhưng một số biểu diễn database bắt buộc customer_id.

**IMPACT IF IMPLEMENTED:** Không thể lưu đơn khách lẻ nếu FK bắt buộc, hoặc phải tự tạo khách mặc định không có requirement.

**RECOMMENDED RESOLUTION:** Người dùng quyết định cho `customer_id` NULL hay định nghĩa rõ mô hình khách lẻ; không tự tạo customer mặc định.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-011 Tính nguyên tử của bán hàng không khớp các quy trình tách rời

**SOURCE A:** Bảng 2.2 quy định chỉ khi tạo đơn, tạo hóa đơn và cập nhật tồn kho đều thành công thì giao dịch hoàn tất; lỗi phải rollback. Quality Requirement cũng cấm tình trạng đã trừ kho nhưng chưa có hóa đơn.

**SOURCE B:** Hình 3.7 kết thúc ngay sau xác nhận bán hàng, không thể hiện tạo hóa đơn hoặc cập nhật tồn; Hình 3.8 lập hóa đơn là quy trình riêng bắt đầu từ tra cứu đơn.

**CONFLICT:** Business Rule yêu cầu một transaction nguyên tử nhưng Activity Diagram chia thành các quy trình có điểm kết thúc độc lập.

**IMPACT IF IMPLEMENTED:** Có thể sinh đơn đã xác nhận nhưng chưa hóa đơn/chưa trừ kho, hoặc giao dịch không rollback toàn bộ.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận ranh giới transaction và thứ tự chuẩn giữa xác nhận đơn, tạo hóa đơn, cập nhật tồn.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-012 Nhập hàng kết thúc trước bước cập nhật tồn kho

**SOURCE A:** `FR-INV-03` và UC-22 quy định cập nhật tồn sau khi phiếu nhập được xác nhận; bảng nghiệp vụ còn yêu cầu tăng tồn và lưu lịch sử.

**SOURCE B:** Hình 3.6 kết thúc ngay sau “Xác nhận phiếu nhập”; narrative nói cập nhật tồn và lịch sử xử lý ở quy trình quản lý kho riêng.

**CONFLICT:** Sơ đồ nhập hàng không thể hiện hành vi/hậu điều kiện bắt buộc của FR-INV-03 và chưa rõ transaction có liên tục hay không.

**IMPACT IF IMPLEMENTED:** Phiếu đã xác nhận có thể tồn tại mà tồn kho/lịch sử chưa cập nhật.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận cập nhật tồn/lịch sử là bước bắt buộc trong cùng transaction xác nhận phiếu hay là quy trình riêng có cơ chế bảo đảm nhất quán.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-013 Actor của Activity Diagram báo cáo doanh thu

**SOURCE A:** Chương II xác định đối tượng tham gia quy trình báo cáo doanh thu là Quản lý/Admin; thiết kế xử lý cũng yêu cầu quyền Quản lý/Admin.

**SOURCE B:** Tiêu đề Hình 3.10 ghi “Quy trình báo cáo doanh thu của Nhân viên bán hàng”.

**CONFLICT:** Actor trên hình khác actor trong text và rule phân quyền.

**IMPACT IF IMPLEMENTED:** Nhân viên bán hàng có thể được cấp nhầm quyền báo cáo quản trị.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận actor của Hình 3.10 và cập nhật sơ đồ/role matrix.

**USER DECISION 02/11/2026:** Báo cáo quản trị chỉ dành cho role `ADMIN`; không cấp cho `SALES_STAFF`.

**STATUS = RESOLVED FOR IMPLEMENTATION — WORD SYNC STILL REQUIRED**

## INC-014 Mã FR của UC báo cáo tồn kho bị cắt

**SOURCE A:** Bộ FR chính định nghĩa `FR-RPT-05: Hệ thống hiển thị báo cáo tồn kho.`

**SOURCE B:** Bảng 3.2, Admin UC-16 “Xem báo cáo tồn kho” chỉ ghi `FR-RPT-`.

**CONFLICT:** Traceability trong Use Case không có mã FR hoàn chỉnh.

**IMPACT IF IMPLEMENTED:** UC-16 không thể truy vết máy móc/chính xác tới requirement và test.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận mã đầy đủ có phải FR-RPT-05 hay không.

**USER DECISION 02/11/2026:** Ánh xạ Admin UC-16 tới `FR-RPT-05`; không sửa file Word trong task code.

**STATUS = RESOLVED FOR IMPLEMENTATION — WORD SYNC STILL REQUIRED**

## INC-015 Hệ mã FR trong Thiết kế xử lý khác bộ FR chính

**SOURCE A:** Bộ FR chính dùng các nhóm `FR-AUTH`, `FR-PRO`, `FR-INV`, `FR-SUP`, `FR-CUS`, `FR-SAL`, `FR-BIL`, `FR-RPT`.

**SOURCE B:** Thiết kế xử lý dùng `FR-DN-*`, `FR-NH-*`, `FR-BH-*` và các mã chung `FR-03`, `FR-04`, `FR-05`, `FR-06`, `FR-07`, `FR-09`, `FR-10`.

**CONFLICT:** Hai hệ mã Requirement không có bảng ánh xạ chính thức và một số nội dung chỉ xuất hiện ở hệ mã thứ hai.

**IMPACT IF IMPLEMENTED:** Không thể bảo đảm traceability; dễ tự phát minh requirement hoặc triển khai nhầm phạm vi.

**RECOMMENDED RESOLUTION:** Người dùng chọn một hệ mã chuẩn và cung cấp ánh xạ/điều chỉnh chính thức cho phần Thiết kế xử lý.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-016 Thiết kế xử lý dùng schema ngoài 11 bảng database

**SOURCE A:** ERD, Database Diagram và Cấu trúc bảng xác định 11 bảng: `users`, `categories`, `products`, `suppliers`, `customers`, `sales_orders`, `order_details`, `purchase_receipts`, `purchase_receipt_details`, `stock_transactions`, `invoices`.

**SOURCE B:** Thiết kế xử lý dùng tên/structure khác như `NGUOI_DUNG`, `PHIEN_DANG_NHAP`, `NHAT_KY_DANG_NHAP`, `PHIEU_NHAP`, `CHI_TIET_PHIEU_NHAP`, `SAN_PHAM`, `THE_KHO`, `NHA_CUNG_CAP`, `HOA_DON`, `CHI_TIET_HOA_DON`, `KHACH_HANG`, `Stock_Histories`, `Invoice_Details` và các field không có trong baseline.

**CONFLICT:** Processing Design tham chiếu bảng và cột không tồn tại trong database baseline, đồng thời đổi naming convention.

**IMPACT IF IMPLEMENTED:** Tự thêm bảng/cột hoặc đổi tên schema sẽ vi phạm Database Safety và làm mất đồng bộ ERD.

**RECOMMENDED RESOLUTION:** Người dùng quyết định schema chuẩn; sau đó cập nhật Processing Design hoặc thực hiện Impact Analysis và sửa ERD/Diagram/Cấu trúc bảng chính thức.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-017 Thiết kế xử lý thêm nghiệp vụ chưa có trong FR chính

**SOURCE A:** FR chính không đặc tả khóa 5 lần/15 phút, tối đa 3 phiên, login audit, VAT/chiết khấu, công nợ, giá vốn bình quân, bán ghi nợ, hủy hóa đơn, lợi nhuận hoặc export PDF/Excel.

**SOURCE B:** Thiết kế xử lý đăng nhập, nhập hàng, bán hàng, hóa đơn và báo cáo mô tả các hành vi trên cùng rule/công thức riêng.

**CONFLICT:** Processing Design mở rộng nghiệp vụ vượt bộ Requirement và database chính.

**IMPACT IF IMPLEMENTED:** Dễ thêm chức năng, field, bảng, role rule và test ngoài báo cáo baseline được chấp thuận.

**RECOMMENDED RESOLUTION:** Người dùng quyết định từng nội dung được nâng thành Requirement chính hay bị loại khỏi Processing Design; không code trước quyết định.

**USER DECISION 02/11/2026:** Trong phạm vi Admin chỉ nâng quy tắc mật khẩu tối thiểu 8 ký tự có chữ hoa, chữ thường và số, và trạng thái invoice báo cáo là `COMPLETED`. Các nghiệp vụ mở rộng còn lại tiếp tục không được triển khai.

**STATUS = PARTIALLY RESOLVED — REMAINING PROCESSING-DESIGN ITEMS OUT OF SCOPE**

## INC-018 Actor nhập hàng không thống nhất

**SOURCE A:** Chương II và Activity Diagram nhập hàng xác định Nhân viên kho là đối tượng tham gia.

**SOURCE B:** Thiết kế xử lý nhập hàng ghi người có quyền là “Quản lý / Nhân viên kho”.

**CONFLICT:** Quyền nhập hàng của Admin chỉ xuất hiện trong Processing Design.

**IMPACT IF IMPLEMENTED:** Role matrix và authorization endpoint có thể cấp quyền rộng hơn phạm vi actor chính.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận Admin có trực tiếp thực hiện nhập hàng hay chỉ quản lý/xem dữ liệu.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-019 Xử lý sản phẩm trùng trong phiếu nhập chưa được chốt

**SOURCE A:** Bảng 2.3 yêu cầu nếu cùng sản phẩm xuất hiện nhiều lần thì “hệ thống cần gộp hoặc không cho phép trùng tùy thiết kế đã chọn”.

**SOURCE B:** Không có quyết định thiết kế nào trong FR, Use Case, Activity Diagram hoặc cấu trúc database chốt một trong hai phương án.

**CONFLICT:** Một Business Rule chứa hai phương án loại trừ nhau.

**IMPACT IF IMPLEMENTED:** UI, Service, unique constraint và test có thể khác nhau tùy lựa chọn.

**RECOMMENDED RESOLUTION:** Người dùng chọn gộp dòng hay cấm trùng và cập nhật tài liệu trước khi code.

**STATUS = OPEN — REQUIRE USER DECISION**

## INC-020 Tiêu chí chống trùng khách hàng chưa xác định

**SOURCE A:** Bảng 2.2 yêu cầu không tạo khách hàng trùng theo tiêu chí hệ thống quy định, ví dụ phone hoặc email.

**SOURCE B:** Cấu trúc bảng `customers` không đặt UNIQUE cho phone/email và không có rule chốt field hoặc tổ hợp field định danh.

**CONFLICT:** Business Rule phụ thuộc một tiêu chí chưa được định nghĩa.

**IMPACT IF IMPLEMENTED:** Service validation và database constraint có thể không đồng nhất; dữ liệu hợp lệ ở một tầng nhưng bị coi là trùng ở tầng khác.

**RECOMMENDED RESOLUTION:** Người dùng xác nhận tiêu chí định danh/duplicate chính xác và chính sách với giá trị NULL.

**STATUS = OPEN — REQUIRE USER DECISION**
