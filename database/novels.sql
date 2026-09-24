-- ================================================================================
-- DATABASE SCHEMA: TRẠM TRUYỆN (SWP391)
-- Mô hình Web đọc truyện thuần túy, nội dung do Admin/Staff phát hành
-- Chuẩn hóa 25 tables, đầy đủ PK, FK, quan hệ ràng buộc và chỉ mục tìm kiếm
-- ================================================================================

DROP TABLE IF EXISTS user_reader_themes CASCADE;
DROP TABLE IF EXISTS user_daily_checkins CASCADE;
DROP TABLE IF EXISTS web_settings CASCADE;
DROP TABLE IF EXISTS content_reports CASCADE;
DROP TABLE IF EXISTS comment_reports CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS user_chapter_activities CASCADE;
DROP TABLE IF EXISTS bookshelves CASCADE;
DROP TABLE IF EXISTS system_audit_logs CASCADE;
DROP TABLE IF EXISTS user_login_logs CASCADE;
DROP TABLE IF EXISTS payment_logs CASCADE;
DROP TABLE IF EXISTS topup_orders CASCADE;
DROP TABLE IF EXISTS coin_packages CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS system_settings CASCADE;
DROP TABLE IF EXISTS novel_ratings CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS novel_categories CASCADE;
DROP TABLE IF EXISTS novels CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;

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
    wallet_balance INT DEFAULT 0, -- Số dư Coin nạp (dùng mở khóa chương VIP, không hoàn tiền)
    auth_provider VARCHAR(50) DEFAULT 'LOCAL', -- LOCAL (Form email/password), GOOGLE (OAuth2 Google M3-F04)
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

-- 4. EMAIL_VERIFICATION_TOKENS (Mã OTP kích hoạt tài khoản qua Email M3-F02)
CREATE TABLE email_verification_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    otp_code VARCHAR(10) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. CATEGORIES (Danh mục thể loại truyện M4-F01, M4-F02, M4-F03, M4-F04)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- 6. NOVELS (Thông tin bộ truyện - do Staff/Admin quản lý đăng tải M1-F01 -> M1-F06)
CREATE TABLE novels (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL, -- Tác giả gốc của tác phẩm
    summary TEXT, --descriptions
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

-- 7. NOVEL_CATEGORIES (M-N: Phân loại đa thể loại cho truyện M4-F05)
CREATE TABLE novel_categories (
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    category_id INT REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (novel_id, category_id)
);

-- 8. NOVEL_RATINGS (Hệ thống đánh giá sao & nhận xét truyện M4-F10)
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

-- 9. CHAPTERS (Nội dung chương truyện & Cài đặt VIP thu phí Coin M1-F07 -> M1-F11)
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

-- 10. BOOKSHELVES (Tủ sách cá nhân của độc giả M2-F07, M2-F08, M2-F09)
CREATE TABLE bookshelves (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, novel_id)
);

-- 11. USER_CHAPTER_ACTIVITIES (Gộp Lịch sử đọc, Mở khóa VIP và Tiến độ đọc)
CREATE TABLE user_chapter_activities (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    chapter_id INT NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    is_read BOOLEAN DEFAULT FALSE, -- Đánh dấu đã đọc chương này
    read_at TIMESTAMP, -- Thời gian đọc (có thể dùng để suy ra reading_progress mới nhất bằng truy vấn)
    is_unlocked BOOLEAN DEFAULT FALSE, -- Đánh dấu đã mở khóa VIP
    price_paid INT DEFAULT 0, -- Giá Coin đã trả để mở khóa
    unlocked_at TIMESTAMP,
    UNIQUE (user_id, chapter_id)
);

-- 12. COMMENTS (Bình luận cảm nhận truyện/chương M5-F01, M5-F02)
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    -- novel_id INT REFERENCES novels(id) ON DELETE CASCADE, -- Đã bỏ theo yêu cầu, truy xuất qua chapter_id.
    chapter_id INT NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'VISIBLE', -- VISIBLE, HIDDEN, DELETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. COMMENT_REPORTS (Báo cáo bình luận độc hại / vi phạm chính sách M5-F03, M5-F04)
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

