# Report Baseline

## Trạng thái và nguồn đặc tả

- Trạng thái: `SPECIFIED WITH OPEN INCONSISTENCIES`.
- Source document duy nhất: `D:\ktpm_project\docs\report_KTPM_chương_1-2-3 up(tam).docx`.
- Phạm vi đã đọc: toàn bộ Chương I, Chương II và Chương III, gồm text, bảng và các hình Use Case, Activity Diagram, ERD, Database Diagram.
- SHA-256 tại thời điểm đọc: `4696E2404518895D343B5C9E2C67D55DED669158B940A473E4E6693C917EF91B`.
- Tài liệu nguồn chỉ được đọc, không sửa, đổi tên, ghi đè hoặc lưu lại.

## Tên đề tài

**HỆ THỐNG QUẢN LÝ BÁN HÀNG CHO CỬA HÀNG VĂN PHÒNG PHẨM**

## Mục tiêu

Số hóa và quản lý tập trung nghiệp vụ của một cửa hàng bán lẻ: sản phẩm, danh mục, kho, nhập/xuất hàng, nhà cung cấp, khách hàng, bán hàng, hóa đơn, tài khoản, phân quyền và báo cáo. Hệ thống kiểm tra tồn kho, tính tiền, cập nhật tồn kho sau nhập/bán, lưu hóa đơn, tổng hợp doanh thu và hỗ trợ truy vết biến động kho.

## Phạm vi và đối tượng sử dụng

- Cửa hàng bán lẻ quy mô nhỏ và vừa.
- Một cửa hàng hoặc một điểm bán; không quản lý chuỗi nhiều chi nhánh.
- Ba actor đăng nhập: Quản lý/Admin, Nhân viên bán hàng, Nhân viên kho.
- Khách hàng chỉ là dữ liệu nghiệp vụ; báo cáo nêu rõ khách hàng không bắt buộc có tài khoản đăng nhập và không xác định khách hàng là actor.

## Actor và role

| Mã trong báo cáo | Actor / role | Trách nhiệm chính |
|---|---|---|
| `ACT-01` | Quản lý (Administrator/Admin) | Quản lý tài khoản, phân quyền, sản phẩm, danh mục, nhà cung cấp; theo dõi kho; tra cứu hóa đơn; xem báo cáo. |
| `ACT-02` | Nhân viên bán hàng | Tìm sản phẩm, kiểm tra tồn, quản lý dữ liệu khách hàng, lập đơn, tính tiền, xác nhận giao dịch, lập và tra cứu hóa đơn. |
| `ACT-0` trong Bảng 3.1, dự kiến `ACT-03` nhưng chưa được phép tự sửa | Nhân viên kho | Xem sản phẩm/tồn kho, tạo và xác nhận phiếu nhập, xuất kho, kiểm kê, điều chỉnh và xem lịch sử kho. |

Không có role khách hàng. Mã `ACT-0` là inconsistency mở.

## Kiến trúc

Kiến trúc được đặc tả là Multi-Tier kết hợp MVC:

```text
Presentation Layer
├── View
└── Controller
Business Layer
└── Service
Data Access Layer
└── Repository
Database Tier
└── MySQL
```

Luồng bắt buộc của dự án:

```text
Frontend → REST Controller → Service / Business Layer → Repository / Data Access Layer → MySQL
```

- Presentation/View/Controller nhận dữ liệu, kiểm tra đầu vào cơ bản, gọi tầng nghiệp vụ và hiển thị/trả kết quả; không chứa SQL hoặc business rule phức tạp.
- Service/Business Layer xử lý business rule, tính toán, authorization, transaction và điều phối repository.
- Repository/Data Access Layer truy vấn, thêm và cập nhật dữ liệu.
- Client không truy cập MySQL trực tiếp.

## Nền tảng kỹ thuật

- Backend: Java.
- Frontend: HTML, CSS; Node.js và npm hỗ trợ môi trường phát triển/build frontend.
- Giao tiếp: REST API qua HTTP/HTTPS, dữ liệu JSON.
- Database: MySQL.
- Bảo mật: Authentication, Authorization, Spring Security và JWT.
- Báo cáo có nhắc Spring Security và hệ sinh thái Spring nhưng không ghi rõ Spring Boot.

## Nhóm chức năng trong phạm vi

