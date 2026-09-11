-- ==========================================================
-- IDP SYSTEM FULL SEED DATA
-- Run this file after database/idp.sql on a clean database.
-- Default credentials:
--   admin@idp.local      / Admin@123
--   nguyen.thu.ha@idp.local / Manager@123
--   tran.quang.minh@idp.local / Manager@123
--   le.bao.ngoc@idp.local / Employee@123
--   pham.duc.long@idp.local / Employee@123
--   vo.minh.chau@idp.local / Employee@123
-- ==========================================================

BEGIN;

-- ----------------------------------------------------------
-- Align users.status with backend enum values
-- ----------------------------------------------------------
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_status_check;
ALTER TABLE users
ADD CONSTRAINT users_status_check
CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'LOCKED'));

-- ----------------------------------------------------------
-- Master data
-- ----------------------------------------------------------
UPDATE departments
SET description = CASE department_name
    WHEN 'IT' THEN 'Van hanh ha tang, tich hop he thong, quan tri AI service'
    WHEN 'HR' THEN 'Quan ly nhan su, onboarding, lao dong va chinh sach'
    WHEN 'Finance' THEN 'Kiem soat thanh toan, doi soat va ngan sach'
    WHEN 'Legal' THEN 'Tham dinh hop dong, quy trinh duyet va rui ro phap ly'
    ELSE description
END;

UPDATE document_types
SET description = CASE document_type_id
    WHEN 1 THEN 'Hop dong mua ban va cung ung hang hoa'
    WHEN 2 THEN 'Hop dong lao dong va thoa thuan nhan su'
    WHEN 3 THEN 'Hop dong dich vu, tu van, van hanh'
    WHEN 4 THEN 'Hop dong thue van phong, kho bai, tai san'
    ELSE description
END;

UPDATE ai_models
SET description = CASE model_name
    WHEN 'PaddleOCR' THEN 'Mo hinh OCR cho van ban tieng Viet va song ngu'
    WHEN 'YOLOv11' THEN 'Mo hinh phat hien vung chu ky, con dau, bang bieu'
    WHEN 'Qwen3' THEN 'Mo hinh tong hop, tom tat, hoi dap va phan tich rui ro'
    WHEN 'bge-m3' THEN 'Embedding cho tim kiem ngu nghia va RAG'
    ELSE description
END;

