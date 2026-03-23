-- Insert Admin User
-- admin@claimx.com, admin@123
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'ADMIN001',
    'System Administrator',
    'admin@claimx.com',
    '$2a$10$zItWxpsQVlb91tl4.y1ZdOtB5Fz016jqpcGXlLQVJtgRtte1MNr52',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Insert User finance
-- akash.finance@claimx.com, akash@123
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'FIN001',
    'Akash',
    'akash.finance@claimx.com',
    '$2a$10$ofbjY9J86..6ItNVP1Xn6O9G8fYR8uyA207SsrvD55lFVjPny6.Ze',
    'FINANCE',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Insert Manager
-- venkat.manager@claimx.com, venkat@123
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'MGR001',
    'Venkat',
    'venkat.manager@claimx.com',
    '$2a$10$.9UP0kiqsOHCitGF6v11I.y8aM8PDt/EowLF8MVTyAG2ZPRwgWCaO',
    'MANAGER',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Insert manager
-- mohan.manager@claimx.com, mohan@123
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'MGR002',
    'Mohan',
    'mohan.manager@claimx.com',
    '$2a$10$e5.zClR2.DEo9qtp8s8mSeOEOGnHfX64CoqIsE3Tkv2VkFdOKpdBO',
    'MANAGER',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;


--Insert employee 1
--prasid.employee@claimx.com, prasid@123
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'EMP001',
    'Prasid',
    'prasid.employee@claimx.com',
    '$2a$10$vdxATMainDFdz6ag213o2.Np.RTiBemDiMvDntLMQ/YeWRalmbZoS',
    'EMPLOYEE',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Insert employee 2
-- prajwal.employee@claimx.com, prajwal@123
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'EMP002',
    'Prajwal',
    'prajwal.employee@claimx.com',
    '$2a$10$yLNJUv3l0OwpY38OtbUttujd85YWQ0P8pxWZvbirSxX9l7oDuhn3u',
    'EMPLOYEE',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- assign employee manager
-- Assign EMP001(Prasid) to MGR001(Venkat)
INSERT INTO employee_manager (employee_id, manager_id)
SELECT u1.id, u2.id
FROM users u1
JOIN users u2 ON u2.employee_code = 'MGR001'
WHERE u1.employee_code = 'EMP001'
AND NOT EXISTS (
    SELECT 1
    FROM employee_manager em
    WHERE em.employee_id = u1.id
);

-- assign employee manager
-- Assign EMP002(Prajwal) to MGR001(Venkat)
INSERT INTO employee_manager (employee_id, manager_id)
SELECT u1.id, u2.id
FROM users u1
JOIN users u2 ON u2.employee_code = 'MGR001'
WHERE u1.employee_code = 'EMP002'
AND NOT EXISTS (
    SELECT 1
    FROM employee_manager em
    WHERE em.employee_id = u1.id
);

-- assign employee manager
-- Assign MGR001(Venkat) to MGR002(Mohan)
INSERT INTO employee_manager (employee_id, manager_id)
SELECT u1.id, u2.id
FROM users u1
JOIN users u2 ON u2.employee_code = 'MGR002'
WHERE u1.employee_code = 'MGR001'
AND NOT EXISTS (
    SELECT 1
    FROM employee_manager em
    WHERE em.employee_id = u1.id
);


-- Insert finance 1
-- manu.employee@claimx.com, manu@123
INSERT INTO users (employee_code, name, email, password, role, is_active, created_at)
VALUES (
    'FIN002',
    'Manu',
    'manu.finance@claimx.com',
    '$2a$10$Hs/UdSPg.HFH8Fs34twWZOPFplxC0iC.ULtKj1tAkeGDqaiBkVdya',
    'FINANCE',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;