# IDP Contract Processing System

Hệ thống quản lý và xử lý hợp đồng, gồm giao diện React, REST API Spring Boot,
dịch vụ AI FastAPI, PostgreSQL và FAISS.

## Thành phần chính

- `frontend/`: giao diện React và TypeScript.
- `backend/idpapi/`: API nghiệp vụ Spring Boot.
- `ai-server/`: OCR, phát hiện chữ ký, trích xuất thông tin, phân tích rủi ro và RAG.
- `database/`: lược đồ và dữ liệu khởi tạo PostgreSQL.
- `configs/`, `nginx/`, `scripts/`: cấu hình và tập lệnh triển khai.
- `storage/`: thư mục dữ liệu cục bộ; nội dung hợp đồng và chữ ký không được đưa lên Git.

## Chạy bằng Docker

Yêu cầu: Git, Docker Desktop và Docker Compose.

```cmd
copy .env.example .env
docker compose up --build
```

Sau khi các container khởi động:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- AI Server: `http://localhost:8000`

Hãy thay `APP_SECURITY_JWT_SECRET` và các thông tin xác thực trong `.env` trước
khi triển khai ngoài môi trường phát triển.

## Dữ liệu không lưu trong Git

Môi trường ảo, thư viện đã cài, tệp hợp đồng, dữ liệu huấn luyện, trọng số mô
hình, kết quả chạy và nhật ký được loại khỏi kho mã nguồn. Các tài nguyên này
cần được cung cấp riêng cho từng môi trường triển khai.