INSERT INTO permissions(permission_name, description)
VALUES
('AUTH_LOGIN', 'Dang nhap va quan ly phien'),
('AUTH_SESSION_VIEW', 'Xem danh sach phien dang nhap'),
('USER_VIEW', 'Xem thong tin nguoi dung'),
('USER_MANAGE', 'Tao, sua, khoa va gan role cho nguoi dung'),
('ROLE_VIEW', 'Xem danh sach role'),
('ROLE_MANAGE', 'Quan ly role va role-permission'),
('PERMISSION_VIEW', 'Xem danh sach permission'),
('PERMISSION_MANAGE', 'Quan ly permission'),
('DEPARTMENT_VIEW', 'Xem phong ban'),
('DEPARTMENT_MANAGE', 'Quan ly phong ban'),
('PARTNER_VIEW', 'Xem doi tac'),
('PARTNER_MANAGE', 'Quan ly doi tac'),
('DOCUMENT_TYPE_VIEW', 'Xem loai tai lieu'),
('DOCUMENT_TYPE_MANAGE', 'Quan ly loai tai lieu'),
('CONTRACT_VIEW', 'Xem hop dong'),
('CONTRACT_CREATE', 'Tao hop dong'),
('CONTRACT_UPDATE', 'Cap nhat hop dong va phien ban'),
('CONTRACT_DELETE', 'Xoa hop dong'),
('CONTRACT_APPROVE', 'Duyet va tu choi hop dong'),
('CONTRACT_SHARE', 'Chia se hop dong'),
('CONTRACT_FAVORITE', 'Danh dau hop dong yeu thich'),
('CONTRACT_DOWNLOAD', 'Tai file hop dong'),
('COMMENT_MANAGE', 'Binh luan va mention tren hop dong'),
('AI_PROCESS', 'Khoi dong pipeline AI'),
('AI_REVIEW', 'Xem OCR, metadata, summary, risk'),
('AI_CHAT', 'Hoi dap RAG tren hop dong'),
('PROCESSING_VIEW', 'Theo doi processing jobs'),
('NOTIFICATION_VIEW', 'Xem va cap nhat thong bao'),
('DASHBOARD_VIEW', 'Xem dashboard'),
('SETTING_MANAGE', 'Cap nhat cau hinh he thong'),
('API_KEY_MANAGE', 'Quan ly API key va tich hop')
ON CONFLICT (permission_name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON 1 = 1
WHERE r.role_name = 'Admin'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p
  ON p.permission_name IN (
      'AUTH_LOGIN',
      'AUTH_SESSION_VIEW',
      'USER_VIEW',
      'PARTNER_VIEW',
      'PARTNER_MANAGE',
      'DEPARTMENT_VIEW',
      'DOCUMENT_TYPE_VIEW',
      'DOCUMENT_TYPE_MANAGE',
      'CONTRACT_VIEW',
      'CONTRACT_CREATE',
      'CONTRACT_UPDATE',
      'CONTRACT_APPROVE',
      'CONTRACT_SHARE',
      'CONTRACT_FAVORITE',
      'CONTRACT_DOWNLOAD',
      'COMMENT_MANAGE',
      'AI_PROCESS',
      'AI_REVIEW',
      'AI_CHAT',
      'PROCESSING_VIEW',
      'NOTIFICATION_VIEW',
      'DASHBOARD_VIEW'
  )
WHERE r.role_name = 'Manager'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p
  ON p.permission_name IN (
      'AUTH_LOGIN',
      'AUTH_SESSION_VIEW',
      'PARTNER_VIEW',
      'DEPARTMENT_VIEW',
      'DOCUMENT_TYPE_VIEW',
      'CONTRACT_VIEW',
      'CONTRACT_CREATE',
      'CONTRACT_UPDATE',
      'CONTRACT_FAVORITE',
      'CONTRACT_DOWNLOAD',
      'COMMENT_MANAGE',
      'AI_REVIEW',
      'AI_CHAT',
      'PROCESSING_VIEW',
      'NOTIFICATION_VIEW',
      'DASHBOARD_VIEW'
  )
WHERE r.role_name = 'Employee'
ON CONFLICT DO NOTHING;

INSERT INTO system_settings(setting_key, setting_value, description)
VALUES
('dashboard.default_currency', '{"code":"VND","locale":"vi-VN"}'::jsonb, 'Tien te hien thi mac dinh tren dashboard'),
('ai.pipeline.thresholds', '{"ocr":0.92,"metadata":0.88,"risk_alert":0.75}'::jsonb, 'Nguong chat luong AI cho OCR, metadata va risk'),
('contract.reminder_policy', '{"days_before":[30,14,7],"email_enabled":true}'::jsonb, 'Chinh sach nhac hop dong sap het han'),
('storage.retention', '{"contracts_years":10,"audit_years":5}'::jsonb, 'Thoi gian luu tru ho so va audit'),
('security.password_policy', '{"min_length":8,"require_uppercase":true,"require_digit":true}'::jsonb, 'Chinh sach mat khau hien tai')
ON CONFLICT (setting_key) DO UPDATE
SET setting_value = EXCLUDED.setting_value,
    description = EXCLUDED.description;

INSERT INTO tags(tag_name)
VALUES
('Procurement'),
('Logistics'),
('HR'),
('Lease'),
('AI-Reviewed'),
('Risk-High'),
('Priority-Q3')
ON CONFLICT (tag_name) DO NOTHING;

INSERT INTO partners(company_name, partner_type, tax_code, phone, email, address, website, created_at, updated_at)
VALUES
('Alpha Manufacturing JSC', 'Supplier', '0319981001', '02873001001', 'contracts@alphamfg.vn', 'Lot C2, VSIP 1, Binh Duong', 'https://alphamfg.vn', '2026-01-03 08:00:00', '2026-01-03 08:00:00'),
('Green Logistics Co., Ltd', 'Service Provider', '0319981002', '02873001002', 'legal@greenlogistics.vn', '12 Nguyen Van Linh, District 7, Ho Chi Minh City', 'https://greenlogistics.vn', '2026-01-03 08:05:00', '2026-01-03 08:05:00'),
('EastBridge Consulting Vietnam', 'Consultant', '0319981003', '02873001003', 'pm@eastbridge.vn', '81 Dien Bien Phu, Binh Thanh, Ho Chi Minh City', 'https://eastbridge.vn', '2026-01-03 08:10:00', '2026-01-03 08:10:00'),
('Saigon Prime Office Holdings', 'Landlord', '0319981004', '02873001004', 'leasing@saigonprime.vn', '22 Ton Duc Thang, District 1, Ho Chi Minh City', 'https://saigonprime.vn', '2026-01-03 08:15:00', '2026-01-03 08:15:00'),
('Lotus Fintech Solutions', 'Technology Vendor', '0319981005', '02873001005', 'sales@lotusfintech.vn', '95 Vo Van Kiet, District 1, Ho Chi Minh City', 'https://lotusfintech.vn', '2026-01-03 08:20:00', '2026-01-03 08:20:00'),
('Bluewave Retail Corporation', 'Customer', '0319981006', '02873001006', 'sourcing@bluewave.vn', '118 Xa Lo Ha Noi, Thu Duc City', 'https://bluewave.vn', '2026-01-03 08:25:00', '2026-01-03 08:25:00');

-- ----------------------------------------------------------
-- Users and access control
-- ----------------------------------------------------------
INSERT INTO users(
    department_id,
    full_name,
    email,
    password_hash,
    phone,
    avatar,
    status,
    failed_login_count,
    last_login,
    is_deleted,
    created_at,
    updated_at,
    password_changed_at,
    email_verified,
    locked_until
)
VALUES
(
    (SELECT department_id FROM departments WHERE department_name = 'IT'),
    'System Administrator',
    'admin@idp.local',
    '$2a$10$5WZiaNqNKifLzxRoNADNZ.ckIYxNziY6vJ2eiK5BK28JyBIMsshAu',
    '0901000001',
    'https://i.pravatar.cc/150?img=12',
    'ACTIVE',
    0,
    '2026-07-30 08:15:00',
    FALSE,
    '2026-01-05 08:00:00',
    '2026-07-30 08:15:00',
    '2026-01-05 08:00:00',
    TRUE,
    NULL
),
(
    (SELECT department_id FROM departments WHERE department_name = 'Legal'),
    'Nguyen Thu Ha',
    'nguyen.thu.ha@idp.local',
    '$2a$10$2tjDTy9e6b/VkCigwIVjU.EBu0/ctaVjqHArPKpevHAcP/dQq/3SG',
    '0901000002',
    'https://i.pravatar.cc/150?img=32',
    'ACTIVE',
    0,
    '2026-07-29 17:10:00',
    FALSE,
    '2026-01-06 08:30:00',
    '2026-07-29 17:10:00',
    '2026-01-06 08:30:00',
    TRUE,
    NULL
),
(
    (SELECT department_id FROM departments WHERE department_name = 'Finance'),
    'Tran Quang Minh',
    'tran.quang.minh@idp.local',
    '$2a$10$2tjDTy9e6b/VkCigwIVjU.EBu0/ctaVjqHArPKpevHAcP/dQq/3SG',
    '0901000003',
    'https://i.pravatar.cc/150?img=15',
    'ACTIVE',
    0,
    '2026-07-30 07:50:00',
    FALSE,
    '2026-01-06 09:00:00',
    '2026-07-30 07:50:00',
    '2026-01-06 09:00:00',
    TRUE,
    NULL
),
(
    (SELECT department_id FROM departments WHERE department_name = 'Legal'),
    'Le Bao Ngoc',
    'le.bao.ngoc@idp.local',
    '$2a$10$LxnTkhj6WtIqvBCnj7ODD.LnKtN8/kpNXOxZVeEZG3G3tEVR4xZOO',
    '0901000004',
    'https://i.pravatar.cc/150?img=25',
    'ACTIVE',
    0,
    '2026-07-28 16:45:00',
    FALSE,
    '2026-01-07 09:30:00',
    '2026-07-28 16:45:00',
    '2026-01-07 09:30:00',
    TRUE,
    NULL
),
(
    (SELECT department_id FROM departments WHERE department_name = 'HR'),
    'Pham Duc Long',
    'pham.duc.long@idp.local',
    '$2a$10$LxnTkhj6WtIqvBCnj7ODD.LnKtN8/kpNXOxZVeEZG3G3tEVR4xZOO',
    '0901000005',
    'https://i.pravatar.cc/150?img=44',
    'ACTIVE',
    0,
    '2026-07-27 18:20:00',
    FALSE,
    '2026-01-07 10:00:00',
    '2026-07-27 18:20:00',
    '2026-01-07 10:00:00',
    TRUE,
    NULL
),
(
    (SELECT department_id FROM departments WHERE department_name = 'IT'),
    'Vo Minh Chau',
    'vo.minh.chau@idp.local',
    '$2a$10$LxnTkhj6WtIqvBCnj7ODD.LnKtN8/kpNXOxZVeEZG3G3tEVR4xZOO',
    '0901000006',
    'https://i.pravatar.cc/150?img=47',
    'ACTIVE',
    0,
    '2026-07-30 06:55:00',
    FALSE,
    '2026-01-07 10:30:00',
    '2026-07-30 06:55:00',
    '2026-01-07 10:30:00',
    TRUE,
    NULL
);

INSERT INTO user_roles(user_id, role_id, assigned_by, assigned_at)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    (SELECT role_id FROM roles WHERE role_name = 'Admin'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-01-05 08:05:00'
),
(
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    (SELECT role_id FROM roles WHERE role_name = 'Manager'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-01-06 08:35:00'
),
(
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    (SELECT role_id FROM roles WHERE role_name = 'Manager'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-01-06 09:05:00'
),
(
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'),
    (SELECT role_id FROM roles WHERE role_name = 'Employee'),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    '2026-01-07 09:35:00'
),
(
    (SELECT user_id FROM users WHERE email = 'pham.duc.long@idp.local'),
    (SELECT role_id FROM roles WHERE role_name = 'Employee'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-01-07 10:05:00'
),
(
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    (SELECT role_id FROM roles WHERE role_name = 'Employee'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-01-07 10:35:00'
);

INSERT INTO notification_settings(
    user_id,
    email_enabled,
    system_notification,
    contract_new,
    contract_approval,
    contract_expiring,
    comment_mention,
    updated_at
)
SELECT
    u.user_id,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    '2026-07-30 08:00:00'
FROM users u
WHERE u.email IN (
    'admin@idp.local',
    'nguyen.thu.ha@idp.local',
    'tran.quang.minh@idp.local',
    'le.bao.ngoc@idp.local',
    'pham.duc.long@idp.local',
    'vo.minh.chau@idp.local'
)
ON CONFLICT (user_id) DO UPDATE
SET email_enabled = EXCLUDED.email_enabled,
    system_notification = EXCLUDED.system_notification,
    contract_new = EXCLUDED.contract_new,
    contract_approval = EXCLUDED.contract_approval,
    contract_expiring = EXCLUDED.contract_expiring,
    comment_mention = EXCLUDED.comment_mention,
    updated_at = EXCLUDED.updated_at;

-- ----------------------------------------------------------
-- Contracts
-- ----------------------------------------------------------
INSERT INTO contracts(
    document_type_id,
    partner_id,
    uploaded_by,
    contract_number,
    contract_name,
    partner_representative_name,
    partner_representative_position,
    signed_date,
    effective_date,
    expired_date,
    total_value,
    currency,
    status,
    current_version_id,
    created_at,
    updated_at
)
VALUES
(
    1,
    (SELECT partner_id FROM partners WHERE tax_code = '0319981001'),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'IDP-2026-001',
    'Master Purchase Agreement for Production Materials',
    'Bui Quoc Hung',
    'Procurement Director',
    '2026-01-05',
    '2026-01-10',
    '2027-01-09',
    3500000000.00,
    'VND',
    'Approved',
    NULL,
    '2026-01-05 09:00:00',
    '2026-02-18 15:20:00'
),
(
    3,
    (SELECT partner_id FROM partners WHERE tax_code = '0319981002'),
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'),
    'IDP-2026-002',
    'Regional Freight and Distribution Service Agreement',
    'Nguyen Van Dat',
    'Operations Director',
    '2026-06-15',
    '2026-06-20',
    '2027-06-19',
    850000000.00,
    'VND',
    'Processing',
    NULL,
    '2026-06-15 10:15:00',
    '2026-07-29 16:40:00'
),
(
    2,
    (SELECT partner_id FROM partners WHERE tax_code = '0319981003'),
    (SELECT user_id FROM users WHERE email = 'pham.duc.long@idp.local'),
    'IDP-2026-003',
    'Draft Workforce Outsourcing Framework Agreement',
    'Le Thanh Son',
    'Delivery Manager',
    NULL,
    NULL,
    '2026-12-31',
    680000000.00,
    'VND',
    'Draft',
    NULL,
    '2026-07-10 09:20:00',
    '2026-07-30 08:00:00'
),
(
    4,
    (SELECT partner_id FROM partners WHERE tax_code = '0319981004'),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'IDP-2026-004',
    'Head Office Lease Agreement - Floor 18',
    'Tran Bao Khoa',
    'Leasing Director',
    '2026-07-01',
    '2026-07-31',
    '2029-07-30',
    2100000000.00,
    'VND',
    'Pending',
    NULL,
    '2026-07-01 14:00:00',
    '2026-07-30 09:10:00'
),
(
    3,
    (SELECT partner_id FROM partners WHERE tax_code = '0319981005'),
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    'IDP-2026-005',
    'Software Subscription and Support Agreement',
    'Pham Gia Huy',
    'Enterprise Sales Director',
    '2026-03-10',
    '2026-03-15',
    '2027-03-14',
    1250000000.00,
    'VND',
    'Rejected',
    NULL,
    '2026-03-10 11:00:00',
    '2026-04-02 18:00:00'
),
(
    3,
    (SELECT partner_id FROM partners WHERE tax_code = '0319981003'),
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'IDP-2025-006',
    'Internal Audit and Compliance Advisory Agreement',
    'Le Thanh Son',
    'Delivery Manager',
    '2025-01-10',
    '2025-01-15',
    '2026-06-30',
    420000000.00,
    'VND',
    'Expired',
    NULL,
    '2025-01-10 09:00:00',
    '2026-06-30 17:00:00'
),
(
    4,
    (SELECT partner_id FROM partners WHERE tax_code = '0319981001'),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'IDP-2024-007',
    'Warehouse Lease for Finished Goods Storage',
    'Bui Quoc Hung',
    'Procurement Director',
    '2024-02-01',
    '2024-02-15',
    '2027-02-14',
    900000000.00,
    'VND',
    'Terminated',
    NULL,
    '2024-02-01 08:45:00',
    '2026-05-15 16:30:00'
);

INSERT INTO contract_versions(
    contract_id,
    version_number,
    edited_by,
    change_note,
    status,
    created_at,
    is_current
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    1,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Ban phat hanh ban dau sau khi doi tac thong nhat quy cach vat tu.',
    'Published',
    '2026-01-05 09:15:00',
    FALSE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    2,
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'Cap nhat SLA giao hang va bo sung muc phat cham giao.',
    'Published',
    '2026-02-18 15:10:00',
    TRUE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002'),
    1,
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'),
    'Ban dang xu ly OCR, trich xuat metadata va doi soat phu luc.',
    'Published',
    '2026-06-15 10:25:00',
    TRUE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-003'),
    1,
    (SELECT user_id FROM users WHERE email = 'pham.duc.long@idp.local'),
    'Ban nhap de HR va Legal review truoc khi gui nha cung cap.',
    'Draft',
    '2026-07-10 09:30:00',
    TRUE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    1,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Da bo sung dieu khoan dat coc va lich thanh toan tien thue.',
    'Published',
    '2026-07-01 14:20:00',
    TRUE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'),
    1,
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    'Ban de xuat ban dau tu bo phan CNTT.',
    'Published',
    '2026-03-10 11:10:00',
    FALSE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'),
    2,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'Dieu chinh pham vi ho tro sau khi danh gia rui ro bao mat.',
    'Published',
    '2026-04-02 17:40:00',
    TRUE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2025-006'),
    1,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'Ban da het han, luu de doi chieu audit nam 2026.',
    'Published',
    '2025-01-10 09:10:00',
    TRUE
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2024-007'),
    1,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Ban chot cuoi truoc khi thuc hien thanh ly som.',
    'Published',
    '2024-02-01 09:00:00',
    TRUE
);

UPDATE contracts
SET current_version_id = (
    SELECT cv.version_id
    FROM contract_versions cv
    WHERE cv.contract_id = contracts.contract_id
      AND cv.is_current = TRUE
)
WHERE contract_number IN (
    'IDP-2026-001',
    'IDP-2026-002',
    'IDP-2026-003',
    'IDP-2026-004',
    'IDP-2026-005',
    'IDP-2025-006',
    'IDP-2024-007'
);

INSERT INTO contract_files(
    version_id,
    file_name,
    file_path,
    file_type,
    file_size,
    page_count,
    file_hash,
    uploaded_at
)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'alpha_manufacturing_master_purchase_v2.txt',
    'storage/contracts/IDP-2026-001/alpha_manufacturing_master_purchase_v2.txt',
    'text/plain',
    2148,
    6,
    'seed-hash-idp-2026-001-v2',
    '2026-02-18 15:15:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002') AND version_number = 1),
    'green_logistics_freight_service_v1.txt',
    'storage/contracts/IDP-2026-002/green_logistics_freight_service_v1.txt',
    'text/plain',
    1987,
    5,
    'seed-hash-idp-2026-002-v1',
    '2026-06-15 10:30:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-003') AND version_number = 1),
    'eastbridge_hr_outsourcing_draft_v1.txt',
    'storage/contracts/IDP-2026-003/eastbridge_hr_outsourcing_draft_v1.txt',
    'text/plain',
    1760,
    4,
    'seed-hash-idp-2026-003-v1',
    '2026-07-10 09:35:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    'saigon_prime_office_lease_v1.txt',
    'storage/contracts/IDP-2026-004/saigon_prime_office_lease_v1.txt',
    'text/plain',
    2012,
    5,
    'seed-hash-idp-2026-004-v1',
    '2026-07-01 14:25:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005') AND version_number = 2),
    'lotus_fintech_software_license_v2.txt',
    'storage/contracts/IDP-2026-005/lotus_fintech_software_license_v2.txt',
    'text/plain',
    1876,
    4,
    'seed-hash-idp-2026-005-v2',
    '2026-04-02 17:45:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2025-006') AND version_number = 1),
    'eastbridge_internal_audit_service_v1.txt',
    'storage/contracts/IDP-2025-006/eastbridge_internal_audit_service_v1.txt',
    'text/plain',
    1690,
    4,
    'seed-hash-idp-2025-006-v1',
    '2025-01-10 09:15:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2024-007') AND version_number = 1),
    'alpha_warehouse_lease_v1.txt',
    'storage/contracts/IDP-2024-007/alpha_warehouse_lease_v1.txt',
    'text/plain',
    1825,
    5,
    'seed-hash-idp-2024-007-v1',
    '2024-02-01 09:05:00'
);

