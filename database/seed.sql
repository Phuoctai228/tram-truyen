-- Seed default roles
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_STAFF'),
('ROLE_MEMBER')
ON CONFLICT DO NOTHING;

-- Seed default users
-- Mật khẩu sử dụng BCrypt của chuỗi '123456'
-- $2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS
INSERT INTO users (id, email, password, full_name, status) VALUES 
(1, 'admin@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'System Admin', 'ACTIVE'),
(2, 'staff@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'System Staff', 'ACTIVE'),
(3, 'member@tramtruyen.com', '$2a$10$DowX8eL6tH23.x93z3fHGuuY3PofAUBhEToF6Z.K9o5eTz9uN73iS', 'Normal Member', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Seed user_roles
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- Admin = ROLE_ADMIN
(2, 2), -- Staff = ROLE_STAFF
(3, 3)  -- Member = ROLE_MEMBER
ON CONFLICT DO NOTHING;

-- Seed categories
INSERT INTO categories (name, description) VALUES
('Tiên Hiệp', 'Truyện về thế giới tu đạo, phi thăng tiên giới.'),
('Kiếm Hiệp', 'Truyện về võ lâm giang hồ, ân oán tình cừu.'),
('Huyền Huyễn', 'Truyện về thế giới ma pháp, kỳ ảo phương Tây hoặc phương Đông.'),
('Ngôn Tình', 'Truyện tình cảm lãng mạn.'),
('Đô Thị', 'Truyện bối cảnh hiện đại, đời sống thành phố.')
ON CONFLICT DO NOTHING;

-- Reset sequence for users so subsequent inserts don't fail
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
