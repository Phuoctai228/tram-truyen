-- ================================================================================
-- DATABASE SEED DATA: TRẠM TRUYỆN (SWP391)
-- ================================================================================

-- 1. Seed default roles (Bỏ id, bắt trùng theo name)
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_STAFF'),
('ROLE_MEMBER')
ON CONFLICT (name) DO NOTHING;

-- 2. Seed default users (Bỏ id, bắt trùng theo email)
-- Mật khẩu sử dụng mã hóa BCrypt của chuỗi '12345678'
-- $2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS
INSERT INTO users (email, password, full_name, wallet_balance, status) VALUES 
('admin@tramtruyen.com', '$2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS', 'System Admin', 10000, 'ACTIVE'),
('staff@tramtruyen.com', '$2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS', 'Content Staff', 5000, 'ACTIVE'),
('member@tramtruyen.com', '$2a$10$LmC09bKcSP1wc7N7eArmUe8kOsjWYqjPYoTeIMJseMNNoRpPiGwSS', 'Standard Reader', 1000, 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- 3. Seed user_roles (Map động theo email và role name, không set cứng id)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (VALUES 
    ('admin@tramtruyen.com', 'ROLE_ADMIN'),
    ('staff@tramtruyen.com', 'ROLE_STAFF'),
    ('member@tramtruyen.com', 'ROLE_MEMBER')
) AS mapping(email, role_name)
JOIN users u ON u.email = mapping.email
JOIN roles r ON r.name = mapping.role_name
ON CONFLICT DO NOTHING;

-- 4. Seed categories (Bỏ id, bắt trùng theo slug)
INSERT INTO categories (name, slug, description) VALUES
('Tiên Hiệp', 'tien-hiep', 'Truyện về thế giới tu đạo, phi thăng tiên giới, trường sinh bất lão.'),
('Kiếm Hiệp', 'kiem-hiep', 'Truyện về thế giới võ lâm giang hồ, môn phái, hiệp khách, ân oán tình cừu.'),
('Huyền Huyễn', 'huyen-huyen', 'Truyện về thế giới ma pháp, dị giới, năng lực siêu nhiên phương Đông và phương Tây.'),
('Ngôn Tình', 'ngon-tinh', 'Truyện khai thác đề tài tình cảm lãng mạn, thanh xuân vườn trường, ngọt sủng.'),
('Đô Thị', 'do-thi', 'Truyện bối cảnh thế giới hiện đại, thương trường, dị năng đô thị, đời sống thường nhật.')
ON CONFLICT (slug) DO NOTHING;

-- 5. Seed system_settings (Cấu hình tỷ giá nạp Coin, thông tin liên hệ và chính sách bản quyền)
INSERT INTO system_settings (setting_key, setting_value, description) VALUES 
('vnd_to_coin_rate', '1', 'Tỷ giá nạp tiền: 1 VNĐ = 1 Coin (ví dụ nạp 10.000 VNĐ nhận 10.000 Coin - không hỗ trợ hoàn tiền)'),
('support_email', 'support@tramtruyen.com', 'Email tiếp nhận khiếu nại bản quyền và hỗ giả'),
('contact_phone', '1900-1234', 'Hotline hỗ trợ kỹ thuật và chăm sóc khách hàng 24/7'),
('notice_takedown_policy', 'Cam kết thẩm tra và tạm ẩn tác phẩm vi phạm bản quyền trong vòng 24h kể từ khi nhận được khiếu nại hợp lệ')
ON CONFLICT (setting_key) DO UPDATE 
SET setting_value = EXCLUDED.setting_value, description = EXCLUDED.description;

-- 6. Tự động đồng bộ tất cả sequences về MAX(id) nếu có bất kỳ dữ liệu nào được chèn trước đó
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT 
            c.relname AS seq_name,
            t.relname AS tab_name,
            a.attname AS col_name
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_depend d ON d.objid = c.oid AND d.deptype = 'a'
        JOIN pg_class t ON t.oid = d.refobjid
        JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = d.refobjsubid
        WHERE c.relkind = 'S' AND n.nspname = 'public'
    LOOP
        EXECUTE format(
            'SELECT CASE 
                WHEN (SELECT COUNT(*) FROM %I) > 0 
                THEN setval(%L, (SELECT MAX(%I) FROM %I), true)
                ELSE setval(%L, 1, false)
             END',
            r.tab_name, r.seq_name, r.col_name, r.tab_name, r.seq_name
        );
    END LOOP;
END $$;