INSERT INTO contract_tags(contract_id, tag_id)
VALUES
((SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'), (SELECT tag_id FROM tags WHERE tag_name = 'Procurement')),
((SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'), (SELECT tag_id FROM tags WHERE tag_name = 'AI-Reviewed')),
((SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002'), (SELECT tag_id FROM tags WHERE tag_name = 'Logistics')),
((SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-003'), (SELECT tag_id FROM tags WHERE tag_name = 'HR')),
((SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'), (SELECT tag_id FROM tags WHERE tag_name = 'Lease')),
((SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'), (SELECT tag_id FROM tags WHERE tag_name = 'Priority-Q3')),
((SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'), (SELECT tag_id FROM tags WHERE tag_name = 'Risk-High'));

INSERT INTO contract_parties(
    contract_id,
    partner_id,
    party_role,
    representative_name,
    representative_position,
    signed,
    created_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    (SELECT partner_id FROM partners WHERE tax_code = '0319981001'),
    'Counterparty',
    'Bui Quoc Hung',
    'Procurement Director',
    TRUE,
    '2026-01-05 09:05:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002'),
    (SELECT partner_id FROM partners WHERE tax_code = '0319981002'),
    'LogisticsProvider',
    'Nguyen Van Dat',
    'Operations Director',
    TRUE,
    '2026-06-15 10:20:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-003'),
    (SELECT partner_id FROM partners WHERE tax_code = '0319981003'),
    'OutsourcingVendor',
    'Le Thanh Son',
    'Delivery Manager',
    FALSE,
    '2026-07-10 09:25:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    (SELECT partner_id FROM partners WHERE tax_code = '0319981004'),
    'Lessor',
    'Tran Bao Khoa',
    'Leasing Director',
    TRUE,
    '2026-07-01 14:10:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'),
    (SELECT partner_id FROM partners WHERE tax_code = '0319981005'),
    'SoftwareVendor',
    'Pham Gia Huy',
    'Enterprise Sales Director',
    TRUE,
    '2026-03-10 11:05:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2025-006'),
    (SELECT partner_id FROM partners WHERE tax_code = '0319981003'),
    'Consultant',
    'Le Thanh Son',
    'Delivery Manager',
    TRUE,
    '2025-01-10 09:05:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2024-007'),
    (SELECT partner_id FROM partners WHERE tax_code = '0319981001'),
    'Lessor',
    'Bui Quoc Hung',
    'Procurement Director',
    TRUE,
    '2024-02-01 08:50:00'
);

INSERT INTO favorite_contracts(user_id, contract_id, created_at)
VALUES
((SELECT user_id FROM users WHERE email = 'admin@idp.local'), (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'), '2026-07-25 09:00:00'),
((SELECT user_id FROM users WHERE email = 'admin@idp.local'), (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'), '2026-07-26 14:30:00'),
((SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'), (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'), '2026-07-21 11:00:00'),
((SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'), (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002'), '2026-07-22 15:00:00'),
((SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'), (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'), '2026-07-29 09:20:00');

INSERT INTO approval_workflow(
    contract_id,
    step_number,
    approver_id,
    status,
    comment,
    created_at,
    approved_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    1,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Approved',
    'Da kiem tra dieu khoan giao hang va phat cham tien do.',
    '2026-01-06 09:00:00',
    '2026-01-06 11:15:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    2,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'Approved',
    'Ngan sach va lich thanh toan dap ung ke hoach quy 1.',
    '2026-01-06 11:20:00',
    '2026-01-06 14:05:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    1,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Approved',
    'Dieu khoan dat coc da duoc chinh sua theo chinh sach cong ty.',
    '2026-07-02 09:00:00',
    '2026-07-02 10:30:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    2,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'Pending',
    'Cho CFO xac nhan ngan sach van phong 2026-2027.',
    '2026-07-02 10:35:00',
    NULL
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'),
    1,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'Rejected',
    'Phi giay phep vuot ngan sach va dieu khoan gioi han trach nhiem qua bat loi.',
    '2026-04-01 15:00:00',
    '2026-04-02 09:45:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'),
    2,
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'Skipped',
    'Dung luong duyet do da bi tu choi tai buoc tai chinh.',
    '2026-04-02 09:50:00',
    '2026-04-02 09:50:00'
);

INSERT INTO contract_payments(
    contract_id,
    installment_no,
    description,
    amount,
    paid_amount,
    due_date,
    paid_date,
    status,
    confirmed_by,
    created_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    1,
    'Dat coc 30% sau khi ky ket',
    1050000000.00,
    1050000000.00,
    '2026-01-20',
    '2026-01-19',
    'Paid',
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2026-01-06 15:00:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    2,
    'Thanh toan 40% khi giao du 70% khoi luong',
    1400000000.00,
    1400000000.00,
    '2026-04-30',
    '2026-04-29',
    'Paid',
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2026-01-06 15:05:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    3,
    'Thanh toan 30% con lai sau nghiem thu',
    1050000000.00,
    0.00,
    '2026-09-30',
    NULL,
    'Pending',
    NULL,
    '2026-01-06 15:10:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    1,
    'Tien dat coc 2 thang',
    350000000.00,
    0.00,
    '2026-08-05',
    NULL,
    'Pending',
    NULL,
    '2026-07-02 11:00:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2025-006'),
    1,
    'Phi tu van dot 1',
    420000000.00,
    420000000.00,
    '2025-03-15',
    '2025-03-14',
    'Paid',
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2025-01-12 09:30:00'
);

INSERT INTO contract_appendices(
    contract_id,
    appendix_number,
    title,
    description,
    file_path,
    signed_date,
    effective_date,
    created_by,
    created_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    'PL-01',
    'Danh muc vat tu va SLA giao hang',
    'Phu luc cap nhat quy cach vat tu, lead time va KPI giao hang.',
    'storage/contracts/IDP-2026-001/appendix_material_sla.txt',
    '2026-02-18',
    '2026-02-18',
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-02-18 15:25:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    'PL-01',
    'Bang dien tich va phi dich vu',
    'Phu luc chi tiet dien tich su dung, phi quan ly va bang gia cho xe.',
    'storage/contracts/IDP-2026-004/appendix_floor18_service_fee.txt',
    '2026-07-01',
    '2026-07-31',
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    '2026-07-01 14:40:00'
);

INSERT INTO contract_lifecycle(
    contract_id,
    action_type,
    old_expired_date,
    new_expired_date,
    reason,
    performed_by,
    performed_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2025-006'),
    'Renew',
    '2025-12-31',
    '2026-06-30',
    'Gia han 6 thang de hoan thanh audit va remediate control gap.',
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2025-12-20 10:00:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2024-007'),
    'Terminate',
    '2027-02-14',
    '2026-05-15',
    'Chuyen toan bo hang ton sang kho tap trung moi tai Long An.',
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    '2026-05-15 16:00:00'
);

INSERT INTO contract_shares(
    contract_id,
    shared_by,
    shared_to,
    permission,
    shared_at,
    expires_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    'VIEW',
    '2026-07-20 09:00:00',
    '2026-12-31 23:59:59'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'),
    'EDIT',
    '2026-07-24 14:00:00',
    '2026-09-30 23:59:59'
);

-- ----------------------------------------------------------
-- AI extraction, summaries and chat
-- ----------------------------------------------------------
INSERT INTO ocr_results(version_id, page_number, language, engine, ocr_text, confidence, created_at)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    1,
    'vi',
    'PaddleOCR',
    'Master Purchase Agreement between IDP Company and Alpha Manufacturing JSC. Effective date 10/01/2026. Total value VND 3,500,000,000.',
    0.982,
    '2026-02-18 15:30:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    2,
    'vi',
    'PaddleOCR',
    'Delivery SLA: 95 percent on-time rate. Delay penalty: 0.05 percent per delayed day, capped at 8 percent of delayed lot value.',
    0.974,
    '2026-02-18 15:31:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002') AND version_number = 1),
    1,
    'vi',
    'PaddleOCR',
    'Regional Freight and Distribution Service Agreement. Scope includes warehousing, inland trucking and last-mile distribution in the South region.',
    0.966,
    '2026-06-15 10:40:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    1,
    'vi',
    'PaddleOCR',
    'Head Office Lease Agreement for Floor 18, Saigon Prime Tower. Deposit equals two months of base rent.',
    0.971,
    '2026-07-01 14:50:00'
);

INSERT INTO detection_regions(version_id, page_number, label, x_min, y_min, x_max, y_max, confidence)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    1,
    'signature_block',
    0.71, 0.82, 0.95, 0.96,
    0.943
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    2,
    'payment_table',
    0.08, 0.41, 0.93, 0.74,
    0.917
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    1,
    'lease_fee_table',
    0.11, 0.36, 0.91, 0.68,
    0.924
);

INSERT INTO ai_metadata(
    version_id,
    field_name,
    field_type,
    original_value,
    current_value,
    confidence,
    verified,
    verified_by,
    verified_at,
    normalized_value
)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'contract_number',
    'STRING',
    to_jsonb('IDP-2026-001'::text),
    to_jsonb('IDP-2026-001'::text),
    0.995,
    TRUE,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    '2026-02-18 16:00:00',
    to_jsonb('IDP-2026-001'::text)
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'effective_date',
    'DATE',
    to_jsonb('10/01/2026'::text),
    to_jsonb('2026-01-10'::text),
    0.981,
    TRUE,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    '2026-02-18 16:02:00',
    to_jsonb('2026-01-10'::text)
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'total_value',
    'MONEY',
    '{"amount":"3.500.000.000","currency":"VND"}'::jsonb,
    '{"amount":3500000000,"currency":"VND"}'::jsonb,
    0.976,
    TRUE,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2026-02-18 16:05:00',
    '{"amount":3500000000,"currency":"VND"}'::jsonb
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    'deposit_months',
    'NUMBER',
    to_jsonb('02'::text),
    to_jsonb(2),
    0.944,
    TRUE,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2026-07-02 11:20:00',
    to_jsonb(2)
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    'monthly_rent',
    'MONEY',
    '{"amount":"175.000.000","currency":"VND"}'::jsonb,
    '{"amount":175000000,"currency":"VND"}'::jsonb,
    0.932,
    FALSE,
    NULL,
    NULL,
    '{"amount":175000000,"currency":"VND"}'::jsonb
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005') AND version_number = 2),
    'liability_cap',
    'PERCENT',
    to_jsonb('100% annual fee'::text),
    to_jsonb('100% annual fee'::text),
    0.903,
    FALSE,
    NULL,
    NULL,
    to_jsonb('100_percent_annual_fee'::text)
);

INSERT INTO metadata_history(metadata_id, old_value, new_value, edited_by, edited_at)
VALUES
(
    (
        SELECT metadata_id
        FROM ai_metadata
        WHERE field_name = 'total_value'
          AND version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2)
    ),
    '{"amount":3480000000,"currency":"VND"}'::jsonb,
    '{"amount":3500000000,"currency":"VND"}'::jsonb,
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2026-02-18 16:06:00'
),
(
    (
        SELECT metadata_id
        FROM ai_metadata
        WHERE field_name = 'monthly_rent'
          AND version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1)
    ),
    '{"amount":170000000,"currency":"VND"}'::jsonb,
    '{"amount":175000000,"currency":"VND"}'::jsonb,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    '2026-07-02 11:25:00'
);

INSERT INTO ai_summary(version_id, summary, model_id, created_at, summary_json)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'Hop dong mua ban da duoc phe duyet. Doi tac cam ket SLA giao hang 95%, muc phat cham toi da 8% gia tri lo hang cham. Tong gia tri 3.5 ty VND.',
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    '2026-02-18 16:20:00',
    '{"parties":["IDP Company","Alpha Manufacturing JSC"],"key_terms":["SLA 95%","delay penalty 0.05%/day","cap 8%"],"payment":"30-40-30","risk_level":"Low"}'::jsonb
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002') AND version_number = 1),
    'Hop dong logistics dang o trang thai processing. Pham vi bao gom kho, van chuyen noi dia va giao hang chang cuoi khu vuc phia Nam.',
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    '2026-06-15 11:00:00',
    '{"parties":["IDP Company","Green Logistics Co., Ltd"],"scope":["warehousing","inland trucking","last-mile distribution"],"status":"Processing"}'::jsonb
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    'Hop dong thue van phong dang cho phe duyet tai buoc tai chinh. Dat coc 2 thang, thoi han thue 36 thang, gia thue hang thang 175 trieu VND.',
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    '2026-07-02 11:30:00',
    '{"term_months":36,"deposit_months":2,"monthly_rent":175000000,"workflow_status":"Pending Finance"}'::jsonb
);

INSERT INTO embedding_info(version_id, vector_index, model_id, chunk_count, created_at)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'contracts/idp-2026-001/v2',
    (SELECT model_id FROM ai_models WHERE model_name = 'bge-m3'),
    3,
    '2026-02-18 16:30:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    'contracts/idp-2026-004/v1',
    (SELECT model_id FROM ai_models WHERE model_name = 'bge-m3'),
    3,
    '2026-07-02 11:40:00'
);

INSERT INTO embedding_chunks(embedding_id, chunk_index, chunk_text, vector_id, page_number, created_at)
VALUES
(
    (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2)),
    1,
    'Khoi 1: Thong tin chung, gia tri hop dong, hieu luc va doi tuong giao dich.',
    'vec-idp-2026-001-1',
    1,
    '2026-02-18 16:31:00'
),
(
    (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2)),
    2,
    'Khoi 2: SLA giao hang, KPI, phat cham tien do va co che nghiem thu.',
    'vec-idp-2026-001-2',
    2,
    '2026-02-18 16:31:30'
),
(
    (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2)),
    3,
    'Khoi 3: Dieu khoan thanh toan, bao mat, bao hanh va giai quyet tranh chap.',
    'vec-idp-2026-001-3',
    3,
    '2026-02-18 16:32:00'
),
(
    (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1)),
    1,
    'Khoi 1: Mo ta mat bang, dien tich thue va trang thai ban giao.',
    'vec-idp-2026-004-1',
    1,
    '2026-07-02 11:41:00'
),
(
    (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1)),
    2,
    'Khoi 2: Gia thue, phi dich vu, dat coc va chi phi bo sung.',
    'vec-idp-2026-004-2',
    2,
    '2026-07-02 11:41:30'
),
(
    (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1)),
    3,
    'Khoi 3: Dieu khoan cham dut, gia han va xu ly vi pham.',
    'vec-idp-2026-004-3',
    3,
    '2026-07-02 11:42:00'
);

