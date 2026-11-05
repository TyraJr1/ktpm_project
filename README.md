# KTPM Project

Hệ thống quản lý bán hàng cho cửa hàng văn phòng phẩm. Backend dùng Java/Spring Boot, frontend dùng Vite và dữ liệu lưu trong MySQL.

## Chạy sau khi clone (Windows PowerShell)

### 1. Chuẩn bị

- JDK 25, Maven, Node.js và npm trong `PATH` (đã kiểm tra với Node.js 24/npm 11).
- MySQL đang chạy và một tài khoản MySQL có quyền tạo database, bảng và chạy migration. Trên máy phát triển đã kiểm tra với MySQL 8.4.
- Có kết nối Internet ở lần đầu để Maven và npm tải dependency.

Kiểm tra công cụ:

```powershell
java -version
mvn -version
node --version
npm --version
```

### 2. Tạo database rỗng

Trong MySQL Workbench (hoặc MySQL CLI), kết nối bằng tài khoản MySQL của **máy đang chạy**, rồi thực hiện:

```sql
CREATE DATABASE IF NOT EXISTS ktpm_sales_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Chỉ tạo database rỗng. Khi backend chạy, Flyway tự chạy V1 để tạo 11 bảng và V2 để thêm tài khoản demo. **Không chạy `database/schema.sql` trước Flyway trên database mới**, vì hai nơi đều tạo cùng các bảng. Nếu database đã có bảng/dữ liệu, xem [hướng dẫn database](database/README.md) trước khi chạy.

### 3. Chạy backend

Từ thư mục gốc của repository:

```powershell
cd backend
mvn test
cd ..
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-backend.ps1
```

Script hỏi mật khẩu MySQL mà không hiện ký tự khi nhập. Mặc định kết nối `root@localhost:3306/ktpm_sales_db`; nếu máy dùng tài khoản/host khác, đặt `DB_USERNAME` và `DB_URL` trong cửa sổ PowerShell trước khi chạy script:

```powershell
$env:DB_USERNAME = 'ten_user_mysql'
$env:DB_URL = 'jdbc:mysql://localhost:3306/ktpm_sales_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-backend.ps1
```

Backend chạy tại `http://localhost:8080`. Giữ cửa sổ này mở. Có thể đặt sẵn `DB_PASSWORD` và `JWT_SECRET` trong môi trường nếu không muốn nhập/tạo mới mỗi lần; xem [cấu hình backend](backend/README.md). **Không commit mật khẩu hoặc JWT secret.** Mỗi thành viên có thể dùng mật khẩu MySQL của máy mình; biết mật khẩu của một người không tự tạo được MySQL server/database trên máy người khác.

Nếu muốn kết nối **cùng MySQL server của bạn**, thành viên còn cần địa chỉ server trong `DB_URL`, `DB_USERNAME`, quyền kết nối từ máy của họ và quyền trên database; chỉ có mật khẩu là chưa đủ. Để code song song không đè dữ liệu/migration của nhau, nên dùng database phát triển riêng cho mỗi người.

### 4. Chạy frontend

Mở cửa sổ PowerShell thứ hai tại thư mục gốc:

```powershell
cd frontend
npm ci
npm run dev
```

Mở `http://localhost:5173`. Frontend mặc định gọi API tại `http://localhost:8080`. Nếu backend ở địa chỉ khác, đặt `VITE_API_BASE_URL` trước `npm run dev`, ví dụ `$env:VITE_API_BASE_URL = 'http://localhost:8080'`.

Tài khoản demo do Flyway V2 tạo: `admin01`, `sales01`, `warehouse01`; mật khẩu phát triển `Demo@123`. Chỉ phần Admin đã có các trang nghiệp vụ hiện tại; trang Sales/Warehouse vẫn là placeholder.

## Build khi phát triển

```powershell
cd backend
mvn test
mvn package
```

```powershell
cd frontend
npm ci
npm run build
```

Chạy backend và frontend như trên để kiểm tra UI với API/MySQL thật sau khi sửa code. Không đưa `node_modules`, `target`, `dist` hay mật khẩu lên Git.

## Cấu trúc

- `backend/`: REST Controller → Service → Repository → MySQL.
- `frontend/`: giao diện và API client.
- `database/`: SQL baseline và dữ liệu demo tùy chọn.
- `docs/`: báo cáo Word, traceability và change plan.
- `scripts/`: lệnh hỗ trợ môi trường phát triển.
