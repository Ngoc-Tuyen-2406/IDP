# IDP System

## Giới thiệu dự án

IDP System là hệ thống web quản lý và xử lý hợp đồng bằng trí tuệ nhân tạo.
Hệ thống giúp tập trung tài liệu, quản lý phiên bản, kiểm soát quyền truy cập
và theo dõi quy trình phê duyệt thay cho việc lưu trữ, tra cứu và rà soát thủ
công.

Pipeline AI hỗ trợ OCR tài liệu tiếng Việt, phát hiện chữ ký, trích xuất
metadata và điều khoản, tóm tắt, phân tích rủi ro và tạo embedding. RAG Chat
chỉ truy xuất nội dung của hợp đồng được chọn và trả về đoạn nguồn để người
dùng đối chiếu.

Dự án sử dụng React/TypeScript, Spring Boot, FastAPI, PostgreSQL và FAISS.
Kết quả AI có vai trò hỗ trợ, không thay thế quyết định chuyên môn hoặc pháp
lý.

## Chức năng chính

- Đăng nhập JWT và phân quyền RBAC cho Admin, Manager và Employee.
- Quản lý người dùng, đối tác, loại tài liệu và hợp đồng.
- Quản lý phiên bản, tải lên, tìm kiếm và tải xuống hợp đồng.
- OCR, phát hiện chữ ký, trích xuất thông tin, tóm tắt và phân tích rủi ro.
- Hỏi–đáp RAG theo từng hợp đồng, kèm nguồn tham chiếu.
- Phê duyệt nhiều bước, bình luận và thông báo.

## Hướng dẫn cài đặt và chạy

Yêu cầu:

- Python 3.11.
- Java 17 và Maven.
- Node.js và npm.
- Docker Desktop để chạy PostgreSQL.

Từ thư mục gốc, tạo cấu hình và khởi động cơ sở dữ liệu:

```powershell
Copy-Item .env.example .env
docker compose up -d postgres
```

Dữ liệu demo được nạp khi PostgreSQL tạo volume lần đầu.

### Terminal 1 — AI Server

Mở PowerShell tại thư mục gốc dự án:

```powershell
cd .\ai-server
& "$env:LOCALAPPDATA\Programs\Python\Python311\python.exe" --version
& "$env:LOCALAPPDATA\Programs\Python\Python311\python.exe" -m venv venv
.\venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
python -m uvicorn main:app --reload --host 127.0.0.1 --port 8000
```

Nếu `venv` cũ bị lỗi, chạy
`Remove-Item -Recurse -Force .\venv` rồi tạo lại.

- API docs: [http://localhost:8000/docs](http://localhost:8000/docs)
- Health: [http://localhost:8000/api/v1/health](http://localhost:8000/api/v1/health)

Trọng số phát hiện chữ ký phải được đặt tại:

```text
ai-server/trained_models/signature_yolo_v1/baseline/weights/best.pt
```

### Terminal 2 — Backend

Mở PowerShell khác tại thư mục gốc:

```powershell
cd .\backend\idpapi
mvn spring-boot:run
```

Backend chạy thành công khi xuất hiện `Tomcat started on port 8080`.

- Swagger: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Terminal 3 — Frontend

Mở PowerShell khác tại thư mục gốc:

```powershell
cd .\frontend
npm install
npm run dev
```

Mở [http://localhost:5173](http://localhost:5173). Nếu Vite chọn cổng khác,
dùng đúng địa chỉ `Local` được in trong terminal.

Giữ ba terminal hoạt động trong khi sử dụng:

| Dịch vụ | Địa chỉ |
| --- | --- |
| AI Server | [http://127.0.0.1:8000](http://127.0.0.1:8000) |
| Backend | [http://localhost:8080](http://localhost:8080) |
| Frontend | [http://localhost:5173](http://localhost:5173) |

## Cách sử dụng

Tài khoản demo:

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Admin | `admin@idp.local` | `Admin@123` |
| Manager | `nguyen.thu.ha@idp.local` | `Manager@123` |
| Employee | `le.bao.ngoc@idp.local` | `Employee@123` |

Quy trình cơ bản:

1. Đăng nhập và chọn **Upload contract** để tạo hợp đồng.
2. Mở trang chi tiết rồi chọn **Run AI Review**.
3. Chờ Job chuyển từ `QUEUED` sang `RUNNING` và `COMPLETED`.
4. Kiểm tra OCR, metadata, điều khoản, tóm tắt và rủi ro.
5. Dùng **RAG Chat** hoặc gửi hợp đồng vào quy trình phê duyệt.

## Dữ liệu sử dụng

Dữ liệu thô không được đưa lên GitHub để tránh lộ hợp đồng, dữ liệu cá nhân
và vi phạm điều kiện phân phối. Repository chỉ lưu registry, hướng dẫn gán
nhãn và cấu hình dataset.

| Dataset | Mục đích | Nguồn |
| --- | --- | --- |
| ContractNLI | Suy luận hợp đồng, RAG và đánh giá rủi ro | [Stanford ContractNLI](https://stanfordnlp.github.io/contract-nli/) |
| Roboflow-100 Signatures | Huấn luyện phát hiện chữ ký | [Roboflow Signatures](https://universe.roboflow.com/roboflow-100/signatures-xc8up) |
| Signature Detection – Amruth | Bổ sung ảnh và nhãn chữ ký | [Roboflow Signature Detection](https://universe.roboflow.com/amruth/signature-detection-qky9p) |
| DocLayNet | Nhận dạng bố cục tài liệu | [DS4SD/DocLayNet](https://github.com/DS4SD/DocLayNet) |
| NDL-DocL | Thử nghiệm bảng, con dấu và bố cục | [NDL Layout Dataset](https://github.com/ndl-lab/layout-dataset) |
| PubTables-1M | Nhận dạng cấu trúc bảng | [Microsoft Table Transformer](https://github.com/microsoft/table-transformer) |

Chi tiết:
[`DATA_REGISTER.csv`](datasets/registry/DATA_REGISTER.csv) và
[`DATASET_READINESS.md`](datasets/registry/DATASET_READINESS.md).