INSERT INTO ai_chat_history(version_id, user_id, question, answer, response_time_ms, created_at, conversation_id, source_chunk_ids)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'Muc phat cham giao hang trong hop dong nay la bao nhieu?',
    'Hop dong quy dinh muc phat 0.05% moi ngay cham, tong muc phat toi da bang 8% gia tri lo hang bi cham.',
    892,
    '2026-07-28 09:05:00',
    '11111111-1111-1111-1111-111111111111',
    ARRAY(
        SELECT chunk_id
        FROM embedding_chunks
        WHERE embedding_id = (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2))
          AND chunk_index IN (2, 3)
        ORDER BY chunk_index
    )
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Cho toi tom tat nhanh cach thanh toan.',
    'Lich thanh toan theo mo hinh 30-40-30: dat coc sau ky ket, thanh toan khi giao 70% khoi luong, va thanh toan phan con lai sau nghiem thu.',
    735,
    '2026-07-28 09:08:00',
    '11111111-1111-1111-1111-111111111111',
    ARRAY(
        SELECT chunk_id
        FROM embedding_chunks
        WHERE embedding_id = (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2))
          AND chunk_index = 3
    )
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'Dat coc cua hop dong thue van phong bang bao nhieu thang?',
    'Dat coc bang 2 thang tien thue co ban, va hien van dang cho phe duyet buoc tai chinh.',
    801,
    '2026-07-29 15:15:00',
    '22222222-2222-2222-2222-222222222222',
    ARRAY(
        SELECT chunk_id
        FROM embedding_chunks
        WHERE embedding_id = (SELECT embedding_id FROM embedding_info WHERE version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1))
          AND chunk_index = 2
    )
);

