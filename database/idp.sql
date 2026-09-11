-- ===========================================
-- DATABASE: Intelligent Document Processing
-- PostgreSQL (Optimized Version)
-- ===========================================

-- 1. ROLES
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- 2. DEPARTMENTS
CREATE TABLE departments (
    department_id SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 3. USERS
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    role_id INT NOT NULL,
    department_id INT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL, -- Bỏ UNIQUE ở đây để dùng Partial Index tối ưu xóa mềm
    password_hash TEXT NOT NULL,
    phone VARCHAR(20),
    avatar TEXT,
    status BOOLEAN DEFAULT TRUE,
    failed_login_count INT DEFAULT 0,
    last_login TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    password_changed_at TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE,
    locked_until TIMESTAMP,

    FOREIGN KEY(role_id) REFERENCES roles(role_id),
    FOREIGN KEY(department_id) REFERENCES departments(department_id)
);

-- Tạo Partial Unique Index để giải quyết bài toán trùng email khi xóa mềm (Soft Delete)
CREATE UNIQUE INDEX idx_users_email_unique_active 
ON users(email) 
WHERE is_deleted = FALSE;

-- 4. DOCUMENT TYPES
CREATE TABLE document_types (
    document_type_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 5. PARTNERS
CREATE TABLE partners (
    partner_id SERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    partner_type VARCHAR(50),
    tax_code VARCHAR(50) UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. CONTRACTS (Tạo trước nhưng để current_version_id là NULLable)
CREATE TABLE contracts (
    contract_id SERIAL PRIMARY KEY,
    document_type_id INT NOT NULL,
    partner_id INT NOT NULL,
    uploaded_by INT NOT NULL,
    contract_number VARCHAR(100) UNIQUE NOT NULL,
    contract_name VARCHAR(255),
    partner_representative_name VARCHAR(255),
    partner_representative_position VARCHAR(255),
    signed_date DATE,
    effective_date DATE,
    expired_date DATE,
    total_value NUMERIC(18,2),
    currency VARCHAR(10),
    status VARCHAR(30) DEFAULT 'Processing' CHECK(status IN ('Processing','Pending','Approved','Rejected','Expired')),
    current_version_id INT, -- Sẽ tạo FK sau khi bảng contract_versions được khởi tạo
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(document_type_id) REFERENCES document_types(document_type_id),
    FOREIGN KEY(partner_id) REFERENCES partners(partner_id),
    FOREIGN KEY(uploaded_by) REFERENCES users(user_id)
);

-- 7. CONTRACT VERSIONS
CREATE TABLE contract_versions (
    version_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    version_number INT NOT NULL,
    edited_by INT,
    change_note TEXT,
    status VARCHAR(20) CHECK(status IN ('Draft','Published')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(contract_id, version_number),
    FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    FOREIGN KEY(edited_by) REFERENCES users(user_id)
);

-- Bây giờ tiến hành tạo khóa ngoại chéo an toàn từ contracts sang contract_versions
ALTER TABLE contracts
ADD CONSTRAINT fk_current_version
FOREIGN KEY(current_version_id)
REFERENCES contract_versions(version_id)
ON DELETE SET NULL; -- Đổi sang SET NULL để tránh lỗi xung đột dây chuyền khi xóa bản ghi

-- 8. CONTRACT FILES
CREATE TABLE contract_files (
    file_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    file_name VARCHAR(255),
    file_path TEXT NOT NULL,
    file_type VARCHAR(20),
    file_size BIGINT,
    page_count INT,
    file_hash TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE
);

-- 26. AI MODELS (Được đẩy lên trước để các bảng AI khác tham chiếu trực tiếp bằng ID)
CREATE TABLE ai_models (
    model_id SERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL UNIQUE,
    model_type VARCHAR(50),
    version VARCHAR(50),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. OCR RESULTS
CREATE TABLE ocr_results (
    ocr_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    page_number INT,
    language VARCHAR(20),
    engine VARCHAR(50),
    ocr_text TEXT,
    confidence FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE
);

-- 10. DETECTION REGIONS
CREATE TABLE detection_regions (
    region_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    page_number INT,
    label VARCHAR(50),
    x_min FLOAT,
    y_min FLOAT,
    x_max FLOAT,
    y_max FLOAT,
    confidence FLOAT,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE
);

-- 11. AI METADATA (Tối ưu hóa kiểu TEXT thành JSONB cho các trường lưu trữ giá trị linh hoạt)
CREATE TABLE ai_metadata (
    metadata_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    field_name VARCHAR(100),
    field_type VARCHAR(50),
    original_value JSONB, -- Chuyển sang JSONB để lưu trữ linh hoạt cấu trúc kết quả AI
    current_value JSONB,  -- Chuyển sang JSONB
    confidence FLOAT,
    verified BOOLEAN DEFAULT FALSE,
    verified_by INT,
    verified_at TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY(verified_by) REFERENCES users(user_id)
);

-- 12. METADATA HISTORY
CREATE TABLE metadata_history (
    history_id SERIAL PRIMARY KEY,
    metadata_id INT NOT NULL,
    old_value JSONB,
    new_value JSONB,
    edited_by INT,
    edited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(metadata_id) REFERENCES ai_metadata(metadata_id) ON DELETE CASCADE,
    FOREIGN KEY(edited_by) REFERENCES users(user_id)
);

-- 13. AI SUMMARY (Sử dụng khóa ngoại liên kết tới bảng ai_models thay vì lưu Text tự do)
CREATE TABLE ai_summary (
    summary_id SERIAL PRIMARY KEY,
    version_id INT UNIQUE,
    summary TEXT,
    model_id INT, -- Tối ưu hóa khóa ngoại
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY(model_id) REFERENCES ai_models(model_id)
);

-- 14. EMBEDDING INFO
CREATE TABLE embedding_info (
    embedding_id SERIAL PRIMARY KEY,
    version_id INT UNIQUE,
    vector_index VARCHAR(100),
    model_id INT, -- Tối ưu hóa khóa ngoại trỏ sang bảng ai_models
    chunk_count INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY(model_id) REFERENCES ai_models(model_id)
);

-- 15. AI CHAT HISTORY
CREATE TABLE ai_chat_history (
    chat_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    user_id INT NOT NULL,
    question TEXT,
    answer TEXT,
    response_time_ms INT, -- Tối ưu hóa thành mili-giây dạng INT dễ đo lường tính toán
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES users(user_id)
);

-- 16. COMMENTS
CREATE TABLE comments (
    comment_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    metadata_id INT,
    parent_comment_id INT,
    user_id INT NOT NULL,
    content TEXT,
    page_number INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    FOREIGN KEY(metadata_id) REFERENCES ai_metadata(metadata_id) ON DELETE SET NULL,
    FOREIGN KEY(parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES users(user_id)
);

-- 17. APPROVAL HISTORY
CREATE TABLE approval_history (
    approval_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    approved_by INT NOT NULL,
    status VARCHAR(20) CHECK(status IN ('Pending','Approved','Rejected','Returned')),
    note TEXT,
    approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    FOREIGN KEY(approved_by) REFERENCES users(user_id)
);

-- 18. NOTIFICATIONS
CREATE TABLE notifications (
    notification_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255),
    content TEXT,
    type VARCHAR(50),
    link TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 19. PROCESSING LOGS
CREATE TABLE processing_logs (
    process_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    model_id INT,
    step VARCHAR(100),
    status VARCHAR(20) CHECK(status IN ('Success','Fail')),
    duration FLOAT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY(model_id) REFERENCES ai_models(model_id)
);

-- 20. PERMISSIONS 
CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- 21. ROLE PERMISSIONS
CREATE TABLE role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY(role_id, permission_id),
    FOREIGN KEY(role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY(permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- 22. REFRESH TOKENS
CREATE TABLE refresh_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    token TEXT NOT NULL,
    expires_at TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    device_name VARCHAR(100),
    ip_address VARCHAR(50),
    created_by_ip VARCHAR(50),
    
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 23. AUDIT LOGS
CREATE TABLE audit_logs (
    audit_id SERIAL PRIMARY KEY,
    user_id INT,
    action VARCHAR(100),
    table_name VARCHAR(100),
    record_id INT,
    description TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE SET NULL -- Tránh mất lịch sử khi user bị xóa hẳn khỏi DB
);

-- 24. TAGS
CREATE TABLE tags (
    tag_id SERIAL PRIMARY KEY,
    tag_name VARCHAR(100) UNIQUE
);

-- 25. CONTRACT TAGS
CREATE TABLE contract_tags (
    contract_id INT,
    tag_id INT,
    PRIMARY KEY(contract_id, tag_id),
    FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    FOREIGN KEY(tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

-- 27. PROCESSING QUEUE
CREATE TABLE processing_queue (
    queue_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    task_type VARCHAR(50),
    queue_name VARCHAR(100),
    worker_name VARCHAR(100),
    status VARCHAR(30),
    priority INT DEFAULT 1,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    
    FOREIGN KEY(version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE
);

-- 28. AI PROMPTS
CREATE TABLE ai_prompts (
    prompt_id SERIAL PRIMARY KEY,
    prompt_name VARCHAR(100),
    prompt_content TEXT,
    model_id INT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(model_id) REFERENCES ai_models(model_id),
    FOREIGN KEY(created_by) REFERENCES users(user_id)
);

-- 29. CONTRACT SHARES
CREATE TABLE contract_shares (
    share_id SERIAL PRIMARY KEY,
    contract_id INT,
    shared_by INT,
    shared_to INT,
    permission VARCHAR(30),
    shared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    FOREIGN KEY(shared_by) REFERENCES users(user_id),
    FOREIGN KEY(shared_to) REFERENCES users(user_id)
);

-- 30. SYSTEM SETTINGS (Sử dụng JSONB để chứa dữ liệu config có cấu trúc động)
CREATE TABLE system_settings (
    setting_id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE,
    setting_value JSONB, -- Đổi sang JSONB tối ưu lưu config
    description TEXT
);

-- =====================================
-- INDEXES
-- =====================================
CREATE INDEX idx_contract_number ON contracts(contract_number);
CREATE INDEX idx_contract_partner ON contracts(partner_id);
CREATE INDEX idx_contract_upload ON contracts(uploaded_by);
CREATE INDEX idx_version_contract ON contract_versions(contract_id);
CREATE INDEX idx_metadata_version ON ai_metadata(version_id);
CREATE INDEX idx_chat_version ON ai_chat_history(version_id);
CREATE INDEX idx_comment_contract ON comments(contract_id);
CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_processing_version ON processing_logs(version_id);
CREATE INDEX idx_ocr_version ON ocr_results(version_id);
CREATE INDEX idx_detection_version ON detection_regions(version_id);


-- =====================================
-- TEST
-- =====================================
SELECT tablename
FROM pg_tables
WHERE schemaname = 'public';


-- =====================================
-- Khởi tạo dữ liệu ban đầu
-- =====================================

-- =====================================
-- Roles
-- =====================================
INSERT INTO roles(role_name, description)
VALUES
('Admin','Quản trị hệ thống'),
('Manager','Quản lý'),
('Employee','Nhân viên');
SELECT * FROM roles;


-- =====================================
-- Departments
-- =====================================
INSERT INTO departments(department_name)
VALUES
('IT'),
('HR'),
('Finance'),
('Legal');
SELECT * FROM departments;


-- =====================================
-- Document Types
-- =====================================
INSERT INTO document_types(name)
VALUES
('Hợp đồng mua bán'),
('Hợp đồng lao động'),
('Hợp đồng dịch vụ'),
('Hợp đồng thuê');
SELECT * FROM document_types;


-- =====================================
-- AI Models
-- =====================================
INSERT INTO ai_models(model_name, model_type, version)
VALUES
('PaddleOCR','OCR','1.0'),
('YOLOv11','Detection','11'),
('Qwen3','LLM','3'),
('bge-m3','Embedding','1.0');
SELECT * FROM ai_models;


ALTER TABLE users
DROP COLUMN status;

ALTER TABLE users
ADD COLUMN status VARCHAR(20)
NOT NULL
DEFAULT 'PENDING'
CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'LOCKED'));

SELECT current_database();

-- ===========================================
-- DATABASE: Intelligent Document Processing
-- PHẦN 2: DATABASE EXTENSION
-- (Chạy sau 01_database_schema.sql)
-- PostgreSQL
-- ===========================================


-- ===========================================
-- A. AUTHENTICATION
-- Bổ sung chức năng quên mật khẩu và xác thực email
-- ===========================================

-- A1. PASSWORD RESET TOKENS
CREATE TABLE password_reset_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user
ON password_reset_tokens(user_id);


-- A2. EMAIL VERIFICATION TOKENS
CREATE TABLE email_verification_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_email_verify_user
ON email_verification_tokens(user_id);


-- ===========================================
-- B. CONTRACT FAVORITES
-- Đánh dấu hợp đồng yêu thích
-- ===========================================

CREATE TABLE favorite_contracts (
    user_id INT NOT NULL,
    contract_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(user_id, contract_id),

    FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE CASCADE
);


-- ===========================================
-- C. CONTRACT APPENDICES
-- Quản lý phụ lục hợp đồng
-- ===========================================

CREATE TABLE contract_appendices (
    appendix_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    appendix_number VARCHAR(50),
    title VARCHAR(255),
    description TEXT,
    file_path TEXT,
    signed_date DATE,
    effective_date DATE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE CASCADE,

    FOREIGN KEY(created_by)
        REFERENCES users(user_id)
);

CREATE INDEX idx_appendix_contract
ON contract_appendices(contract_id);


-- ===========================================
-- D. CONTRACT LIFECYCLE
-- Gia hạn / Thanh lý hợp đồng
-- ===========================================

CREATE TABLE contract_lifecycle (
    lifecycle_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,

    action_type VARCHAR(20)
        CHECK(action_type IN ('Renew','Terminate')),

    old_expired_date DATE,
    new_expired_date DATE,

    reason TEXT,

    performed_by INT,

    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE CASCADE,

    FOREIGN KEY(performed_by)
        REFERENCES users(user_id)
);

ALTER TABLE contracts
DROP CONSTRAINT contracts_status_check;

ALTER TABLE contracts
ADD CONSTRAINT contracts_status_check
CHECK(status IN
(
'Processing',
'Pending',
'Approved',
'Rejected',
'Expired',
'Terminated'
));


-- ===========================================
-- E. APPROVAL WORKFLOW
-- Quy trình duyệt nhiều bước
-- ===========================================

CREATE TABLE approval_workflow (

    workflow_id SERIAL PRIMARY KEY,

    contract_id INT NOT NULL,

    step_number INT NOT NULL,

    approver_id INT NOT NULL,

    status VARCHAR(20)
        CHECK(status IN
        ('Pending','Approved','Rejected','Skipped')),

    comment TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    approved_at TIMESTAMP,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE CASCADE,

    FOREIGN KEY(approver_id)
        REFERENCES users(user_id)
);

CREATE INDEX idx_workflow_contract
ON approval_workflow(contract_id);


-- ===========================================
-- F. PAYMENT MANAGEMENT
-- Theo dõi thanh toán hợp đồng
-- ===========================================

CREATE TABLE contract_payments (

    payment_id SERIAL PRIMARY KEY,

    contract_id INT NOT NULL,

    installment_no INT,

    description VARCHAR(255),

    amount NUMERIC(18,2) NOT NULL,

    paid_amount NUMERIC(18,2),

    due_date DATE,

    paid_date DATE,

    status VARCHAR(20)
        DEFAULT 'Pending'
        CHECK(status IN
        ('Pending','Paid','Overdue','Cancelled')),

    confirmed_by INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE CASCADE,

    FOREIGN KEY(confirmed_by)
        REFERENCES users(user_id)
);

CREATE INDEX idx_payment_contract
ON contract_payments(contract_id);


-- ===========================================
-- G. AI RISK ANALYSIS
-- Đánh giá rủi ro hợp đồng
-- ===========================================

CREATE TABLE ai_risk_analysis (

    risk_id SERIAL PRIMARY KEY,

    version_id INT NOT NULL,

    model_id INT,

    risk_level VARCHAR(20)
        CHECK(risk_level IN
        ('Low','Medium','High','Critical')),

    risk_score FLOAT,

    risk_summary TEXT,

    recommendation TEXT,

    risk_details JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(version_id)
        REFERENCES contract_versions(version_id)
        ON DELETE CASCADE,

    FOREIGN KEY(model_id)
        REFERENCES ai_models(model_id)
);

CREATE INDEX idx_risk_version
ON ai_risk_analysis(version_id);


-- ===========================================
-- H. DIGITAL SIGNATURE
-- Quản lý chữ ký điện tử
-- ===========================================

CREATE TABLE signatures (

    signature_id SERIAL PRIMARY KEY,

    user_id INT NOT NULL,

    signature_type VARCHAR(20)
        CHECK(signature_type IN
        ('Signature','Stamp')),

    file_path TEXT NOT NULL,

    file_hash TEXT,

    is_default BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);


CREATE TABLE contract_signatures (

    contract_signature_id SERIAL PRIMARY KEY,

    contract_id INT NOT NULL,

    signature_id INT,

    signer_id INT NOT NULL,

    sign_order INT DEFAULT 1,

    status VARCHAR(20)
        DEFAULT 'Pending'
        CHECK(status IN
        ('Pending','Signed','Rejected')),

    signed_page INT,

    position_x FLOAT,

    position_y FLOAT,

    ip_address VARCHAR(50),

    device_info TEXT,

    signed_at TIMESTAMP,

    reject_reason TEXT,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE CASCADE,

    FOREIGN KEY(signature_id)
        REFERENCES signatures(signature_id),

    FOREIGN KEY(signer_id)
        REFERENCES users(user_id)
);

CREATE INDEX idx_signature_contract
ON contract_signatures(contract_id);


-- ===========================================
-- I. EMAIL LOGS
-- Lưu lịch sử gửi email
-- ===========================================

CREATE TABLE email_logs (

    email_id SERIAL PRIMARY KEY,

    contract_id INT,

    sender_id INT,

    recipient_email VARCHAR(255) NOT NULL,

    subject VARCHAR(255),

    email_type VARCHAR(50),

    provider_message_id VARCHAR(255),

    status VARCHAR(20)
        DEFAULT 'Sent'
        CHECK(status IN
        ('Pending','Sent','Failed')),

    error_message TEXT,

    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE SET NULL,

    FOREIGN KEY(sender_id)
        REFERENCES users(user_id)
);


-- ===========================================
-- J. NOTIFICATION SETTINGS
-- Cấu hình nhận thông báo
-- ===========================================

CREATE TABLE notification_settings (

    user_id INT PRIMARY KEY,

    email_enabled BOOLEAN DEFAULT TRUE,

    system_notification BOOLEAN DEFAULT TRUE,

    contract_new BOOLEAN DEFAULT TRUE,

    contract_approval BOOLEAN DEFAULT TRUE,

    contract_expiring BOOLEAN DEFAULT TRUE,

    comment_mention BOOLEAN DEFAULT TRUE,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);


-- ===========================================
-- K. REMINDER LOGS
-- Lưu lịch sử gửi nhắc hợp đồng
-- ===========================================

CREATE TABLE reminder_logs (

    reminder_id SERIAL PRIMARY KEY,

    contract_id INT NOT NULL,

    reminder_type VARCHAR(50),

    days_before INT,

    status VARCHAR(20)
        CHECK(status IN
        ('Pending','Sent','Failed')),

    sent_to INT,

    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(contract_id)
        REFERENCES contracts(contract_id)
        ON DELETE CASCADE,

    FOREIGN KEY(sent_to)
        REFERENCES users(user_id)
);


-- ===========================================
-- L. COMMENT MENTIONS
-- Người được mention trong bình luận
-- ===========================================

CREATE TABLE comment_mentions (

    comment_id INT NOT NULL,

    mentioned_user_id INT NOT NULL,

    PRIMARY KEY(comment_id, mentioned_user_id),

    FOREIGN KEY(comment_id)
        REFERENCES comments(comment_id)
        ON DELETE CASCADE,

    FOREIGN KEY(mentioned_user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);


-- ===========================================
-- M. EMBEDDING CHUNKS
-- Lưu từng đoạn văn đã embedding
-- ===========================================

CREATE TABLE embedding_chunks (

    chunk_id SERIAL PRIMARY KEY,

    embedding_id INT NOT NULL,

    chunk_index INT,

    chunk_text TEXT,

    vector_id VARCHAR(100),

    page_number INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(embedding_id)
        REFERENCES embedding_info(embedding_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_embedding_chunk
ON embedding_chunks(embedding_id);


-- ===========================================
-- N. CONTRACT COMPARISONS
-- Lưu kết quả so sánh hai phiên bản hợp đồng
-- ===========================================

CREATE TABLE contract_comparisons (

    comparison_id SERIAL PRIMARY KEY,

    source_version_id INT NOT NULL,

    target_version_id INT NOT NULL,

    comparison_result TEXT,

    compared_by INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(source_version_id)
        REFERENCES contract_versions(version_id),

    FOREIGN KEY(target_version_id)
        REFERENCES contract_versions(version_id),

    FOREIGN KEY(compared_by)
        REFERENCES users(user_id)
);


-- ===========================================
-- O. AI CHAT HISTORY EXTENSION
-- Lưu các chunk đã sử dụng để trả lời
-- ===========================================

ALTER TABLE ai_chat_history
ADD COLUMN source_chunk_ids INT[];

-- ===========================================
-- DATABASE: Intelligent Document Processing
-- PHẦN 3: FIX MIGRATION (gộp)
-- (Chạy sau 01_database_schema.sql và 02_database_extension.sql)
-- PostgreSQL
-- ===========================================


-- ===========================================
-- A. FIX: USER - ROLE (1-N  ->  N-N)
-- Lý do: API có /api/users/{id}/roles (GET/POST/DELETE)
-- cho phép gán/thu hồi NHIỀU role cho 1 user,
-- nhưng bảng users hiện tại chỉ cho phép 1 role_id duy nhất.
-- ===========================================

-- A1. Tạo bảng trung gian user_roles
CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    assigned_by INT,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(user_id, role_id),

    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY(assigned_by) REFERENCES users(user_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- A2. Migrate dữ liệu role_id hiện có trong users sang bảng mới
INSERT INTO user_roles(user_id, role_id)
SELECT user_id, role_id FROM users
ON CONFLICT DO NOTHING;

-- A3. Bỏ cột role_id cũ trong bảng users
ALTER TABLE users
DROP CONSTRAINT users_role_id_fkey;

ALTER TABLE users
DROP COLUMN role_id;

-- Lưu ý: Nếu ứng dụng cần khái niệm "role mặc định / role chính"
-- để hiển thị nhanh mà không JOIN, có thể thêm cột thay thế:
-- ALTER TABLE users ADD COLUMN primary_role_id INT REFERENCES roles(role_id);
-- và đồng bộ nó bằng trigger hoặc tầng ứng dụng khi user_roles thay đổi.


-- ===========================================
-- B. BỔ SUNG: API KEYS
-- Lý do: API có GET/POST/DELETE /api/admin/api-keys
-- nhưng chưa có bảng lưu trữ tương ứng.
-- ===========================================

CREATE TABLE api_keys (
    key_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    key_name VARCHAR(100),
    key_prefix VARCHAR(20),      -- phần hiển thị công khai, ví dụ "sk_live_ab12"
    key_hash TEXT NOT NULL,      -- lưu hash, không lưu key gốc
    scopes TEXT[],               -- danh sách quyền hạn của key, ví dụ '{read:contracts, write:contracts}'
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY(created_by) REFERENCES users(user_id)
);

CREATE UNIQUE INDEX idx_api_keys_hash ON api_keys(key_hash);
CREATE INDEX idx_api_keys_user ON api_keys(user_id);


-- ===========================================
-- C. BỎ APPROVAL_HISTORY
-- Dùng approval_workflow làm nguồn duy nhất cho cả
-- trạng thái duyệt hiện tại (status theo từng step)
-- lẫn lịch sử duyệt (mỗi step tự nó là 1 record lịch sử,
-- insert step mới thay vì update đè lên record cũ).
-- ===========================================

DROP TABLE IF EXISTS approval_history;


-- ===========================================
-- D. THÊM 'Draft' VÀO contracts.status
-- Lý do: hợp đồng cần trạng thái nháp trước khi vào luồng xử lý AI/duyệt.
-- ===========================================

ALTER TABLE contracts
DROP CONSTRAINT contracts_status_check;

ALTER TABLE contracts
ADD CONSTRAINT contracts_status_check
CHECK(status IN
(
'Draft',
'Processing',
'Pending',
'Approved',
'Rejected',
'Expired',
'Terminated'
));


-- ===========================================
-- E. THÊM progress VÀO processing_queue
-- Lý do: hỗ trợ API theo dõi tiến trình xử lý AI
-- (GET /api/ai/jobs/{jobId}, GET /api/processing/jobs/{id})
-- ===========================================

ALTER TABLE processing_queue
ADD COLUMN progress SMALLINT DEFAULT 0
CHECK(progress BETWEEN 0 AND 100);


-- ===========================================
-- F. THÊM conversation_id VÀO ai_chat_history
-- Lý do: nhóm các câu hỏi/trả lời thành 1 phiên hội thoại,
-- hỗ trợ GET /api/chat/history theo từng conversation
-- thay vì chỉ liệt kê phẳng theo version_id.
-- ===========================================

-- Cần bật extension pgcrypto để dùng gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE ai_chat_history
ADD COLUMN conversation_id UUID DEFAULT gen_random_uuid();

CREATE INDEX idx_chat_conversation
ON ai_chat_history(conversation_id);


-- ===========================================
-- G. THÊM request_id VÀO processing_logs
-- Lý do: truy vết một request/job cụ thể xuyên suốt nhiều bước xử lý
-- (preprocess -> ocr -> extract -> analyze), khớp với response
-- của POST /api/ai/jobs trả về jobId để client theo dõi.
-- ===========================================

ALTER TABLE processing_logs
ADD COLUMN request_id UUID;

CREATE INDEX idx_processing_logs_request
ON processing_logs(request_id);


-- ===========================================
-- H. GHI CHÚ: /api/auth/sessions
-- Không cần bảng mới. "Session" ánh xạ trực tiếp vào refresh_tokens
-- (đã có device_name, ip_address, revoked, expires_at).
-- sessionId trong API = token_id trong DB.
-- ===========================================


-- ===========================================
-- I. USERS.STATUS: THÊM 'Pending'
-- Lý do: user mới tạo nhưng chưa xác thực email (email_verified = FALSE)
-- cần trạng thái riêng thay vì mặc định 'Active', để chặn đăng nhập
-- cho tới khi xác thực xong.
-- ===========================================

ALTER TABLE users
DROP CONSTRAINT users_status_check;

ALTER TABLE users
ADD CONSTRAINT users_status_check
CHECK(status IN ('PENDING','ACTIVE','INACTIVE','LOCKED'));

-- Gợi ý tầng ứng dụng: khi tạo user mới -> status = 'Pending';
-- khi verify-email thành công -> status = 'Active'.


-- ===========================================
-- J. CONTRACT_PARTIES: HỖ TRỢ HỢP ĐỒNG NHIỀU BÊN
-- Lý do: contracts hiện chỉ có 1 partner_id + 1 đại diện,
-- nhưng thực tế hợp đồng có thể có nhiều bên (Bên A, Bên B,
-- bên bảo lãnh, bên thứ ba làm chứng...).
-- Các cột partner_id / partner_representative_* trên contracts
-- được GIỮ LẠI để tương thích ngược (xem là "bên đối tác chính"),
-- bảng mới dùng cho các trường hợp nhiều bên.
-- ===========================================

CREATE TABLE contract_parties (
    party_id SERIAL PRIMARY KEY,
    contract_id INT NOT NULL,
    partner_id INT,
    party_role VARCHAR(50),              -- 'PartyA','PartyB','Guarantor','Witness',...
    representative_name VARCHAR(255),
    representative_position VARCHAR(255),
    signed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE,
    FOREIGN KEY(partner_id) REFERENCES partners(partner_id)
);

CREATE INDEX idx_contract_parties_contract ON contract_parties(contract_id);
CREATE INDEX idx_contract_parties_partner ON contract_parties(partner_id);


-- ===========================================
-- K. AI_SUMMARY: THÊM summary_json
-- Lý do: bên cạnh bản tóm tắt dạng văn bản tự do (summary TEXT),
-- cần thêm bản tóm tắt có cấu trúc (parties, key_terms, obligations,
-- risks...) để hiển thị UI dạng bảng/card thay vì chỉ đoạn văn.
-- ===========================================

ALTER TABLE ai_summary
ADD COLUMN summary_json JSONB;


-- ===========================================
-- L. AI_METADATA: THÊM normalized_value
-- Lý do: current_value lưu giá trị do AI trích xuất/người dùng sửa
-- ở dạng thô (chuỗi ngày tháng, số tiền... có thể chưa đồng nhất định dạng).
-- normalized_value lưu giá trị đã chuẩn hóa (ISO date, số thực chuẩn...)
-- để tầng ứng dụng/so sánh/tính toán không phải tự parse lại.
-- ===========================================

ALTER TABLE ai_metadata
ADD COLUMN normalized_value JSONB;


-- ===========================================
-- M. NOTIFICATIONS: THÊM read_at
-- Lý do: is_read chỉ biết đã đọc hay chưa, không biết đọc lúc nào
-- (cần cho thống kê/SLA phản hồi thông báo).
-- ===========================================

ALTER TABLE notifications
ADD COLUMN read_at TIMESTAMP;

-- Gợi ý tầng ứng dụng: khi PUT /api/notifications/{id}/read ->
-- set is_read = TRUE, read_at = CURRENT_TIMESTAMP.


-- ===========================================
-- N. PROCESSING_QUEUE: THÊM error_message
-- Lý do: processing_logs có error_message chi tiết từng bước xử lý,
-- nhưng processing_queue (đại diện cho cả job) cũng cần lưu nhanh
-- lỗi gần nhất để hiển thị trạng thái job mà không cần JOIN sang logs.
-- ===========================================

ALTER TABLE processing_queue
ADD COLUMN error_message TEXT;


-- ===========================================
-- O. CONTRACT_SHARES: THÊM expires_at
-- Lý do: chia sẻ hợp đồng cho người khác (kể cả ngoài hệ thống)
-- nên có thời hạn, tránh quyền truy cập tồn tại vĩnh viễn.
-- ===========================================

ALTER TABLE contract_shares
ADD COLUMN expires_at TIMESTAMP;


-- ===========================================
-- P. CONTRACT_VERSIONS: THÊM is_current
-- Lý do: contracts.current_version_id đã xác định version hiện hành,
-- nhưng khi truy vấn/lọc trực tiếp trên contract_versions (ví dụ
-- "lấy tất cả version đang là bản hiện hành của mọi hợp đồng")
-- sẽ phải JOIN ngược lại contracts. Thêm is_current giúp truy vấn
-- nhanh hơn và cache lại thông tin này ngay trên chính bảng version.
-- ===========================================

ALTER TABLE contract_versions
ADD COLUMN is_current BOOLEAN DEFAULT FALSE;

-- Đồng bộ dữ liệu is_current từ contracts.current_version_id hiện có
UPDATE contract_versions cv
SET is_current = TRUE
FROM contracts c
WHERE c.current_version_id = cv.version_id;

-- Đảm bảo mỗi hợp đồng chỉ có tối đa 1 version được đánh dấu is_current = TRUE
CREATE UNIQUE INDEX idx_one_current_version_per_contract
ON contract_versions(contract_id)
WHERE is_current = TRUE;

-- AI clause extraction results from the OCR pipeline.
CREATE TABLE IF NOT EXISTS ai_clauses (
    clause_id SERIAL PRIMARY KEY,
    version_id INT NOT NULL,
    clause_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    clause_text TEXT NOT NULL,
    matched_keywords JSONB,
    page_number INT,
    confidence FLOAT,
    model_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_clauses_version
        FOREIGN KEY (version_id) REFERENCES contract_versions(version_id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_clauses_model
        FOREIGN KEY (model_id) REFERENCES ai_models(model_id)
);

CREATE INDEX IF NOT EXISTS idx_ai_clauses_version
ON ai_clauses(version_id);

-- Lưu ý tầng ứng dụng: mỗi khi cập nhật contracts.current_version_id,
-- cần đồng thời set is_current = FALSE cho version cũ và
-- is_current = TRUE cho version mới (nên bọc trong 1 transaction,
-- hoặc dùng trigger AFTER UPDATE ON contracts để tự động hóa).