1. Quản lý sản phẩm và trạng thái sản phẩm.
2. Quản lý danh mục.
3. Quản lý kho, tồn kho và lịch sử biến động.
4. Nhập hàng, lập/xác nhận phiếu nhập.
5. Xuất và điều chỉnh kho.
6. Quản lý nhà cung cấp.
7. Quản lý và tra cứu khách hàng.
8. Bán hàng, lập đơn và xác nhận giao dịch.
9. Tạo, lưu, xem và tra cứu hóa đơn.
10. Quản lý tài khoản và phân quyền.
11. Báo cáo doanh thu, số lượng hóa đơn, sản phẩm bán chạy và tồn kho.

## Chức năng ngoài phạm vi

- Quản lý nhiều chi nhánh.
- Website thương mại điện tử cho khách hàng tự đặt hàng.
- Giao hàng và theo dõi vận chuyển.
- Thanh toán trực tuyến với ngân hàng hoặc ví điện tử.
- Hệ thống kế toán hoàn chỉnh.
- AI dự báo doanh số.
- Recommendation system.
- Microservices.
- Quản lý chuỗi cung ứng quy mô lớn.

## Functional Requirement chính

### Authentication và Authorization

| ID | Nội dung |
|---|---|
| FR-AUTH-01 | Hệ thống cho phép người dùng đăng nhập bằng tài khoản hợp lệ. |
| FR-AUTH-02 | Hệ thống cho phép người dùng đăng xuất. |
| FR-AUTH-03 | Hệ thống xác định quyền dựa trên vai trò người dùng. |
| FR-AUTH-04 | Hệ thống từ chối truy cập các chức năng người dùng không được phép thực hiện. |

### Product Management

| ID | Nội dung |
|---|---|
| FR-PRO-01 | Hệ thống cho phép xem danh sách sản phẩm. |
| FR-PRO-02 | Hệ thống cho phép tạo sản phẩm. |
| FR-PRO-03 | Hệ thống cho phép cập nhật thông tin sản phẩm. |
| FR-PRO-04 | Hệ thống cho phép tìm kiếm sản phẩm. |
| FR-PRO-05 | Hệ thống cho phép thay đổi trạng thái hoạt động của sản phẩm. |
| FR-PRO-06 | Hệ thống cho phép quản lý danh mục sản phẩm. |

### Inventory

| ID | Nội dung |
|---|---|
| FR-INV-01 | Hệ thống hiển thị số lượng tồn kho của từng sản phẩm. |
| FR-INV-02 | Hệ thống cho phép tạo phiếu nhập. |
| FR-INV-03 | Hệ thống cập nhật tồn kho sau khi phiếu nhập được xác nhận. |
| FR-INV-04 | Hệ thống cho phép thực hiện xuất hoặc điều chỉnh kho theo quyền. |
| FR-INV-05 | Hệ thống lưu lịch sử biến động tồn kho. |
| FR-INV-06 | Hệ thống không cho phép xuất số lượng lớn hơn tồn kho. |

### Supplier

| ID | Nội dung |
|---|---|
| FR-SUP-01 | Hệ thống cho phép tạo nhà cung cấp. |
| FR-SUP-02 | Hệ thống cho phép cập nhật thông tin nhà cung cấp. |
| FR-SUP-03 | Hệ thống cho phép tra cứu nhà cung cấp. |

### Customer

| ID | Nội dung |
|---|---|
| FR-CUS-01 | Hệ thống cho phép tạo khách hàng. |
| FR-CUS-02 | Hệ thống cho phép cập nhật thông tin khách hàng. |
| FR-CUS-03 | Hệ thống cho phép tìm kiếm khách hàng. |
| FR-CUS-04 | Hệ thống hiển thị lịch sử mua hàng của khách hàng nếu có. |

### Sales

| ID | Nội dung |
|---|---|
| FR-SAL-01 | Hệ thống cho phép nhân viên tạo đơn hàng. |
| FR-SAL-02 | Hệ thống cho phép thêm sản phẩm vào đơn. |
| FR-SAL-03 | Hệ thống kiểm tra tồn kho trước khi xác nhận số lượng. |
| FR-SAL-04 | Hệ thống tự động tính thành tiền từng sản phẩm. |
| FR-SAL-05 | Hệ thống tự động tính tổng giá trị đơn hàng. |
| FR-SAL-06 | Hệ thống cho phép xác nhận giao dịch. |
| FR-SAL-07 | Hệ thống cập nhật tồn kho sau giao dịch thành công. |

### Invoice

| ID | Nội dung |
|---|---|
| FR-BIL-01 | Hệ thống tạo hóa đơn từ giao dịch bán hàng hoàn tất. |
| FR-BIL-02 | Hệ thống lưu thông tin hóa đơn. |
| FR-BIL-03 | Hệ thống cho phép tra cứu hóa đơn. |
| FR-BIL-04 | Hệ thống hiển thị chi tiết hóa đơn. |

### Reporting

