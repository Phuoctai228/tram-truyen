# Phân tích Luồng Dữ liệu và Thao tác Database (28 Bảng)
Dựa theo file schema `novels.sql` của dự án Trạm Truyện.

Báo cáo này phân loại 28 bảng trong CSDL theo các nhóm nghiệp vụ và giải thích rõ: **Bảng đó chứa dữ liệu gì**, **Khi nào hệ thống tự động cập nhật**, và **Khi nào User thao tác nghiệp vụ**.

---

## 1. Nhóm Người Dùng, Xác Thực & Phân Quyền (Auth & Security)

### 1. `roles` (Vai trò)
- **Chứa dữ liệu gì:** Định nghĩa cứng các quyền (ADMIN, STAFF, MEMBER).
- **Cập nhật tự động:** KHÔNG. 
- **User thao tác:** Hầu như tĩnh. Chỉ Admin (hoặc Script DB) Insert thủ công lúc khởi tạo dự án.

### 2. `users` (Tài khoản)
- **Chứa dữ liệu gì:** Thông tin cá nhân, mật khẩu mã hóa, số dư ví (`wallet_balance`), trạng thái hoạt động.
- **Cập nhật tự động:** CÓ (Trường `wallet_balance` tự động thay đổi do trigger/logic từ Backend khi có giao dịch nạp tiền, mở khóa chương, nhận thưởng điểm danh).
- **User thao tác:** Khách Đăng ký (Insert). User Đổi mật khẩu/Cập nhật Profile (Update). Admin thao tác Khóa tài khoản (Update status = BANNED).

### 3. `user_roles` (Phân quyền User)
- **Chứa dữ liệu gì:** Nối `user_id` với `role_id` (Nhiều-Nhiều).
- **Cập nhật tự động:** CÓ. Khi khách đăng ký qua Google hoặc Email hoàn tất, hệ thống tự động gán ROLE_MEMBER cho họ.
- **User thao tác:** Admin thao tác giao diện quản trị để Update/Insert/Delete nhằm cấp hoặc tước quyền STAFF/ADMIN của 1 user.

### 4. `password_reset_tokens` & 5. `email_verification_tokens` (Tokens)
- **Chứa dữ liệu gì:** Lưu mã OTP/Link token bảo mật và thời gian hết hạn.
- **Cập nhật tự động:** CÓ. Khi user bấm "Quên mật khẩu" hoặc tạo tài khoản mới, hệ thống (Backend) tự sinh ra mã code và tự động Insert vào đây. Khi Token hết hạn, hệ thống (Cronjob) có thể tự động xóa đi.
- **User thao tác:** Nhập mã OTP vào Form.

---

## 2. Nhóm Quản Trị Truyện & Phân Loại (Content Management)

### 6. `categories` (Thể loại)
- **Chứa dữ liệu gì:** Tên, slug, mô tả của các thể loại truyện (Tiên Hiệp, Huyền Huyễn...).
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Admin chủ động quản lý (Insert, Update, Delete) trên màn hình Dashboard.

### 7. `novels` (Bộ truyện)
- **Chứa dữ liệu gì:** Thông tin cốt lõi (Tên, Tác giả, Avatar truyện), số lượt xem, điểm đánh giá trung bình.
- **Cập nhật tự động:** CÓ. Các trường thống kê như `views`, `average_rating`, `rating_count` sẽ được hệ thống (Backend) tự động tính toán và Update vào bảng này khi có độc giả xem hoặc đánh giá truyện.
- **User thao tác:** Staff/Admin chủ động đăng truyện (Insert), cập nhật mô tả (Update), tạm ẩn (Update status).

### 8. `novel_categories` (Truyện - Thể loại)
- **Chứa dữ liệu gì:** Lưu trữ truyện đó thuộc những thể loại nào (Nối `novel_id` và `category_id`).
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Staff/Admin chọn thể loại (Checkboxes) lúc đăng truyện, tạo ra các lệnh Insert.