INSERT INTO ai_risk_analysis(
    version_id,
    model_id,
    risk_level,
    risk_score,
    risk_summary,
    recommendation,
    risk_details,
    created_at
)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    'Low',
    0.21,
    'Rui ro thap, dieu khoan phat va bao hanh ro rang, nghia vu hai ben can bang.',
    'Theo doi sat KPI giao hang trong 2 dot dau de kiem tra nang luc thuc thi cua nha cung cap.',
    '{"risk_items":[{"code":"DELIVERY_DELAY","severity":"LOW"},{"code":"PRICE_ADJUSTMENT","severity":"LOW"}]}'::jsonb,
    '2026-02-18 16:40:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1),
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    'Medium',
    0.58,
    'Rui ro trung binh do co dieu khoan escalation phi dich vu theo CPI va nghia vu dat coc lon.',
    'Can bo sung tran tang phi nam va dieu kien hoan tra dat coc neu ben cho thue cham ban giao.',
    '{"risk_items":[{"code":"CPI_ESCALATION","severity":"MEDIUM"},{"code":"DEPOSIT_EXPOSURE","severity":"MEDIUM"}]}'::jsonb,
    '2026-07-02 11:50:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005') AND version_number = 2),
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    'High',
    0.87,
    'Rui ro cao do gioi han trach nhiem, quyen audit va yeu cau bao mat chua dap ung policy noi bo.',
    'Khong phe duyet cho den khi bo sung DPA, audit clause va tran trach nhiem cao hon.',
    '{"risk_items":[{"code":"DATA_PRIVACY","severity":"HIGH"},{"code":"LIABILITY_CAP","severity":"HIGH"},{"code":"SUBPROCESSOR_CONTROL","severity":"HIGH"}]}'::jsonb,
    '2026-04-02 17:55:00'
);

