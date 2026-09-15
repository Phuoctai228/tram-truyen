-- ================================================================================
-- DATABASE SEED DATA: TRẠM TRUYỆN (SWP391)
-- ================================================================================

-- 1. Seed default roles (Hệ thống chuẩn: ADMIN, STAFF, MEMBER)
INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_STAFF'),
(3, 'ROLE_MEMBER')
ON CONFLICT (id) DO NOTHING;

-- 2. Seed default users
-- Mật khẩu sử dụng mã hóa BCrypt của chuỗi '123456'
-- $2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS
INSERT INTO users (id, email, password, full_name, wallet_balance, status) VALUES 
(1, 'admin@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'System Admin', 10000, 'ACTIVE'),
(2, 'staff@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'Content Staff', 5000, 'ACTIVE'),
(3, 'member@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'Standard Reader', 1000, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 3. Seed user_roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- Admin = ROLE_ADMIN
(2, 2), -- Staff = ROLE_STAFF
(3, 3)  -- Member = ROLE_MEMBER
ON CONFLICT DO NOTHING;

-- 4. Seed categories
INSERT INTO categories (id, name, slug, description) VALUES
(1, 'Tiên Hiệp', 'tien-hiep', 'Truyện về thế giới tu đạo, phi thăng tiên giới, trường sinh bất lão.'),
(2, 'Kiếm Hiệp', 'kiem-hiep', 'Truyện về thế giới võ lâm giang hồ, môn phái, hiệp khách, ân oán tình cừu.'),
(3, 'Huyền Huyễn', 'huyen-huyen', 'Truyện về thế giới ma pháp, dị giới, năng lực siêu nhiên phương Đông và phương Tây.'),
(4, 'Ngôn Tình', 'ngon-tinh', 'Truyện khai thác đề tài tình cảm lãng mạn, thanh xuân vườn trường, ngọt sủng.'),
(5, 'Đô Thị', 'do-thi', 'Truyện bối cảnh thế giới hiện đại, thương trường, dị năng đô thị, đời sống thường nhật.')
ON CONFLICT (id) DO NOTHING;

-- 5. Seed system_settings (Cấu hình tỷ giá nạp Coin, thông tin liên hệ và chính sách bản quyền)
INSERT INTO system_settings (setting_key, setting_value, description) VALUES 
('vnd_to_coin_rate', '1', 'Tỷ giá nạp tiền: 1 VNĐ = 1 Coin (ví dụ nạp 10.000 VNĐ nhận 10.000 Coin - không hỗ trợ hoàn tiền)'),
('support_email', 'support@tramtruyen.com', 'Email tiếp nhận khiếu nại bản quyền và hỗ trợ độc giả'),
('contact_phone', '1900-1234', 'Hotline hỗ trợ kỹ thuật và chăm sóc khách hàng 24/7'),
('notice_takedown_policy', 'Cam kết thẩm tra và tạm ẩn tác phẩm vi phạm bản quyền trong vòng 24h kể từ khi nhận được khiếu nại hợp lệ')
ON CONFLICT (setting_key) DO UPDATE 
SET setting_value = EXCLUDED.setting_value, description = EXCLUDED.description;

-- 6. Reset sequence for serial IDs
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 1) FROM roles));
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('categories_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categories));