### 9. `chapters` (Chương truyện)
- **Chứa dữ liệu gì:** Nội dung văn bản chương, giá Coin (nếu set VIP), hẹn giờ mở khóa, trạng thái (Nháp, Đã đăng).
- **Cập nhật tự động:** CÓ. Lượt `views` tự động tăng. Nếu có hẹn ngày (`auto_unlock_at`), Cronjob hệ thống sẽ tự động Update để mở khóa miễn phí chương VIP đó.
- **User thao tác:** Staff/Admin trực tiếp đăng bài (Insert), chỉnh sửa nội dung/đặt giá tiền (Update).

---

## 3. Nhóm Tương Tác & Trải Nghiệm Độc Giả
### 10. `bookshelves` (Tủ sách)
- **Chứa dữ liệu gì:** Danh sách truyện user đang theo dõi.
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Độc giả bấm "Lưu tủ sách" (Insert) hoặc "Bỏ theo dõi" (Delete).

### 11. `reading_progress` (Tiến độ đọc)
- **Chứa dữ liệu gì:** Lưu Bookmark (chương gần nhất độc giả vừa đọc dở) để phục vụ cho nút "Đọc tiếp".
- **Cập nhật tự động:** CÓ (Silent Update). Hệ thống ẩn (Backend) tự động Upsert lại ID chương hiện tại vào bảng này khi user cuộn trang/đọc truyện. User không chủ động bấm nút "Lưu".

### 12. `user_read_chapters` (Lịch sử đọc)
- **Chứa dữ liệu gì:** Danh sách toàn bộ các chương người dùng đã đọc qua.
- **Cập nhật tự động:** CÓ. Khi user request lấy dữ liệu của 1 chương để hiển thị lên màn hình, Backend tự động sinh ra 1 dòng Insert vào bảng này để lưu vết.

### 13. `unlocked_chapters` (Chương VIP đã mở)
- **Chứa dữ liệu gì:** Lưu quyền truy cập của User đối với chương truyện VIP có thu phí.
- **Cập nhật tự động:** CÓ. Tuy độc giả là người bấm "Mở khóa", nhưng việc Insert vào bảng này là kết quả của 1 Chuỗi Giao Dịch Tự Động (Tự trừ Coin ở bảng `users` -> Ghi log vào bảng `transactions` -> Cấp quyền vào bảng `unlocked_chapters`).
- **User thao tác:** Độc giả bấm "Xác nhận mở khóa".

### 14. `user_reader_settings` (Cài đặt UI đọc truyện)
- **Chứa dữ liệu gì:** Sở thích giao diện cá nhân (Font chữ, màu nền Light/Dark, tốc độ Audio).
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Thay đổi khi user tùy chỉnh (Update) trên thanh công cụ trong màn hình đọc.

### 15. `user_daily_checkins` (Điểm danh)
- **Chứa dữ liệu gì:** Ngày điểm danh, chuỗi ngày liên tiếp, số Coin thưởng ngẫu nhiên.
- **Cập nhật tự động:** CÓ (Trường hợp Update Ví). Hệ thống kiểm tra hợp lệ xong sẽ Tự động cộng tiền sang bảng `users`.
- **User thao tác:** User bấm "Điểm danh" (Insert record).

---

## 4. Nhóm Cộng Đồng & Báo Cáo (Community & Moderation)

### 16. `novel_ratings` (Đánh giá sao)
- **Chứa dữ liệu gì:** Số điểm (1-5 sao) và nhận xét của độc giả.
- **Cập nhật tự động:** CÓ (Liên đới). Khi user Insert dòng này xong, Backend tự tính lại Trung bình cộng rồi Update sang bảng `novels`.
- **User thao tác:** Độc giả viết đánh giá và bấm Gửi (Insert).

### 17. `comments` (Bình luận)
- **Chứa dữ liệu gì:** Comment của user trong truyện hoặc từng chương.
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Độc giả viết Comment (Insert). Admin quản lý (Update trạng thái HIDDEN/DELETED).