| ID | Nội dung |
|---|---|
| FR-RPT-01 | Hệ thống cho phép chọn khoảng thời gian báo cáo. |
| FR-RPT-02 | Hệ thống tính tổng doanh thu trong khoảng thời gian. |
| FR-RPT-03 | Hệ thống thống kê số lượng hóa đơn. |
| FR-RPT-04 | Hệ thống thống kê sản phẩm bán chạy. |
| FR-RPT-05 | Hệ thống hiển thị báo cáo tồn kho. |

## Business Rule

### Xác thực, tài khoản và phân quyền

- Tài khoản và mật khẩu không được để trống; tài khoản phải tồn tại; mật khẩu phải khớp.
- Người dùng phải được xác thực trước khi dùng chức năng được bảo vệ.
- Quyền được xác định theo role; backend phải từ chối hành động không được cấp quyền.
- Chỉ Quản lý được tạo, sửa, xóa tài khoản và phân quyền.
- Username không được trống hoặc trùng; mật khẩu mới phải đáp ứng quy định bảo mật.
- Không xóa tài khoản đang đăng nhập hoặc tài khoản đã phát sinh giao dịch; phải xác nhận trước khi xóa.
- Khi thay đổi quyền, hệ thống phải cập nhật quyền tương ứng.
- Mật khẩu không được lưu plain text.

### Sản phẩm, danh mục và nhà cung cấp

- Mã sản phẩm duy nhất, không rỗng; tên không rỗng; giá bán `>= 0`; danh mục phải tồn tại; chỉ sản phẩm hoạt động được bán.
- Tên danh mục không rỗng; mã danh mục nếu có phải duy nhất; tên không nên trùng; danh mục gán cho sản phẩm phải tồn tại và hoạt động.
- Nhà cung cấp phải có tên; mã nếu có phải duy nhất; phone/email nếu có phải đúng định dạng; nhà cung cấp dùng trong phiếu nhập phải tồn tại và hoạt động.
- Tìm sản phẩm theo mã hoặc tên, có thể lọc trạng thái; kết quả phản ánh dữ liệu hiện tại.

### Khách hàng

- Khách hàng có thể có hoặc không đối với khách mua lẻ.
- Nếu chọn khách hàng thì bản ghi phải tồn tại và dùng thông tin hiện tại.
- Khi tạo khách hàng, tên theo yêu cầu hệ thống không được rỗng; phone/email nếu có phải đúng định dạng.
- Không tạo trùng theo tiêu chí định danh do hệ thống quy định, ví dụ phone hoặc email; tiêu chí cuối cùng chưa được chốt trong báo cáo.

### Đơn hàng, bán hàng và hóa đơn

- Đơn hàng phải có ít nhất một sản phẩm; sản phẩm phải tồn tại, hoạt động và được phép bán; đơn phải gắn với nhân viên thực hiện.
- Số lượng là số nguyên dương, `Quantity > 0` và `Quantity <= StockQuantity`.
- Xóa sản phẩm khỏi đơn phải tính lại tổng tiền; không xác nhận đơn rỗng.
- Thành tiền dòng = Đơn giá × Số lượng; tổng đơn = tổng thành tiền các dòng; người dùng không nhập tổng tiền trực tiếp.
- Chỉ khi tạo đơn, tạo hóa đơn và cập nhật tồn kho đều thành công thì giao dịch mới hoàn tất.
- Chỉ tạo hóa đơn từ đơn đã xác nhận; hóa đơn liên kết đúng đơn; tổng hóa đơn bằng tổng đơn.
- Nếu tạo hóa đơn hoặc cập nhật tồn kho thất bại thì rollback.
- Tra cứu hóa đơn theo mã/ngày; mã xác định duy nhất; khoảng ngày phải có bắt đầu `<=` kết thúc; chỉ hiển thị trong phạm vi quyền.

### Nhập hàng và kho

- Phiếu nhập cần nhà cung cấp hợp lệ, ít nhất một sản phẩm; phiếu chưa xác nhận không đổi tồn kho.
- Sản phẩm nhập phải tồn tại; số lượng nhập `> 0`; giá nhập `>= 0`.
- Nếu cùng sản phẩm xuất hiện nhiều lần trong một phiếu thì báo cáo chưa chốt giữa gộp và cấm trùng.
- Phiếu chỉ được xác nhận một lần. Sau xác nhận: `Tồn mới = Tồn hiện tại + Số lượng nhập`.
- Xuất kho yêu cầu `0 < Số lượng xuất <= Tồn hiện tại`; tồn kho không âm.
- Kiểm kê: số lượng thực tế là số nguyên không âm; `Chênh lệch = Thực tế - Hệ thống`; chênh lệch 0 không điều chỉnh, khác 0 phải ghi nhận.
- Điều chỉnh cần quyền và lý do; tồn sau điều chỉnh không âm; lưu người thực hiện, thời gian, lý do, giá trị trước/sau.
- Lịch sử kho cho phép lọc theo sản phẩm, loại giao dịch, thời gian; ngày bắt đầu `<=` ngày kết thúc; ghi sản phẩm, loại, lượng thay đổi, thời gian và người thực hiện.