-- ----------------------------------------------------------
-- Comments and notifications
-- ----------------------------------------------------------
INSERT INTO comments(contract_id, metadata_id, parent_comment_id, user_id, content, page_number, created_at)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    (
        SELECT metadata_id
        FROM ai_metadata
        WHERE field_name = 'total_value'
          AND version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2)
    ),
    NULL,
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'),
    'De nghi kiem tra lai dieu khoan phat cham giao hang o muc 8.2 va doi soat tong gia tri don hang.',
    2,
    '2026-07-28 10:00:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    (
        SELECT metadata_id
        FROM ai_metadata
        WHERE field_name = 'monthly_rent'
          AND version_id = (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND version_number = 1)
    ),
    NULL,
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Nho team Finance xac nhan lai muc gia thue hang thang va tac dong ngan sach nam 2026.',
    1,
    '2026-07-29 14:40:00'
);

INSERT INTO comments(contract_id, metadata_id, parent_comment_id, user_id, content, page_number, created_at)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    NULL,
    (
        SELECT comment_id
        FROM comments
        WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001')
          AND content = 'De nghi kiem tra lai dieu khoan phat cham giao hang o muc 8.2 va doi soat tong gia tri don hang.'
    ),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'Da ghi nhan. Legal chot muc phat, Finance xac nhan lai tong gia tri 3.5 ty truoc khi khoa ban.',
    2,
    '2026-07-28 10:15:00'
);

