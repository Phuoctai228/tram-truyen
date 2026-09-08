-- ================================================================================
-- DATABASE SCHEMA: TRẠM TRUYỆN (SWP391)
-- ================================================================================

DROP TABLE IF EXISTS withdrawal_requests CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS comment_reports CASCADE;
DROP TABLE IF EXISTS chapter_reports CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS user_read_chapters CASCADE;
DROP TABLE IF EXISTS reading_progress CASCADE;
DROP TABLE IF EXISTS bookshelves CASCADE;
DROP TABLE IF EXISTS unlocked_chapters CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS system_settings CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS novel_categories CASCADE;
DROP TABLE IF EXISTS novels CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- 1. ROLES
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- 2. USERS
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(255),
    wallet_balance INT DEFAULT 0, -- Số dư Coin trong ví
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, BANNED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. USER_ROLES (M-N)
CREATE TABLE user_roles (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    role_id INT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. PASSWORD_RESET_TOKENS (Hỗ trợ quên mật khẩu M3-F04)
CREATE TABLE password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. CATEGORIES
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- 6. NOVELS
CREATE TABLE novels (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    summary TEXT,
    cover_url VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ONGOING', -- ONGOING, COMPLETED, PAUSED
    uploader_id INT REFERENCES users(id) ON DELETE SET NULL, -- Bất kỳ Member nào cũng có thể là người đăng truyện
    approval_status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, ARCHIVED
    views INT DEFAULT 0, -- Tổng lượt xem của truyện (cộng dồn từ các chương)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. NOVEL_CATEGORIES (M-N)
CREATE TABLE novel_categories (
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    category_id INT REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (novel_id, category_id)
);

-- 8. CHAPTERS
CREATE TABLE chapters (
    id SERIAL PRIMARY KEY,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    chapter_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    price INT DEFAULT 0, -- Giá mở khóa bằng Coin (0 = miễn phí)
    auto_unlock_at TIMESTAMP, -- Hẹn giờ tự động mở khóa VIP sau X ngày
    views INT DEFAULT 0, -- Lượt xem của chương
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, PUBLISHED_REGULAR, PUBLISHED_VIP
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (novel_id, chapter_number)
);

-- 9. BOOKSHELVES (Tủ sách cá nhân)
CREATE TABLE bookshelves (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, novel_id)
);

-- 10. USER_READ_CHAPTERS (Nhận diện chương đã đọc - Click 1 lần để đánh dấu, dùng tính tiến độ %)
CREATE TABLE user_read_chapters (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, chapter_id)
);

-- 11. READING_PROGRESS (Bookmark vị trí chương gần nhất để nút 'Đọc tiếp' mapping đến đúng chương đó)
CREATE TABLE reading_progress (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    novel_id INT REFERENCES novels(id) ON DELETE CASCADE,
    last_read_chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, novel_id)
);

-- 12. COMMENTS
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

-- 13. CHAPTER_REPORTS (Báo cáo lỗi chương truyện)
CREATE TABLE chapter_reports (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, RESOLVED, REJECTED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- 14. COMMENT_REPORTS (Báo cáo bình luận vi phạm)
CREATE TABLE comment_reports (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    comment_id INT REFERENCES comments(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, RESOLVED, DISMISSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- 15. UNLOCKED_CHAPTERS (Danh sách chương VIP người dùng đã mở khóa bằng Coin)
CREATE TABLE unlocked_chapters (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    chapter_id INT REFERENCES chapters(id) ON DELETE CASCADE,
    price_paid INT DEFAULT 0,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, chapter_id)
);

-- 16. TRANSACTIONS (Lịch sử giao dịch ví: Nạp tiền, Mở khóa VIP, Nhận doanh thu, Rút tiền)
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    amount INT NOT NULL, -- Số Coin biến động (+/-)
    type VARCHAR(50) NOT NULL, -- DEPOSIT (Nạp tiền), UNLOCK_CHAPTER (Mở khóa VIP), AUTHOR_RECEIVE (Tác giả nhận Coin), WITHDRAWAL (Rút tiền về ngân hàng)
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 17. WITHDRAWAL_REQUESTS (Yêu cầu rút tiền tác quyền về tài khoản ngân hàng)
CREATE TABLE withdrawal_requests (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    amount_coin INT NOT NULL, -- Số Coin muốn rút
    amount_vnd INT NOT NULL, -- Số tiền thực tế quy đổi tương ứng (VNĐ)
    bank_name VARCHAR(100) NOT NULL, -- Tên ngân hàng (Vietcombank, MBBank, v.v.)
    bank_account_number VARCHAR(50) NOT NULL, -- Số tài khoản ngân hàng
    bank_account_name VARCHAR(100) NOT NULL, -- Tên chủ tài khoản
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING (Chờ duyệt), APPROVED (Đã chuyển khoản), REJECTED (Từ chối)
    admin_note TEXT, -- Ghi chú của Admin khi duyệt/từ chối
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- 18. SYSTEM_SETTINGS (Cấu hình hệ thống: Tỷ giá Coin, % Ăn chia doanh thu, Hạn mức rút)
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
