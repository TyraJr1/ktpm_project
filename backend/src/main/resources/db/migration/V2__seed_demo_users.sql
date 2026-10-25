-- Development/demo users for authentication verification.
-- Password values are BCrypt hashes; no plaintext password is stored.
INSERT INTO users (username, password_hash, full_name, `role`, status, created_at)
SELECT 'admin01', '$2a$12$8tjmihdOyEQ.fC/7kIZqeepsTbJnX3h8NGGoOvYzlJE/w0QnCf7Q6',
       'Quản trị viên Demo', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin01');

INSERT INTO users (username, password_hash, full_name, `role`, status, created_at)
SELECT 'sales01', '$2a$12$8tjmihdOyEQ.fC/7kIZqeepsTbJnX3h8NGGoOvYzlJE/w0QnCf7Q6',
       'Nhân viên bán hàng Demo', 'SALES_STAFF', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'sales01');

INSERT INTO users (username, password_hash, full_name, `role`, status, created_at)
SELECT 'warehouse01', '$2a$12$8tjmihdOyEQ.fC/7kIZqeepsTbJnX3h8NGGoOvYzlJE/w0QnCf7Q6',
       'Nhân viên kho Demo', 'WAREHOUSE_STAFF', 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'warehouse01');