### 18. `comment_reports` & 19. `content_reports` (Báo cáo vi phạm)
- **Chứa dữ liệu gì:** Các vé (Ticket) tố cáo từ độc giả về Comment độc hại, Truyện sai lệch...
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Độc giả gửi phiếu (Insert). Admin/Staff vào duyệt báo cáo (Update status = PROCESSED).

### 20. `notifications` (Thông báo)
- **Chứa dữ liệu gì:** Hộp thư chứa thông báo từ hệ thống đến User.
- **Cập nhật tự động:** CÓ. Hệ thống tự động sinh ra (Insert) gửi cho User khi kết quả Báo Cáo của họ được Admin xử lý xong.
- **User thao tác:** Admin chủ động tạo thông báo thủ công gửi nhiều người. Độc giả bấm xem thì Update `is_read` = true.

---

## 5. Nhóm Thanh Toán & Giao Dịch (Payment)

### 21. `coin_packages` (Gói Nạp Coin)
- **Chứa dữ liệu gì:** Các mức giá (Ví dụ: Gói 10k VNĐ = 100 Coin).
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Admin chủ động tạo ra các gói (Insert/Update).

### 22. `deposit_orders` (Đơn Nạp Tiền)
- **Chứa dữ liệu gì:** Thông tin đơn giao dịch (Mã TT12345, Số tiền, Trạng thái: PENDING/SUCCESS).
- **Cập nhật tự động:** CÓ. Khi Ngân hàng (Ví dụ: VNPay) gọi API (Webhook/IPN) trả kết quả giao dịch về Backend của bạn, hệ thống tự động Đổi Trạng Thái từ PENDING sang SUCCESS.
- **User thao tác:** Độc giả bấm Nạp Tiền (Khởi tạo đơn PENDING).

### 23. `payment_logs` (Nhật ký Bank)
- **Chứa dữ liệu gì:** Lưu nguyên cục JSON/Data thô (Raw) mà Bank gửi về server để đối soát rủi ro.
- **Cập nhật tự động:** HOÀN TOÀN TỰ ĐỘNG (Bởi Backend đón API IPN từ Bank).

### 24. `transactions` (Sổ cái Ví)
- **Chứa dữ liệu gì:** Dòng thời gian chi tiết (Timestamp, Số lượng cộng/trừ, Dư nợ cuối) của 1 cái ví User.
- **Cập nhật tự động:** HOÀN TOÀN TỰ ĐỘNG. Đây là bảng Không Cho Phép Xóa/Sửa. Backend tự Insert mỗi khi: Đơn Nạp Thành Công (+), Mua Chương VIP (-), Điểm Danh (+), Admin tặng tiền (+).

---

## 6. Nhóm Hệ Thống & Kiểm Toán (System Admin)

### 25. `system_settings` (Cấu hình chung)
- **Chứa dữ liệu gì:** Key-Value (Tỷ giá quy đổi, Chính sách bảo mật, Link Facebook Admin).
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Admin chỉnh sửa trên Dashboard (Update).

### 26. `system_audit_logs` (Nhật ký Admin)
- **Chứa dữ liệu gì:** Lưu Vết để chống lạm quyền (Admin A xóa Truyện B vào lúc nào, ID cũ là gì, ID mới là gì).
- **Cập nhật tự động:** CÓ. Các API nhạy cảm của Admin sẽ có AOP (Aspect-Oriented Programming) / Filter tự động móc vào và Insert log ẩn.

### 27. `user_login_logs` (Nhật ký Đăng nhập)
- **Chứa dữ liệu gì:** Lưu IP, Trình duyệt, kết quả pass/fail khi đăng nhập.
- **Cập nhật tự động:** CÓ. Security tự động Insert dòng này khi request chạy tới endpoint `/login`.

### 28. `themes` (Quản lý Giao diện)
- **Chứa dữ liệu gì:** Lưu thông tin các gói Layout (Giao diện Tết, Haloween...).
- **Cập nhật tự động:** KHÔNG.
- **User thao tác:** Admin tải lên (Upload file) và kích hoạt (Update `is_active` = true).
