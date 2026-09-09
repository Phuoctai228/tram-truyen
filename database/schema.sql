-- ================================================================================
-- DATABASE SCHEMA: TRẠM TRUYỆN (SWP391)
-- Mô hình Web đọc truyện thuần túy, nội dung do Admin/Staff phát hành
-- Chuẩn hóa 21 tables, đầy đủ PK, FK, quan hệ ràng buộc và chỉ mục tìm kiếm
-- ================================================================================

DROP TABLE IF EXISTS novel_reports CASCADE;
DROP TABLE IF EXISTS chapter_reports CASCADE;
DROP TABLE IF EXISTS comment_reports CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS reading_progress CASCADE;
DROP TABLE IF EXISTS user_read_chapters CASCADE;
DROP TABLE IF EXISTS bookshelves CASCADE;
DROP TABLE IF EXISTS unlocked_chapters CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS system_settings CASCADE;
DROP TABLE IF EXISTS novel_ratings CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS novel_categories CASCADE;
DROP TABLE IF EXISTS novels CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- 1. ROLES (Phân quyền: ROLE_ADMIN, ROLE_STAFF, ROLE_MEMBER)
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- 2. USERS (Tài khoản người dùng & Số dư ví Coin M3-F01, M3-F03, M3-F04, M3-F08, M3-F09, M3-F11)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255), -- Có thể NULL nếu người dùng đăng nhập bằng Google OAuth2
    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(255),
    wallet_balance INT DEFAULT 0, -- Số dư Coin nạp (dùng mở khóa chương VIP, không rút tiền, không hoàn tiền)
    auth_provider VARCHAR(50) DEFAULT 'LOCAL', -- LOCAL (Form email/password), GOOGLE (OAuth2 Google M3-F04)
    provider_id VARCHAR(255), -- ID định danh từ Google nếu đăng nhập OAuth2
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, PENDING_VERIFICATION (M3-F02), BANNED (M3-F13: Ban/Enable User)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. USER_ROLES (M-N: Quan hệ người dùng và vai trò M3-F12: Change User Role)
CREATE TABLE user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. PASSWORD_RESET_TOKENS (Mã token đặt lại mật khẩu qua Email M3-F06)
CREATE TABLE password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. EMAIL_VERIFICATION_TOKENS (Mã OTP kích hoạt tài khoản qua Email M3-F02)
CREATE TABLE email_verification_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    otp_code VARCHAR(10) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. CATEGORIES (Danh mục thể loại truyện M4-F01, M4-F02, M4-F03, M4-F04)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- 7. NOVELS (Thông tin bộ truyện - do Staff/Admin quản lý đăng tải M1-F01 -> M1-F06)
CREATE TABLE novels (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL, -- Tác giả gốc của tác phẩm
    summary TEXT,
    cover_url VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ONGOING', -- ONGOING (Đang ra), COMPLETED (Hoàn thành), ON_HOLD (Tạm ngưng), ARCHIVED (Tạm ẩn M1-F03)
    is_deleted BOOLEAN DEFAULT FALSE, -- Cờ xóa mềm bộ truyện (M1-F04: Delete Novel)
    uploader_id INT REFERENCES users(id) ON DELETE SET NULL, -- Tài khoản Staff/Admin đăng tải tác phẩm
    views INT DEFAULT 0, -- Tổng lượt xem tích lũy (cộng dồn từ các chương)
    average_rating NUMERIC(3, 2) DEFAULT 0.0, -- Điểm đánh giá trung bình từ 1.00 đến 5.00 (M4-F10)
    rating_count INT DEFAULT 0, -- Tổng số lượt độc giả đã đánh giá sao (M4-F10)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. NOVEL_CATEGORIES (M-N: Phân loại đa thể loại cho truyện M4-F05)
CREATE TABLE novel_categories (
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    category_id INT REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (novel_id, category_id)
);

-- 9. NOVEL_RATINGS (Hệ thống đánh giá sao & nhận xét truyện M4-F10)
CREATE TABLE novel_ratings (
    id SERIAL PRIMARY KEY,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5), -- Điểm chấm từ 1 đến 5 sao
    review_text TEXT, -- Nhận xét cảm nhận của độc giả
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (novel_id, user_id) -- Mỗi user chỉ được chấm điểm 1 lần cho 1 truyện (có thể sửa đổi)
);