### Báo cáo

- Chỉ hóa đơn hợp lệ và hoàn thành được tính; ngày bắt đầu `<=` ngày kết thúc.
- Doanh thu = tổng tiền các hóa đơn hợp lệ trong khoảng chọn.
- Số lượng hóa đơn = số hóa đơn hợp lệ trong khoảng chọn.
- Sản phẩm bán chạy được xếp giảm dần theo tổng số lượng bán từ đơn/hóa đơn hợp lệ.
- Chỉ người có quyền được trích xuất báo cáo; số liệu trích xuất phải khớp kết quả tính cùng khoảng thời gian.
- Báo cáo tồn kho dùng số lượng hiện tại và không âm.

### Business Rule được ghi riêng trong phần Thiết kế xử lý

Các rule dưới đây có trong báo cáo nhưng xung đột hoặc chưa truy vết được tới bộ FR/database chính, nên không được tự dùng để triển khai:

- `BR-DN-03`: chỉ tài khoản hoạt động được đăng nhập.
- `BR-DN-04`: sai mật khẩu 5 lần liên tiếp thì khóa 15 phút.
- `BR-DN-05`: thông báo lỗi chung, không lộ tài khoản có tồn tại hay không.
- `BR-DN-06`: phiên hết hạn sau 30 phút, tối đa 3 phiên.
- `BR-NH-02`: số lượng nhập lớn hơn 0.
- `BR-NH-09`: phiếu đã xác nhận không được sửa/xóa.
- `BR-NH-12`: lưu phiếu, cập nhật kho và công nợ trong một transaction.
- `BR-NH-14`: giá vốn theo bình quân gia quyền.
- `BR-BH-02`: không bán vượt tồn kho.
- `BR-BH-04`: giảm giá tối đa theo vai trò, 10% với nhân viên và 30% với quản lý.
- `BR-BH-08`: khách lẻ phải thanh toán đủ, không ghi nợ.
- `BR-BH-09`: hóa đơn đã tạo không được sửa, chỉ được hủy nếu có quyền.
- `BR-01`: không bán vượt tồn kho.
- `BR-02`: hóa đơn đã tạo không được sửa, chỉ được hủy nếu có quyền.
- `BR-03`: mọi thay đổi tồn kho phải ghi log vào `Stock_Histories`.
- `BR-06`: chỉ Quản lý/Admin truy cập báo cáo doanh thu.

## Quality Requirement

- **Usability:** giao diện rõ ràng, nhất quán; thao tác tìm sản phẩm, thêm vào đơn, kiểm tra tồn và thanh toán thuận tiện; lỗi dễ hiểu.
- **Performance:** tra cứu không làm gián đoạn bán hàng; danh sách lớn nên có phân trang/tìm kiếm.
- **Security:** xác thực trước chức năng quản trị; password không plain text; phân quyền theo role; client không truy cập database; kiểm tra quyền ở server.
- **Data Integrity:** đơn hàng, hóa đơn, tồn kho, phiếu nhập nhất quán; nghiệp vụ nhiều ghi database dùng transaction.
- **Reliability:** lỗi không để dữ liệu dở dang; không được trừ kho nếu hóa đơn chưa tạo.
- **Maintainability:** tổ chức code theo trách nhiệm; Controller không SQL; rule ở Service; Repository truy cập dữ liệu.
- **Scalability:** có thể mở rộng theo số sản phẩm, hóa đơn, người dùng và giao dịch.
- **Auditability:** lưu người thực hiện, thời gian, loại thao tác, lượng thay đổi và lý do điều chỉnh.

## Use Case

### Quản lý Admin