INSERT INTO comment_mentions(comment_id, mentioned_user_id)
VALUES
(
    (
        SELECT comment_id
        FROM comments
        WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001')
          AND content = 'De nghi kiem tra lai dieu khoan phat cham giao hang o muc 8.2 va doi soat tong gia tri don hang.'
    ),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local')
),
(
    (
        SELECT comment_id
        FROM comments
        WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001')
          AND content = 'De nghi kiem tra lai dieu khoan phat cham giao hang o muc 8.2 va doi soat tong gia tri don hang.'
    ),
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local')
),
(
    (
        SELECT comment_id
        FROM comments
        WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004')
          AND content = 'Nho team Finance xac nhan lai muc gia thue hang thang va tac dong ngan sach nam 2026.'
    ),
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local')
),
(
    (
        SELECT comment_id
        FROM comments
        WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004')
          AND content = 'Nho team Finance xac nhan lai muc gia thue hang thang va tac dong ngan sach nam 2026.'
    ),
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local')
);

INSERT INTO notifications(user_id, title, content, type, link, is_read, created_at, read_at)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'Co hop dong can xem xet',
    'Hop dong IDP-2026-004 dang cho buoc phe duyet cuoi cung.',
    'CONTRACT_APPROVAL',
    '/contracts/' || (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    FALSE,
    '2026-07-30 08:10:00',
    NULL
),
(
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'Ban duoc mention trong hop dong mua ban',
    'Le Bao Ngoc da mention ban trong hop dong IDP-2026-001.',
    'COMMENT_MENTION',
    '/contracts/' || (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    FALSE,
    '2026-07-28 10:01:00',
    NULL
),
(
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'),
    'Hop dong thue van phong dang cho doi soat',
    'Manager Legal da de nghi doi soat lai muc gia thue hang thang cua IDP-2026-004.',
    'CONTRACT_REVIEW',
    '/contracts/' || (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    TRUE,
    '2026-07-29 14:41:00',
    '2026-07-29 14:50:00'
),
(
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'AI da cap nhat tom tat hop dong',
    'Tom tat cau truc va ket qua risk analysis cua IDP-2026-001 da san sang.',
    'AI_RESULT_READY',
    '/contracts/' || (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    TRUE,
    '2026-02-18 16:45:00',
    '2026-02-18 17:00:00'
),
(
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    'Hop dong bi tu choi',
    'IDP-2026-005 bi tu choi do rui ro bao mat va gioi han trach nhiem.',
    'CONTRACT_REJECTED',
    '/contracts/' || (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'),
    FALSE,
    '2026-04-02 18:05:00',
    NULL
),
(
    (SELECT user_id FROM users WHERE email = 'pham.duc.long@idp.local'),
    'Ban nhap HR da duoc tao',
    'Ban nhap IDP-2026-003 da duoc khoi tao va cho bo phan Legal review.',
    'CONTRACT_DRAFT',
    '/contracts/' || (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-003'),
    TRUE,
    '2026-07-10 09:40:00',
    '2026-07-10 10:00:00'
);

-- ----------------------------------------------------------
-- Processing and AI operations
-- ----------------------------------------------------------
INSERT INTO processing_queue(
    version_id,
    task_type,
    queue_name,
    worker_name,
    status,
    priority,
    retry_count,
    created_at,
    started_at,
    finished_at,
    progress,
    error_message
)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'FULL_PIPELINE',
    'ai-main',
    'worker-ocr-01',
    'COMPLETED',
    1,
    0,
    '2026-02-18 15:28:00',
    '2026-02-18 15:29:00',
    '2026-02-18 16:45:00',
    100,
    NULL
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002') AND version_number = 1),
    'FULL_PIPELINE',
    'ai-main',
    'worker-ocr-02',
    'RUNNING',
    2,
    0,
    '2026-07-29 16:00:00',
    '2026-07-29 16:01:00',
    NULL,
    68,
    NULL
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005') AND version_number = 2),
    'RISK_ANALYSIS',
    'ai-risk',
    'worker-risk-01',
    'FAILED',
    1,
    1,
    '2026-04-02 17:46:00',
    '2026-04-02 17:47:00',
    '2026-04-02 17:55:00',
    100,
    'Risk policy gate khong dat yeu cau bao mat du lieu.'
);

INSERT INTO processing_logs(
    version_id,
    model_id,
    step,
    status,
    duration,
    error_message,
    created_at,
    request_id
)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    (SELECT model_id FROM ai_models WHERE model_name = 'PaddleOCR'),
    'OCR',
    'Success',
    12.4,
    NULL,
    '2026-02-18 15:35:00',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    'SUMMARY',
    'Success',
    4.8,
    NULL,
    '2026-02-18 16:20:00',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002') AND version_number = 1),
    (SELECT model_id FROM ai_models WHERE model_name = 'PaddleOCR'),
    'OCR',
    'Success',
    10.1,
    NULL,
    '2026-07-29 16:05:00',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-002') AND version_number = 1),
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    'METADATA',
    'Success',
    6.7,
    NULL,
    '2026-07-29 16:10:00',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005') AND version_number = 2),
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    'RISK_ANALYSIS',
    'Fail',
    7.3,
    'Security policy gate phat hien dieu khoan du lieu va subprocessor chua dat.',
    '2026-04-02 17:55:00',
    'cccccccc-cccc-cccc-cccc-cccccccccccc'
);

INSERT INTO ai_prompts(prompt_name, prompt_content, model_id, created_by, created_at)
VALUES
(
    'SEED_SUMMARY_PROMPT_V1',
    'Tom tat hop dong theo cau truc: parties, obligations, payment, risk, next action.',
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-01-05 10:00:00'
),
(
    'SEED_RISK_PROMPT_V1',
    'Danh gia rui ro phap ly, bao mat, thanh toan, SLA va dat muc do Low/Medium/High/Critical.',
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-01-05 10:05:00'
),
(
    'SEED_CHAT_SYSTEM_PROMPT_V1',
    'Tra loi ngan gon, trich dan dung dieu khoan tu contract chunks neu co.',
    (SELECT model_id FROM ai_models WHERE model_name = 'Qwen3'),
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    '2026-01-05 10:10:00'
);

INSERT INTO contract_comparisons(
    source_version_id,
    target_version_id,
    comparison_result,
    compared_by,
    created_at
)
VALUES
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 1),
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001') AND version_number = 2),
    'Ban v2 bo sung muc phat cham giao 0.05%/ngay va cap nhat SLA tu 93% len 95%.',
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-02-18 16:55:00'
),
(
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005') AND version_number = 1),
    (SELECT version_id FROM contract_versions WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005') AND version_number = 2),
    'Ban v2 giam pham vi ho tro nhung van chua bo sung DPA va audit right, nen van bi reject.',
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2026-04-02 18:00:00'
);

-- ----------------------------------------------------------
-- Signatures, reminders, email and audit
-- ----------------------------------------------------------
INSERT INTO signatures(user_id, signature_type, file_path, file_hash, is_default, created_at)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'Signature',
    'storage/signatures/admin_signature.txt',
    'seed-sign-admin',
    TRUE,
    '2026-01-08 09:00:00'
),
(
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'Stamp',
    'storage/signatures/legal_stamp.txt',
    'seed-sign-legal',
    TRUE,
    '2026-01-08 09:05:00'
);

INSERT INTO contract_signatures(
    contract_id,
    signature_id,
    signer_id,
    sign_order,
    status,
    signed_page,
    position_x,
    position_y,
    ip_address,
    device_info,
    signed_at,
    reject_reason
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    (SELECT signature_id FROM signatures WHERE user_id = (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local') AND signature_type = 'Stamp'),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    1,
    'Signed',
    6,
    412.5,
    698.2,
    '10.10.20.11',
    'Chrome on Windows 11',
    '2026-01-06 11:16:00',
    NULL
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    (SELECT signature_id FROM signatures WHERE user_id = (SELECT user_id FROM users WHERE email = 'admin@idp.local') AND signature_type = 'Signature'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    2,
    'Signed',
    6,
    512.0,
    702.8,
    '10.10.20.10',
    'Edge on Windows 11',
    '2026-01-06 14:10:00',
    NULL
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    (SELECT signature_id FROM signatures WHERE user_id = (SELECT user_id FROM users WHERE email = 'admin@idp.local') AND signature_type = 'Signature'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    1,
    'Pending',
    5,
    498.0,
    680.0,
    NULL,
    NULL,
    NULL,
    NULL
);

INSERT INTO email_logs(
    contract_id,
    sender_id,
    recipient_email,
    subject,
    email_type,
    provider_message_id,
    status,
    error_message,
    sent_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-001'),
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'contracts@alphamfg.vn',
    'Contract Approval Notice - IDP-2026-001',
    'APPROVAL_NOTICE',
    'sg-msg-0001',
    'Sent',
    NULL,
    '2026-01-06 14:20:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'leasing@saigonprime.vn',
    'Request for final lease clarification - IDP-2026-004',
    'NEGOTIATION',
    'sg-msg-0002',
    'Sent',
    NULL,
    '2026-07-29 17:20:00'
);

INSERT INTO reminder_logs(
    contract_id,
    reminder_type,
    days_before,
    status,
    sent_to,
    sent_at
)
VALUES
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    'LEASE_PAYMENT_FOLLOWUP',
    2,
    'Sent',
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    '2026-07-29 08:00:00'
),
(
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2025-006'),
    'CONTRACT_EXPIRY',
    30,
    'Sent',
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    '2026-05-31 09:00:00'
);

INSERT INTO audit_logs(user_id, action, table_name, record_id, description, ip_address, created_at)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'CREATE_CONTRACT',
    'contracts',
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004'),
    'Seed: Tao hop dong cho luong duyet va dashboard lease.',
    '10.10.20.10',
    '2026-07-01 14:00:00'
),
(
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'APPROVE_WORKFLOW',
    'approval_workflow',
    (SELECT workflow_id FROM approval_workflow WHERE contract_id = (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-004') AND step_number = 1),
    'Seed: Duyet buoc 1 cho hop dong thue van phong.',
    '10.10.20.12',
    '2026-07-02 10:30:00'
),
(
    (SELECT user_id FROM users WHERE email = 'tran.quang.minh@idp.local'),
    'REJECT_CONTRACT',
    'contracts',
    (SELECT contract_id FROM contracts WHERE contract_number = 'IDP-2026-005'),
    'Seed: Tu choi hop dong do rui ro bao mat va chi phi.',
    '10.10.20.13',
    '2026-04-02 09:45:00'
),
(
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    'RUN_AI_CHAT',
    'ai_chat_history',
    NULL,
    'Seed: Tao du lieu mau cho RAG va lich su hoi dap hop dong.',
    '10.10.20.14',
    '2026-07-28 09:10:00'
);

INSERT INTO api_keys(
    user_id,
    key_name,
    key_prefix,
    key_hash,
    scopes,
    expires_at,
    last_used_at,
    revoked,
    created_by,
    created_at
)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'Internal BI Integration',
    'idp_bi_a1',
    'seed-api-key-hash-admin-001',
    ARRAY['read:dashboard', 'read:contracts'],
    '2027-01-31 23:59:59',
    '2026-07-30 07:20:00',
    FALSE,
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-02-01 08:00:00'
),
(
    (SELECT user_id FROM users WHERE email = 'vo.minh.chau@idp.local'),
    'AI Sandbox Connector',
    'idp_ai_c9',
    'seed-api-key-hash-ai-001',
    ARRAY['read:contracts', 'write:processing'],
    '2026-12-31 23:59:59',
    '2026-07-29 18:00:00',
    FALSE,
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    '2026-03-01 10:00:00'
);

INSERT INTO refresh_tokens(
    user_id,
    token,
    expires_at,
    revoked,
    created_at,
    device_name,
    ip_address,
    created_by_ip
)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'seed_refresh_token_hash_admin_active_001',
    '2026-08-06 08:15:00',
    FALSE,
    '2026-07-30 08:15:00',
    'Admin-Workstation',
    '10.10.20.10',
    '10.10.20.10'
),
(
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'seed_refresh_token_hash_manager_active_001',
    '2026-08-05 17:10:00',
    FALSE,
    '2026-07-29 17:10:00',
    'Legal-Macbook',
    '10.10.20.12',
    '10.10.20.12'
),
(
    (SELECT user_id FROM users WHERE email = 'le.bao.ngoc@idp.local'),
    'seed_refresh_token_hash_employee_revoked_001',
    '2026-08-04 16:45:00',
    TRUE,
    '2026-07-28 16:45:00',
    'Legal-Laptop',
    '10.10.20.21',
    '10.10.20.21'
);

INSERT INTO password_reset_tokens(
    user_id,
    token,
    expires_at,
    used,
    created_at
)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'pham.duc.long@idp.local'),
    'seed-password-reset-employee-001',
    '2026-07-10 11:00:00',
    TRUE,
    '2026-07-10 10:30:00'
);

INSERT INTO email_verification_tokens(
    user_id,
    token,
    expires_at,
    verified_at,
    created_at
)
VALUES
(
    (SELECT user_id FROM users WHERE email = 'admin@idp.local'),
    'seed-email-verify-admin-001',
    '2026-01-06 08:00:00',
    '2026-01-05 08:10:00',
    '2026-01-05 08:00:00'
),
(
    (SELECT user_id FROM users WHERE email = 'nguyen.thu.ha@idp.local'),
    'seed-email-verify-manager-001',
    '2026-01-07 08:30:00',
    '2026-01-06 08:40:00',
    '2026-01-06 08:30:00'
);

COMMIT;