-- 10. CHAPTERS (Nội dung chương truyện & Cài đặt VIP thu phí Coin M1-F07 -> M1-F11)
CREATE TABLE chapters (
    id SERIAL PRIMARY KEY,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    chapter_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    price INT DEFAULT 0, -- Giá mở khóa bằng Coin (0 = miễn phí)
    auto_unlock_at TIMESTAMP, -- Hẹn giờ tự động mở khóa miễn phí sau X ngày (tùy chọn)
    views INT DEFAULT 0, -- Lượt xem của chương
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT (Bản nháp / Tạm ẩn M1-F09), PUBLISHED_REGULAR (Miễn phí), PUBLISHED_VIP (Thu phí Coin)
    is_deleted BOOLEAN DEFAULT FALSE, -- Cờ xóa mềm chương truyện (M1-F10: Delete Chapter)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (novel_id, chapter_number)
);

-- 11. BOOKSHELVES (Tủ sách cá nhân của độc giả M2-F07, M2-F08, M2-F09)
CREATE TABLE bookshelves (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, novel_id)
);

-- 12. USER_READ_CHAPTERS (Lịch sử đánh dấu chương đã đọc để tính tiến độ % M2-F04)
CREATE TABLE user_read_chapters (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, chapter_id)
);

-- 13. READING_PROGRESS (Bookmark vị trí chương đọc gần nhất để bấm 'Đọc tiếp' M2-F06)
CREATE TABLE reading_progress (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    last_read_chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, novel_id)
);

-- 14. UNLOCKED_CHAPTERS (Danh sách chương VIP độc giả đã mở khóa bằng Coin M2-F10, M2-F11)
CREATE TABLE unlocked_chapters (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE,
    price_paid INT DEFAULT 0,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, chapter_id)
);

-- 15. COMMENTS (Bình luận cảm nhận truyện/chương M5-F01, M5-F02)
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE, -- Có thể NULL nếu bình luận chung cho cả bộ truyện
    content TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'VISIBLE', -- VISIBLE, HIDDEN, DELETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 16. COMMENT_REPORTS (Báo cáo bình luận độc hại / vi phạm chính sách M5-F03, M5-F04)
CREATE TABLE comment_reports (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    comment_id INT REFERENCES comments(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'RECEIVED', -- RECEIVED (Đã nhận), PROCESSED (Đã xử lý), REJECTED (Từ chối)
    admin_note TEXT, -- Ghi chú lý do xử lý hoặc từ chối của Admin/Staff
    resolved_by INT REFERENCES users(id) ON DELETE SET NULL, -- Admin/Staff trực tiếp xử lý
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- 17. CONTENT_REPORTS (Báo cáo vi phạm / lỗi cấp bộ truyện hoặc chương M5-F04, M5-F05)
CREATE TABLE content_reports (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT NOT NULL REFERENCES novels(id) ON DELETE CASCADE,
    chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE, -- NULL nếu báo cáo toàn bộ truyện
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'RECEIVED', -- RECEIVED (Đã nhận), PROCESSED (Đã xử lý), REJECTED (Từ chối)
    admin_note TEXT, -- Ghi chú lý do xử lý hoặc từ chối của Admin/Staff
    resolved_by INT REFERENCES users(id) ON DELETE SET NULL, -- Admin/Staff trực tiếp xử lý
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- 18. NOTIFICATIONS (Hòm thư thông báo hệ thống / cá nhân M5-F06, M5-F07)
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE, -- NULL nếu gửi toàn hệ thống (dear all)
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 19. TRANSACTIONS (Lịch sử biến động ví: Nạp tiền VNPay M3-F10, Tiêu Coin mở VIP M2-F10 & Đối soát M5-F08)
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    amount INT NOT NULL, -- Số Coin biến động (+ nạp tiền, - mở khóa VIP)
    type VARCHAR(50) NOT NULL, -- DEPOSIT (Nạp tiền vào ví qua VNPay), UNLOCK_CHAPTER (Dùng Coin mở khóa VIP)
    payment_transaction_id VARCHAR(100), -- Mã giao dịch phản hồi từ cổng VNPay (nếu nạp tiền)
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 20. SYSTEM_SETTINGS (Cấu hình hệ thống: Tỷ giá Coin, Email hỗ trợ, Chính sách M5-F09)
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