-- 14. CONTENT_REPORTS (Báo cáo vi phạm / lỗi cấp chương M5-F04, M5-F05)
CREATE TABLE content_reports (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    -- novel_id INT NOT NULL REFERENCES novels(id) ON DELETE CASCADE, -- Đã bỏ, chỉ link đến chapter
    chapter_id INT NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'RECEIVED', -- RECEIVED (Đã nhận), PROCESSED (Đã xử lý), REJECTED (Từ chối)
    admin_note TEXT, -- Ghi chú lý do xử lý hoặc từ chối của Admin/Staff
    resolved_by INT REFERENCES users(id) ON DELETE SET NULL, -- Admin/Staff trực tiếp xử lý
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- 15. NOTIFICATIONS (Hòm thư thông báo hệ thống / cá nhân M5-F06, M5-F07)
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE, -- NULL nếu gửi toàn hệ thống (dear all)
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 16. COIN_PACKAGES (Danh sách các gói nạp Coin M3-F10)
CREATE TABLE coin_packages (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price INT NOT NULL, -- Số tiền VNĐ
    coin_amount INT NOT NULL, -- Số Coin nhận được
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 17. TOPUP_ORDERS (Đổi tên từ deposit_orders để không nhầm lẫn với rút tiền)
CREATE TABLE topup_orders (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    package_id INT REFERENCES coin_packages(id) ON DELETE SET NULL,
    order_code VARCHAR(50) UNIQUE NOT NULL, -- VD: TT123456 (Dùng cho nội dung chuyển khoản)
    amount_vnd INT NOT NULL, -- Số tiền VNĐ cần thanh toán
    coin_received INT NOT NULL, -- Số Coin sẽ được cộng (bao gồm cả khuyến mãi)
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING (Chờ CK), SUCCESS (Đã CK), FAILED (Thất bại/Quá hạn)
    payment_method VARCHAR(50) DEFAULT 'BANK_TRANSFER', -- BANK_TRANSFER, VNPAY, MOMO
    bank_transaction_code VARCHAR(100), -- Mã giao dịch thật sự của Ngân Hàng (Mã đối soát)
    paid_at TIMESTAMP, -- Thời gian thực tế ngân hàng ghi nhận có tiền
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 18. PAYMENT_LOGS (Truy vết kỹ thuật IPN/Webhook từ Ngân hàng / VNPay)
CREATE TABLE payment_logs (
    id SERIAL PRIMARY KEY,
    order_code VARCHAR(50) REFERENCES topup_orders(order_code) ON DELETE SET NULL, -- Ràng buộc khóa ngoại với đơn hàng
    raw_payload TEXT, -- Lưu toàn bộ dữ liệu trả về từ Bank/VNPay (JSON string)
    ip_address VARCHAR(50),
    status VARCHAR(50), -- SUCCESS (xử lý thành công), UNMATCHED (Không tìm thấy đơn), ERROR (Lỗi code)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 19. TRANSACTIONS (Sổ cái ví: Ghi nhận biến động số dư thực tế, M2-F10)
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    amount INT NOT NULL, -- SỐ COIN (+ hoặc -)
    balance_after INT NOT NULL, -- Lưu lại số dư ví sau khi biến động để truy vết dễ hơn
    type VARCHAR(50) NOT NULL, -- TOPUP_COIN, UNLOCK_CHAPTER, ADMIN_ADJUSTMENT, GIFT_COIN
    reference_id INT, -- ID tham chiếu (Ví dụ: ID của topup_orders hoặc ID của chapters)
    description TEXT,
    created_by INT REFERENCES users(id) ON DELETE SET NULL, -- Lưu ID Admin/Staff nếu thao tác cộng/trừ thủ công
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 20. SYSTEM_SETTINGS (Cấu hình hệ thống chung: Tỷ giá Coin, Email hỗ trợ, Chính sách)
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL, 
    description TEXT,
    updated_by INT REFERENCES users(id) ON DELETE SET NULL, 
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 21. WEB_SETTINGS (Thay thế cho bảng Themes - Cấu hình giao diện web)
CREATE TABLE web_settings (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- Tên giao diện (VD: Giao diện Tết 2026)
    folder_path VARCHAR(255) UNIQUE NOT NULL, 
    thumbnail_url VARCHAR(255), 
    is_active BOOLEAN DEFAULT FALSE, 
    uploaded_by INT REFERENCES users(id) ON DELETE SET NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 22. SYSTEM_AUDIT_LOGS (Nhật ký hoạt động của Admin/Staff chống lạm quyền)
CREATE TABLE system_audit_logs (
    id SERIAL PRIMARY KEY,
    admin_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- Ai làm
    action_type VARCHAR(100) NOT NULL, -- Làm hành động gì (VD: DELETE_NOVEL, BAN_USER, ADD_COIN)
    target_entity VARCHAR(100), -- Tác động lên bảng nào (VD: NOVELS, USERS)
    target_id INT, -- Tác động lên ID nào
    old_value TEXT, -- Dữ liệu trước khi sửa (JSON format)
    new_value TEXT, -- Dữ liệu sau khi sửa (JSON format)
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 23. USER_LOGIN_LOGS (Nhật ký đăng nhập của Member, phục vụ xử lý tranh chấp)
CREATE TABLE user_login_logs (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    ip_address VARCHAR(50),
    user_agent TEXT, -- Thiết bị, Trình duyệt
    status VARCHAR(50) DEFAULT 'SUCCESS', -- SUCCESS, FAILED_PASSWORD, LOCKED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 24. USER_DAILY_CHECKINS (Lịch sử điểm danh hàng ngày của Member - M2-F11)
CREATE TABLE user_daily_checkins (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    checkin_date DATE NOT NULL DEFAULT CURRENT_DATE, -- Ngày điểm danh
    reward_coin INT DEFAULT 0, -- Số Coin thưởng nhận được
    streak_count INT DEFAULT 1, -- Chuỗi ngày điểm danh liên tiếp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, checkin_date) -- Đảm bảo 1 user chỉ điểm danh 1 lần/ngày
);

-- 25. USER_READER_THEMES (Đổi tên từ user_reader_settings - Cài đặt cá nhân hóa)
CREATE TABLE user_reader_themes (
    user_id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    font_family VARCHAR(50) DEFAULT 'Inter', -- Font chữ
    font_size INT DEFAULT 18, -- Cỡ chữ
    theme_mode VARCHAR(20) DEFAULT 'LIGHT', -- Màu nền (LIGHT, DARK, SEPIA)
    audio_voice VARCHAR(50) DEFAULT 'DEFAULT', -- Giọng đọc (VD: Giọng Nam/Nữ)
    audio_speed DECIMAL(3,1) DEFAULT 1.0, -- Tốc độ đọc (1.0x, 1.25x...)
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
