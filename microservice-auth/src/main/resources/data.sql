INSERT INTO auth_user (full_name, email, password_hash, role, is_active, created_at, updated_at)
VALUES ('Admin', 'admin@admin.com', '$2y$10$41MnYfanr5QfHcyTWxZaN.Z4jDA4V8JZAd9GLVTtoOd.U7/Xk1qmi', 'ADMIN', true, NOW(), NOW())
    ON CONFLICT (email) DO NOTHING;