| Use Case | Tên | FR liên quan |
|---|---|---|
| UC-01 | Đăng nhập | FR-AUTH-01 |
| UC-02 | Đăng xuất | Báo cáo ghi FR-AUTH-03, FR-AUTH-04; inconsistency mở vì FR-AUTH-02 mới là đăng xuất |
| UC-03 | Quản lý quyền truy cập | FR-AUTH-03, FR-AUTH-04 |
| UC-04 | Xem danh sách sản phẩm | FR-PRO-01 |
| UC-05 | Tạo sản phẩm | FR-PRO-02 |
| UC-06 | Cập nhật sản phẩm | FR-PRO-03 |
| UC-07 | Tìm kiếm sản phẩm | FR-PRO-04 |
| UC-08 | Thay đổi trạng thái sản phẩm | FR-PRO-05 |
| UC-09 | Quản lý danh mục sản phẩm | FR-PRO-06 |
| UC-10 | Xem tồn kho | FR-INV-01 |
| UC-11 | Quản lý nhà cung cấp | FR-SUP-01, FR-SUP-02, FR-SUP-03 |
| UC-12 | Chọn thời gian báo cáo | FR-RPT-01 |
| UC-13 | Xem báo cáo doanh thu | FR-RPT-02 |
| UC-14 | Thống kê số lượng hóa đơn | FR-RPT-03 |
| UC-15 | Xem sản phẩm bán chạy | FR-RPT-04 |
| UC-16 | Xem báo cáo tồn kho | Báo cáo ghi dở `FR-RPT-`; dự kiến liên quan FR-RPT-05 nhưng chưa được phép tự sửa |

### Nhân viên bán hàng

| Use Case | Tên | FR liên quan |
|---|---|---|
| UC-01 | Đăng nhập | FR-AUTH-01 |
| UC-02 | Đăng xuất | FR-AUTH-02 |
| UC-03 | Kiểm tra quyền truy cập | FR-AUTH-03, FR-AUTH-04 |
| UC-04 | Xem danh sách sản phẩm | FR-PRO-01 |
| UC-05 | Tìm kiếm sản phẩm | FR-PRO-04 |
| UC-06 | Tạo khách hàng | FR-CUS-01 |
| UC-07 | Cập nhật khách hàng | FR-CUS-02 |
| UC-08 | Tìm kiếm khách hàng | FR-CUS-03 |
| UC-09 | Xem lịch sử mua hàng | FR-CUS-04 |
| UC-10 | Lập đơn hàng | FR-SAL-01, FR-SAL-02 |
| UC-11 | Kiểm tra tồn kho khi lập đơn | FR-SAL-03 |
| UC-12 | Tính tiền đơn hàng | FR-SAL-04, FR-SAL-05 |
| UC-13 | Xác nhận giao dịch | FR-SAL-06 |
| UC-14 | Cập nhật tồn kho sau bán hàng | Báo cáo ghi FR-BIL-07; inconsistency mở vì FR chính là FR-SAL-07 |
| UC-15 | Tạo hóa đơn | FR-BIL-01 |
| UC-16 | Tra cứu hóa đơn | FR-BIL-02, FR-BIL-03 |
| UC-17 | Xem chi tiết hóa đơn | FR-BIL-04 |

### Nhân viên kho

| Use Case | Tên | FR liên quan |
|---|---|---|
| UC-01 | Đăng nhập | FR-AUTH-01 |
| UC-02 | Đăng xuất | FR-AUTH-02 |
| UC-03 | Kiểm tra quyền truy cập | FR-AUTH-03, FR-AUTH-04 |
| UC-18 | Xem danh sách sản phẩm | FR-PRO-01 |
| UC-19 | Tìm kiếm sản phẩm | FR-PRO-04 |
| UC-20 | Xem tồn kho | FR-INV-01 |
| UC-21 | Tạo phiếu nhập kho | FR-INV-02 |
| UC-22 | Xác nhận nhập kho | FR-INV-03 |
| UC-23 | Xuất kho | FR-INV-04, FR-INV-06 |
| UC-24 | Điều chỉnh tồn kho | FR-INV-04, FR-INV-05 |
| UC-25 | Xem lịch sử biến động tồn kho | FR-INV-05 |
| UC-26 | Tra cứu nhà cung cấp | FR-SUP-03 |

Kiểm kê được nêu trong chức năng actor và quy trình nghiệp vụ nhưng không có Use Case riêng hoặc FR-INV riêng.

## Sáu Activity Diagram

### 1 Sơ đồ đăng nhập Hình 3.5

