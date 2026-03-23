-- Insert Admin User
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'ADMIN001',
    'System Administrator',
    'admin@claimx.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;