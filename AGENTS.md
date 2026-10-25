# PROJECT SOURCE OF TRUTH

1. Báo cáo Word là nguồn đặc tả chính.
2. Không tự thêm chức năng ngoài báo cáo.
3. Không tự đổi Business Rule.
4. Không tự đổi tên nghiệp vụ.
5. Không tự thêm bảng database.
6. Không tự thêm role.
7. Không tự thay kiến trúc.
8. Khi tài liệu mâu thuẫn phải báo cáo, không tự quyết định.
9. Code, Database, API, UI và Test phải truy vết được về Requirement.
10. Không tự phát minh requirement bị thiếu.

# ARCHITECTURE

Bắt buộc:

```text
Frontend
→ REST Controller
→ Service / Business Layer
→ Repository / Data Access Layer
→ MySQL
```

Controller:

- nhận request;
- nhận DTO;
- validation đầu vào cơ bản;
- gọi Service;
- trả response;
- không SQL;
- không business rule phức tạp;
- không gọi database trực tiếp.

Service:

- business rule;
- validation nghiệp vụ;
- transaction;
- tính toán;
- điều phối Repository.

Repository:

- truy vấn;
- lưu;
- cập nhật;
- giao tiếp database.

# CHANGE SAFETY

Khi sửa một chức năng:

1. Xác định phạm vi ảnh hưởng.
2. Xác định các file liên quan.
3. Xác định Entity/DTO/Service/Repository/API/UI/Test/Database liên quan.
4. Chỉ sửa phần cần thiết.
5. Nếu contract thay đổi phải cập nhật toàn bộ nơi phụ thuộc.
6. Không sửa module không liên quan.
7. Không refactor diện rộng.
8. Không đổi tên field/table/API đang chạy tốt.
9. Không xóa migration cũ.
10. Không làm hỏng chức năng đã pass test.

Nếu chức năng A đang chạy tốt và task chỉ liên quan B: KHÔNG sửa A.

Nếu bắt buộc sửa shared component: phải chạy regression test các chức năng phụ thuộc.

# CHANGE PLAN

Trước thay đổi đáng kể phải tạo:

`docs/change-plans/TASK-xxx.md`

bao gồm:

- Mục tiêu
- Requirement
- Business Rule
- Use Case
- Phạm vi ảnh hưởng
- File thay đổi
- Entity
- DTO
- Service
- Repository
- Database impact
- API impact
- UI impact
- Test
- Regression Test
- Những phần không được sửa
- Rủi ro
- Kết quả

# DATABASE SAFETY

Không tự:

- đổi table;
- đổi column;
- xóa table;
- xóa column;
- đổi datatype;
- đổi PK;
- đổi FK;
- thêm/xóa relationship;
- đổi UNIQUE;
- đổi NOT NULL;
- đổi DEFAULT;
- reset database;
- xóa migration cũ.

Nếu database phải thay đổi: phải thực hiện Impact Analysis trước.

# ERROR FIXING

Khi có lỗi:

1. xác định nguyên nhân;
2. xác định phạm vi;
3. xác định dependency;
4. kiểm tra database impact;
5. kiểm tra API impact;
6. kiểm tra UI impact;
7. sửa phạm vi nhỏ nhất;
8. chạy test lỗi;
9. chạy regression test;
10. không sửa phần không liên quan.

Nếu việc sửa lỗi làm thay đổi logic trong báo cáo: DỪNG và báo cho tôi.

# COMPLETION RULE

Không ghi DONE chỉ vì compile thành công.

Chỉ được DONE khi các phần phù hợp đã được xác minh:

- compile;
- backend;
- database;
- migration;
- API;
- validation;
- business rule;
- authorization;
- transaction;
- test;
- regression test;
- UI gọi API thật nếu có UI.

Nếu chưa xác minh: ghi `PARTIAL` hoặc `NOT VERIFIED`.

# TRACEABILITY

Bắt buộc duy trì:

```text
Requirement
→ Business Rule
→ Use Case
→ Database
→ API
→ Service
→ UI
→ Test
```

# SCOPE CONTROL

Không tự thêm:

- Microservices
- Redis
- Kafka
- Elasticsearch
- Docker
- Kubernetes
- AI
- Recommendation
- online payment
- shipping
- voucher
- promotion
- multi-branch
- customer e-commerce portal