- **Actor:** Admin, Nhân viên bán hàng, Nhân viên kho; swimlane Người dùng, Hệ thống, Cơ sở dữ liệu.
- **Mục đích:** xác thực tài khoản, tạo JWT, lấy role và chuyển tới trang chính theo quyền.
- **Bắt đầu:** người dùng truy cập màn hình đăng nhập.
- **Activity chính:** nhập username/password → kiểm tra đầu vào → tìm username/truy vấn `users` → kiểm tra trạng thái → lấy `password_hash` và so sánh → tạo JWT/lấy role → trả thông tin và token → đăng nhập thành công.
- **Decision và nhánh:** dữ liệu hợp lệ?; tài khoản tồn tại?; tài khoản hoạt động?; mật khẩu đúng? Mỗi nhánh `Không` hiển thị lỗi tương ứng và quay lại nhập; nhánh `Có` tiếp tục.
- **Kết thúc:** đăng nhập thành công, chuyển tới trang chính.
- **FR:** FR-AUTH-01, FR-AUTH-03, FR-AUTH-04.
- **Rule:** dữ liệu bắt buộc, tài khoản tồn tại/hoạt động, password khớp; quyền theo role.

### 2 Sơ đồ nhập hàng Hình 3.6

- **Actor:** Nhân viên kho.
- **Mục đích:** lập và xác nhận phiếu nhập từ nhà cung cấp.
- **Bắt đầu:** đăng nhập hệ thống.
- **Activity chính:** chọn Nhập hàng → chọn nhà cung cấp → chọn sản phẩm → nhập số lượng/giá → tạo phiếu → kiểm tra.
- **Decision:** thông tin phiếu nhập hợp lệ? `Không` → kiểm tra và nhập lại → quay về nhập số lượng/giá; `Có` → xác nhận phiếu.
- **Kết thúc:** ngay sau xác nhận phiếu nhập.
- **FR:** FR-INV-02, FR-INV-03; tra cứu nhà cung cấp liên quan FR-SUP-03.
- **Rule:** nhà cung cấp/sản phẩm hợp lệ; có ít nhất một sản phẩm; số lượng `> 0`; giá `>= 0`; chỉ phiếu xác nhận mới tăng tồn.
- **Lưu ý:** sơ đồ không thể hiện bước cập nhật tồn và ghi lịch sử sau xác nhận, tạo inconsistency với FR-INV-03.

### 3 Sơ đồ bán hàng Hình 3.7

- **Actor:** Nhân viên bán hàng.
- **Mục đích:** chọn hàng, lập đơn và xác nhận bán.
- **Bắt đầu:** đăng nhập hệ thống.
- **Activity chính:** chọn Bán hàng → tìm/chọn sản phẩm → kiểm tra tồn → nhập số lượng và khách hàng → tạo đơn → kiểm tra đơn.
- **Decision:** thông tin hợp lệ? `Không` → chỉnh sửa/nhập lại → quay về kiểm tra; `Có` → xác nhận bán hàng.
- **Kết thúc:** sau hoạt động “Hoàn thành”.
- **FR:** FR-PRO-04, FR-SAL-01 đến FR-SAL-06; dữ liệu khách hàng liên quan FR-CUS; FR-SAL-07 và FR-BIL chưa được thể hiện trong sơ đồ.
- **Rule:** đơn không rỗng; sản phẩm hoạt động; số lượng dương và không vượt tồn; khách hàng không bắt buộc; tổng tiền do hệ thống tính.

### 4 Sơ đồ lập hóa đơn Hình 3.8

- **Actor:** sơ đồ ghi Nhân viên bán hàng; phần quy trình liệt kê Quản lý/Admin và Nhân viên bán hàng.
- **Mục đích:** tạo hóa đơn từ đơn đã xác nhận.
- **Bắt đầu:** đăng nhập hệ thống.
- **Activity chính:** chọn Lập hóa đơn → tra cứu/chọn đơn → kiểm tra đơn → chọn phương thức thanh toán → tạo hóa đơn → kiểm tra hóa đơn.
- **Decision:** thông tin hợp lệ? `Không` → chỉnh sửa/nhập lại → kiểm tra lại; `Có` → xác nhận lập hóa đơn.
- **Kết thúc:** sau hoạt động “Hoàn thành”.
- **FR:** FR-BIL-01 đến FR-BIL-04.
- **Rule:** đơn phải xác nhận; hóa đơn liên kết đúng đơn; tổng hóa đơn bằng tổng đơn; lỗi tạo hóa đơn/cập nhật kho phải rollback.

### 5 Sơ đồ quản lý kho Hình 3.9

