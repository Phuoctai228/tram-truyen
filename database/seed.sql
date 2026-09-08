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
(2, 'staff@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'System Staff', 5000, 'ACTIVE'),
(3, 'member@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'Normal Member', 1000, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- 3. Seed user_roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- Admin = ROLE_ADMIN
(2, 2), -- Staff = ROLE_STAFF
(3, 3)  -- Member = ROLE_MEMBER
ON CONFLICT DO NOTHING;

-- 4. Seed categories
INSERT INTO categories (id, name, description) VALUES
(1, 'Tiên Hiệp', 'Truyện về thế giới tu đạo, phi thăng tiên giới, trường sinh bất lão.'),
(2, 'Kiếm Hiệp', 'Truyện về thế giới võ lâm giang hồ, môn phái, hiệp khách, ân oán tình cừu.'),
(3, 'Huyền Huyễn', 'Truyện về thế giới ma pháp, dị giới, năng lực siêu nhiên phương Đông và phương Tây.'),
(4, 'Ngôn Tình', 'Truyện khai thác đề tài tình cảm lãng mạn, thanh xuân vườn trường, ngọt sủng.'),
(5, 'Đô Thị', 'Truyện bối cảnh thế giới hiện đại, thương trường, dị năng đô thị, đời sống thường nhật.')
ON CONFLICT (id) DO NOTHING;

-- 5. Seed system_settings (Cấu hình tỷ giá quy đổi Coin, Tỷ lệ ăn chia & Hạn mức rút tiền)
INSERT INTO system_settings (setting_key, setting_value, description) VALUES 
('vnd_to_coin_rate', '1', 'Tỷ lệ nạp tiền: 1 VNĐ = 1 Coin (ví dụ nạp 10.000 VNĐ nhận 10.000 Coin)'),
('coin_to_vnd_rate', '1', 'Tỷ lệ quy đổi từ Coin sang VNĐ (ví dụ 10.000 Coin = 10.000 VNĐ)'),
('author_revenue_share_percentage', '70', 'Tỷ lệ chia sẻ doanh thu Coin cho người đăng truyện khi độc giả mở khóa chương VIP (%)'),
('min_withdrawal_coin', '50000', 'Hạn mức rút tiền tối thiểu (Coin) cho mỗi yêu cầu rút tiền về ngân hàng')
ON CONFLICT (setting_key) DO UPDATE 
SET setting_value = EXCLUDED.setting_value, description = EXCLUDED.description;

-- 6. Reset sequence for serial IDs
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id), 1) FROM roles));
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('categories_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categories));