- **Actor:** Nhân viên kho.
- **Mục đích:** xem tồn, cập nhật tồn và xem lịch sử kho.
- **Bắt đầu:** đăng nhập và chọn Quản lý kho.
- **Decision 1:** thực hiện chức năng nào? Ba nhánh: Xem tồn kho; Cập nhật tồn kho; Xem lịch sử kho.
- **Nhánh xem tồn:** chọn xem → tìm/lọc sản phẩm → xem thông tin tồn.
- **Nhánh cập nhật:** chọn sản phẩm → nhập lượng tăng/giảm và lý do → kiểm tra → decision thông tin hợp lệ? `Không` nhập lại; `Có` xác nhận cập nhật.
- **Nhánh lịch sử:** chọn loại nhập/xuất/điều chỉnh → lọc thời gian, sản phẩm, nhà cung cấp → xem danh sách.
- **Kết thúc:** ba nhánh hội tụ tại “Hoàn thành”.
- **FR:** FR-INV-01, FR-INV-04, FR-INV-05, FR-INV-06.
- **Rule:** không âm kho; điều chỉnh có quyền/lý do; lưu lịch sử và người thực hiện.

### 6 Sơ đồ báo cáo doanh thu Hình 3.10

- **Actor:** phần quy trình và thiết kế xử lý xác định Quản lý/Admin; tiêu đề hình lại ghi “Nhân viên bán hàng”, là inconsistency mở.
- **Mục đích:** xem báo cáo doanh thu theo tiêu chí và có thể xuất báo cáo.
- **Bắt đầu:** đăng nhập và chọn Báo cáo doanh thu.
- **Activity/nhánh:** chọn loại báo cáo → một trong bốn nhánh theo thời gian, sản phẩm, khách hàng hoặc nhân viên → chọn khoảng thời gian và đối tượng/đơn vị hiển thị → xem báo cáo.
- **Decision:** thông tin đúng và đầy đủ? `Không` → điều chỉnh điều kiện và xem lại; `Có` → xuất báo cáo (in/tải file).
- **Kết thúc:** sau xuất báo cáo.
- **FR:** FR-RPT-01 đến FR-RPT-05 chỉ bao phủ một phần phạm vi của sơ đồ.
- **Rule:** khoảng thời gian hợp lệ; chỉ dữ liệu hóa đơn hợp lệ/hoàn thành; quyền xem báo cáo; số liệu nhất quán.

## Database Baseline

ERD, Database Diagram và phần Cấu trúc bảng đều thể hiện đúng 11 bảng sau; không bổ sung bảng ngoài danh sách này vào baseline triển khai.

Quy ước: `PK`, `FK`, `UQ`; `NN` = không cho NULL; `NULL` = cho NULL.

### users

Người dùng đăng nhập, role và trạng thái: `user_id BIGINT PK NN`; `username VARCHAR(100) UQ NN`; `password_hash VARCHAR(255) NN`; `full_name VARCHAR(150) NN`; `email VARCHAR(150) NULL`; `phone VARCHAR(20) NULL`; `role VARCHAR(30) NN`; `status VARCHAR(20) NN`; `created_at DATETIME NULL`; `updated_at DATETIME NULL`.

### categories

Danh mục sản phẩm: `category_id BIGINT PK NN`; `category_name VARCHAR(150) UQ NN`; `description VARCHAR(255) NULL`; `status VARCHAR(20) NN`.

### products

Sản phẩm và tồn hiện tại: `product_id BIGINT PK NN`; `category_id BIGINT FK NN`; `product_code VARCHAR(50) UQ NN`; `product_name VARCHAR(200) NN`; `sale_price DECIMAL(15,2) NN`; `stock_quantity INT NN`; `status VARCHAR(20) NN`; `description VARCHAR(500) NULL`; `created_at DATETIME NULL`; `updated_at DATETIME NULL`.

### suppliers

Nhà cung cấp: `supplier_id BIGINT PK NN`; `supplier_code VARCHAR(50) UQ NN`; `supplier_name VARCHAR(200) NN`; `phone VARCHAR(20) NULL`; `email VARCHAR(150) NULL`; `address VARCHAR(255) NULL`; `status VARCHAR(20) NN`.

### customers

Dữ liệu khách hàng, không phải tài khoản đăng nhập: `customer_id BIGINT PK NN`; `customer_name VARCHAR(150) NN`; `phone VARCHAR(20) NULL`; `email VARCHAR(150) NULL`; `address VARCHAR(255) NULL`; `created_at DATETIME NULL`.

### sales_orders

Đơn hàng bán: `order_id BIGINT PK NN`; `customer_id BIGINT FK` (bảng cấu trúc và ERD ghi NN, Database Diagram không hiện NN); `created_by BIGINT FK NN`; `order_code VARCHAR(50) UQ NN`; `order_date DATETIME NN`; `total_amount DECIMAL(15,2) NN`; `status VARCHAR(20) NN`; `note VARCHAR(500) NULL`; `created_at DATETIME NULL`.

### order_details

Chi tiết đơn hàng: `order_detail_id BIGINT PK NN`; `order_id BIGINT FK NN`; `product_id BIGINT FK NN`; `quantity INT NN`; `unit_price DECIMAL(15,2) NN`; `line_total DECIMAL(15,2) NN`.

### purchase_receipts

Phiếu nhập: `purchase_receipt_id BIGINT PK NN`; `supplier_id BIGINT FK NN`; `created_by BIGINT FK NN`; `receipt_code VARCHAR(50) UQ NN`; `receipt_date DATETIME NN`; `total_cost DECIMAL(15,2) NULL`; `status VARCHAR(20) NN`; `note VARCHAR(500) NULL`; `created_at DATETIME NULL`.

### purchase_receipt_details

Chi tiết phiếu nhập: `purchase_receipt_detail_id BIGINT PK NN`; `purchase_receipt_id BIGINT FK NN`; `product_id BIGINT FK NN`; `quantity INT NN`; `purchase_price DECIMAL(15,2) NN`; `line_total DECIMAL(15,2) NULL`.

### stock_transactions

Lịch sử biến động kho: `stock_transaction_id BIGINT PK NN`; `product_id BIGINT FK NN`; `created_by BIGINT FK NN`; `purchase_receipt_id BIGINT FK NULL`; `order_id BIGINT FK NULL`; `transaction_type VARCHAR(30) NN`; `quantity_change INT NN`; `quantity_before INT NN`; `quantity_after INT NN`; `reason VARCHAR(500) NULL`; `transaction_date DATETIME NN`.

### invoices

Hóa đơn, quan hệ một đơn hàng/một hóa đơn qua UNIQUE: `invoice_id BIGINT PK NN`; `order_id BIGINT FK UQ NN`; `invoice_code VARCHAR(50) UQ NN`; `invoice_date DATETIME NN`; `total_amount DECIMAL(15,2) NN`; `payment_method VARCHAR(50) NULL`; `status VARCHAR(20) NN`; `created_at DATETIME NULL`.

### Quan hệ chính

- `categories` 1–N `products`.
- `customers` 1–N `sales_orders`.
- `users` 1–N `sales_orders`, `purchase_receipts`, `stock_transactions` qua trường người tạo/thực hiện.
- `sales_orders` 1–N `order_details`; `products` 1–N `order_details`.
- `suppliers` 1–N `purchase_receipts`.
- `purchase_receipts` 1–N `purchase_receipt_details`; `products` 1–N `purchase_receipt_details`.
- `products` 1–N `stock_transactions`; giao dịch kho có thể tham chiếu phiếu nhập hoặc đơn hàng.
- `sales_orders` 1–1 `invoices` theo `invoices.order_id UNIQUE`.

### Điểm database chưa được phép tự quyết định

- ERD dùng `BIGINT` cho ID, Database Diagram dùng `INT`, cấu trúc bảng dùng `BIGINT`.
- Khách mua lẻ có thể không chọn khách hàng nhưng cấu trúc bảng/ERD ghi `sales_orders.customer_id` không NULL; Database Diagram lại không hiện NN.
- Phần Thiết kế xử lý dùng tên bảng/field khác và thêm các bảng không thuộc baseline 11 bảng như `PHIEN_DANG_NHAP`, `NHAT_KY_DANG_NHAP`, `THE_KHO`, `CHI_TIET_HOA_DON`, `Stock_Histories`; các mục đó không được tự tạo.

## Chức năng theo actor

### Quản lý Admin

Đăng nhập; tạo/sửa/xóa tài khoản; phân quyền; quản lý sản phẩm, danh mục, nhà cung cấp; tìm sản phẩm; xem tồn và lịch sử kho; tra cứu hóa đơn; tính doanh thu; thống kê hóa đơn và sản phẩm bán chạy; báo cáo doanh thu, sản phẩm bán chạy và tồn kho.

### Nhân viên bán hàng

Đăng nhập; tìm sản phẩm; kiểm tra tồn; tạo đơn; thêm/xóa sản phẩm; chọn hoặc tạo khách hàng; tính tổng tiền; xác nhận giao dịch; tạo và tra cứu hóa đơn.

### Nhân viên kho

Đăng nhập; xem sản phẩm và tồn; tạo phiếu nhập; thêm sản phẩm; xác nhận nhập; xuất kho; kiểm kê; điều chỉnh kho; xem lịch sử kho.

## Kết luận baseline

Baseline triển khai chỉ gồm bộ FR chính, ba role/actor và 11 bảng nêu trên. Mọi điểm khác biệt trong phần Use Case, Activity Diagram, Processing Design, ERD và Database Diagram được giữ ở trạng thái mở trong `KNOWN_INCONSISTENCIES.md`; không bên nào được coi là đã được chọn để code.
