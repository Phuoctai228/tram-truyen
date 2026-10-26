# KẾ HOẠCH CHUẨN HÓA VÀ LÀM RÕ CÁCH DÙNG CÁC TRẠNG THÁI TRONG DỰ ÁN TRẠM TRUYỆN

> **Dự án**: Trạm Truyện (SWP391 - Web Đọc Truyện Chữ Thuần Túy)  
> **Kiến trúc áp dụng**: Spring Boot 3.x, Multi-Layer Architecture (Controller → Service → Repository → Entity), OOP, SOLID, Clean Code Standards.  
> **Tài liệu căn cứ**: [danh_sach_chuc_nang.txt:L81-L87](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L81-L87)

---

## 1. MỤC TIÊU VÀ BỐI CẢNH

Hệ thống Trạm Truyện vận hành dựa trên 6 nhóm trạng thái cốt lõi:
1. `NOVEL_STATUS`: Trạng thái vòng đời và phát hành của bộ truyện.
2. `CHAPTER_STATUS`: Trạng thái biên tập, bảo mật và quyền truy cập của từng chương truyện.
3. `REPORT_STATUS`: Trạng thái quy trình kiểm duyệt báo cáo vi phạm nội dung / bình luận.
4. `TRANSACTION_TYPE`: Loại biến động số dư sổ cái ví Coin (Ledger).
5. `AUTH_PROVIDER`: Phương thức xác thực danh tính người dùng.
6. `USER_STATUS`: Trạng thái kích hoạt và kỷ luật của tài khoản người dùng.

Mục tiêu của kế hoạch này là:
* **Làm rõ ranh giới nghiệp vụ (Business Boundaries)**: Định nghĩa chính xác ý nghĩa, điều kiện chuyển đổi (State Transitions) và ca sử dụng (Use Cases) gắn với từng trạng thái.
* **Khắc phục triệt để lỗi sai lệch kiến trúc**: Đặc biệt là lỗi nhầm lẫn nghiêm trọng giữa `LOCKED` (Khóa chương theo UC-40) và chương thu phí `PUBLISHED_VIP` trong tầng Service hiện tại.
* **Chuyển đổi từ String sang Java Enum chuẩn**: Áp dụng Type-Safe Enums, tuân thủ nguyên tắc OOP & Clean Code, loại bỏ Magic Strings.

---

## 2. MA TRẬN LÀM RÕ CÁCH DÙNG TRẠNG THÁI THEO TỪNG CHỨC NĂNG (USE CASE MATRIX)

### 2.1. NOVEL_STATUS (Trạng thái bộ truyện)

| Giá trị | Ý nghĩa nghiệp vụ | Chức năng liên quan (Use Case) | Actor | Tiền điều kiện & Hành động kích hoạt (Trigger) | Hậu điều kiện (Kết quả) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`ONGOING`** | **Đang ra**: Truyện đang trong quá trình sáng tác/dịch, tiếp tục ra chương mới. | • [UC-33: Tạo truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L15)<br>• [UC-34: Cập nhật truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L16)<br>• [UC-06: Xem DS truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L27)<br>• [UC-07: Tìm kiếm truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L59) | Staff, Admin, Guest, Member | • Khi Staff/Admin tạo mới truyện (UC-33), hệ thống gán mặc định `ONGOING`.<br>• Hoặc chuyển từ `ON_HOLD` / `COMPLETED` về lại khi có chương mới. | Truyện hiển thị badge "Đang ra", xuất hiện ở Trang chủ, Mục tìm kiếm, Tủ sách. |
| **`COMPLETED`** | **Hoàn thành**: Bộ truyện đã kết thúc trọn vẹn, không ra thêm chương mới. | • [UC-34: Cập nhật truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L16)<br>• [UC-06: Xem DS truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L27)<br>• [UC-07: Tìm kiếm truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L59)<br>• [UC-18: Xem tủ sách](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L33) | Staff, Admin, Guest, Member | Staff/Admin biên tập xong chương cuối cùng và chọn cập nhật trạng thái truyện thành `COMPLETED` tại form chỉnh sửa. | Truyện hiển thị badge "Hoàn thành", xuất hiện ở tab "Truyện hoàn thành" trên Trang chủ, hỗ trợ tính năng "Đã đọc xong" trong Tủ sách. |
| **`ON_HOLD`** | **Tạm ngưng**: Tác phẩm tạm dừng ra chương mới trong thời gian dài (tác giả drop/nghỉ ngơi) nhưng độc giả vẫn đọc được các chương cũ. | • [UC-34: Cập nhật truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L16)<br>• [UC-07: Tìm kiếm truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L59) | Staff, Admin, Guest, Member | Staff/Admin cập nhật trạng thái khi truyện ngừng cập nhật quá 6 tháng. | Truyện hiển thị badge "Tạm ngưng", độc giả vẫn có thể đọc các chương đã đăng bình thường. |
| **`ARCHIVED`** | **Lưu trữ / Tạm ẩn**: Tạm gỡ tác phẩm khỏi toàn bộ khu vực công khai (để rà soát bản quyền, tranh chấp hoặc nội dung có khiếu nại). | • [UC-36: Tạm ẩn truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L17)<br>• [UC-34: Cập nhật truyện](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L16) | Staff, Admin | Staff/Admin bấm "Tạm ẩn truyện" (UC-36) hoặc chọn `ARCHIVED` trong CMS. | **Ẩn hoàn toàn khỏi độc giả**: Không xuất hiện ở Trang chủ, Tìm kiếm, Bảng xếp hạng. Độc giả truy cập trực tiếp bằng URL slug sẽ nhận lỗi 404 (ResourceNotFoundException). Chỉ Admin/Staff nhìn thấy trong trang quản trị. |

---

### 2.2. CHAPTER_STATUS (Trạng thái chương truyện - TRỌNG TÂM CẦN KHẮC PHỤC)

| Giá trị | Ý nghĩa nghiệp vụ | Chức năng liên quan (Use Case) | Actor | Tiền điều kiện & Hành động kích hoạt (Trigger) | Quyền đọc nội dung của Độc giả (Reader Access) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`DRAFT`** | **Bản nháp**: Chương đang trong quá trình soạn thảo, dịch thuật hoặc tạm ẩn biên tập lại. | • [UC-38: Tạo chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L19)<br>• [UC-39: Cập nhật chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L20)<br>• [UC-42: Cài đặt chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L23)<br>• [UC-43: Xử lý báo cáo](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L71) | Staff, Admin | • Staff bấm "Lưu nháp" khi soạn thảo.<br>• Hoặc Admin xử lý báo cáo vi phạm nội dung (UC-43) chọn hành động "Chuyển chương về DRAFT để sửa đổi". | **Không hiển thị công khai**. Chỉ Staff/Admin xem được trong trang quản trị chương của truyện. |
| **`PUBLISHED_REGULAR`** | **Xuất bản thường**: Chương miễn phí, mở cho toàn bộ cộng đồng đọc đầy đủ. | • [UC-38: Tạo chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L19)<br>• [UC-39: Cập nhật chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L20)<br>• [UC-42: Cài đặt chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L23)<br>• [UC-10: Xem mục lục](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L28)<br>• [UC-11: Đọc chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L29) | Staff, Admin, Guest, Member | Staff/Admin bấm "Xuất bản" với mức giá `price == 0`. | **Mở toàn văn cho tất cả người dùng** (Guest và Member). Tự động ghi nhận `is_read = true` nếu Member đăng nhập. |
| **`PUBLISHED_VIP`** | **Xuất bản VIP**: Chương độc quyền thu phí Coin. Độc giả phải trả Coin để mở khóa đọc vĩnh viễn. | • [UC-38: Tạo chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L19)<br>• [UC-42: Cài đặt chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L23)<br>• [UC-26: Mở khóa chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L35)<br>• [UC-11: Đọc chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L29) | Staff, Admin, Member | Staff/Admin bấm "Xuất bản" với mức giá `price > 0`. | • **Chưa mở khóa**: Chỉ hiển thị tối đa 2 đoạn đầu (Masked Preview) + Modal yêu cầu mở khóa.<br>• **Đã mở khóa (UC-26)**: Hiển thị toàn văn vĩnh viễn cho Member đã mua. |
| **`LOCKED`** | **Khóa chương (Quản trị)**: Chương bị Admin/Staff tạm thời khóa và ẩn khỏi mục lục công khai do phát hiện lỗi nội dung nghiêm trọng hoặc tranh chấp. | • [UC-40: Khóa/mở khóa chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L21) | Staff, Admin | Staff/Admin kích hoạt chức năng "Khóa chương" (UC-40). | **Bị ẩn khỏi mục lục công khai** (`getPublicChapters` không trả về chương này). Độc giả không thể đọc hay mua chương cho đến khi Admin mở khóa lại. |

> [!CAUTION]
> **Điểm sai lệch nghiêm trọng hiện tại của hệ thống**:
> * `ChapterServiceImpl.resolveStatus` đang lấy `price > 0 ? "LOCKED" : "PUBLISHED"`.
> * Việc dùng nhãn `LOCKED` cho chương VIP khiến hệ thống buộc phải đưa `LOCKED` vào `PUBLIC_STATUSES`. Do đó, khi muốn khóa một chương lỗi (UC-40), hệ thống không thể ẩn được chương khỏi độc giả.
> * Ngoài ra, `HomeServiceImpl` truy vấn `...AndStatusOrderByChapterNumberDesc("PUBLISHED")` làm mất tích toàn bộ chương VIP trên danh sách 3 chương mới nhất ở trang chủ.

---

### 2.3. REPORT_STATUS (Trạng thái xử lý báo cáo vi phạm)

| Giá trị | Ý nghĩa nghiệp vụ | Chức năng liên quan (Use Case) | Actor | Tiền điều kiện & Hành động kích hoạt (Trigger) | Hậu điều kiện (Kết quả) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`RECEIVED`** *(hoặc `PENDING`)* | **Chờ xử lý / Đã tiếp nhận**: Phiếu báo cáo vi phạm mới được độc giả gửi lên và đang nằm trong hàng đợi chờ Admin/Staff duyệt. | • [UC-21: Báo cáo vi phạm (Nội dung)](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L70)<br>• [UC-22: Báo cáo bình luận](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L69)<br>• [UC-43: Xử lý báo cáo](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L71) | Member, Staff, Admin | Độc giả bấm nút "Báo lỗi / Vi phạm" tại trang đọc chương hoặc bình luận. | Phiếu báo cáo xuất hiện tại Tab "Chờ xử lý" trong CMS kiểm duyệt, tăng số đếm badge thông báo của Admin/Staff. |
| **`PROCESSED`** | **Đã xử lý (Chấp thuận)**: Admin/Staff đã xác minh phản ánh là đúng và đã thực hiện biện pháp khắc phục. | • [UC-43: Xử lý báo cáo](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L71)<br>• [UC-31: Xem thông báo](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L73) | Staff, Admin | Admin/Staff chọn hành động xử lý (Ẩn/xóa bình luận vi phạm hoặc Chuyển chương sang `DRAFT` để sửa). | • Cập nhật `status = PROCESSED`, ghi nhận `resolved_by`, `processed_at`, `admin_note`.<br>• Gửi Notification thông báo kết quả cảm ơn độc giả đã báo cáo. |
| **`REJECTED`** | **Từ chối**: Admin/Staff xem xét và kết luận nội dung không vi phạm quy định (báo cáo sai lệch, spam). | • [UC-43: Xử lý báo cáo](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L71)<br>• [UC-31: Xem thông báo](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L73) | Staff, Admin | Admin/Staff bấm "Từ chối tiếp nhận" và nhập lý do từ chối. | Cập nhật `status = REJECTED`. Gửi Notification thông báo lý do không phê duyệt báo cáo cho độc giả. |

> [!NOTE]
> **Làm rõ sự dư thừa trong tài liệu**:
> Tài liệu [danh_sach_chuc_nang.txt](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L84) liệt kê cả `PENDING (Chờ xử lý)` và `RECEIVED (Đã nhận)`. Nhưng trong thực tế quy trình kiểm duyệt, một phiếu báo cáo khi gửi lên sẽ ở ngay trạng thái "Chờ xử lý" (hiện trong DB và code đang gọi là `RECEIVED`). Để tránh nhầm lẫn, hệ thống sẽ chuẩn hóa giữ `RECEIVED` (với hiển thị Tiếng Việt trên UI là "Chờ xử lý"), hoặc đổi thành `PENDING` đồng bộ cả code và tài liệu.

---

### 2.4. TRANSACTION_TYPE (Loại biến động sổ cái ví Coin)

| Giá trị | Ý nghĩa nghiệp vụ | Chiều biến động | Chức năng liên quan (Use Case) | Actor | Cơ chế ghi sổ & Ràng buộc toàn vẹn |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`DEPOSIT`** | **Nạp tiền vào ví**: Độc giả thanh toán tiền thật qua cổng VNPay để mua Coin. | **Cộng Coin (+)** | • [UC-30: Nạp Coin](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L50)<br>• [UC-29: Xem LS giao dịch](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L48)<br>• [UC-51: Đối soát giao dịch](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L74) | Member, Admin | Cổng VNPay gửi IPN Webhook hợp lệ -> `VNPayServiceImpl` đối chiếu chữ ký số, cập nhật `topup_orders.status = SUCCESS`, tăng `wallet_balance` và ghi log `transactions` với `type = DEPOSIT`. |
| **`UNLOCK_CHAPTER`** | **Mở khóa chương VIP**: Độc giả tiêu dùng Coin trong ví để mua quyền đọc chương thu phí. | **Trừ Coin (-)** | • [UC-26: Mở khóa chương](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L35)<br>• [UC-29: Xem LS giao dịch](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L48)<br>• [UC-51: Đối soát giao dịch](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L74) | Member, Admin | Khóa bi quan (Pessimistic Lock `PESSIMISTIC_WRITE`) trên bản ghi `User` -> Kiểm tra `wallet_balance >= price` -> Trừ Coin, tạo bản ghi `user_chapter_activities(is_unlocked = true)` và ghi sổ `transactions` với `type = UNLOCK_CHAPTER`. |
| **`GIFT_COIN`** *(Cần bổ sung vào Enum)* | **Thưởng Coin điểm danh**: Hệ thống tặng Coin miễn phí khi độc giả duy trì chuỗi điểm danh hàng ngày. | **Cộng Coin (+)** | • [UC-32: Điểm danh hàng ngày](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L37)<br>• [UC-29: Xem LS giao dịch](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L48)<br>• [UC-51: Đối soát giao dịch](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L74) | Member, Admin | Kiểm tra `user_daily_checkins` hôm nay chưa điểm danh -> Cộng Coin theo chuỗi ngày (Streak) và ghi sổ `transactions` với `type = GIFT_COIN`. |

---

### 2.5. AUTH_PROVIDER (Nhà cung cấp danh tính xác thực)

| Giá trị | Ý nghĩa nghiệp vụ | Chức năng liên quan (Use Case) | Actor | Hành vi bảo mật & Ràng buộc tài khoản |
| :--- | :--- | :--- | :--- | :--- |
| **`LOCAL`** | **Tài khoản mật khẩu nội bộ**: Người dùng đăng ký qua form Email + Mật khẩu trên web. | • [UC-01: Đăng ký tài khoản](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L41)<br>• [UC-02: Đăng nhập nội bộ](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L42)<br>• [UC-05: Quên mật khẩu](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L45)<br>• [UC-17: Đổi mật khẩu](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L46) | Guest, Member, Staff, Admin | • Bắt buộc có trường `password` mã hóa bằng BCrypt.<br>• Hỗ trợ toàn bộ chu trình Quên mật khẩu (OTP Token qua email) và Đổi mật khẩu. |
| **`GOOGLE`** | **Tài khoản liên kết Google OAuth2**: Người dùng đăng nhập 1-chạm thông qua tài khoản Google. | • [UC-03: Đăng nhập Google](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L43)<br>• [UC-15: Xem hồ sơ](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L47) | Guest, Member | • Trường `password` trong CSDL để `NULL` (không cấp mật khẩu cục bộ).<br>• Tự động kích hoạt tài khoản (`status = ACTIVE`) do Google đã xác minh email.<br>• Ẩn hoặc chặn tính năng Đổi mật khẩu nội bộ. |

---

### 2.6. USER_STATUS (Trạng thái tài khoản người dùng)

| Giá trị | Ý nghĩa nghiệp vụ | Chức năng liên quan (Use Case) | Actor | Quy tắc kiểm tra quyền & Chuyển đổi trạng thái |
| :--- | :--- | :--- | :--- | :--- |
| **`PENDING_VERIFICATION`** | **Chờ kích hoạt email**: Người dùng đã điền form đăng ký nhưng chưa nhập mã OTP xác thực email. | • [UC-01: Đăng ký tài khoản](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L41)<br>• [UC-02: Đăng nhập](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L42) | Guest | • Khi đăng ký (UC-01), user được gán `PENDING_VERIFICATION`.<br>• Khi đăng nhập (UC-02), nếu chưa kích hoạt, chặn đăng nhập và điều hướng sang trang nhập OTP xác thực.<br>• Nhập đúng OTP -> Chuyển thành `ACTIVE`. |
| **`ACTIVE`** | **Đang hoạt động**: Tài khoản hợp lệ, có đầy đủ quyền hạn theo Role (ROLE_MEMBER, ROLE_STAFF, ROLE_ADMIN). | • Toàn bộ chức năng thành viên (UC-06 đến UC-52) | Member, Staff, Admin | Cho phép đăng nhập, bình luận, đánh giá truyện, nạp tiền ví Coin, đọc truyện. |
| **`BANNED`** | **Bị khóa tài khoản**: Tài khoản bị Admin xử phạt vô hiệu hóa quyền truy cập do vi phạm điều khoản cộng đồng. | • [UC-50: Khóa/Mở khóa user](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L78)<br>• [UserStatusCheckFilter](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/security/UserStatusCheckFilter.java)<br>• [AccountLockService](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/security/AccountLockService.java) | Admin, Banned User | • Admin kích hoạt khóa (UC-50).<br>• Chặn tức thì mọi request qua `UserStatusCheckFilter`. Khi đăng nhập sẽ hiển thị thời gian bị khóa và lý do.<br>• **Lazy Unban**: Khi hết hạn khóa tự động, request kế tiếp sẽ tự động chuyển trạng thái user về `ACTIVE`. |

---

## 3. KẾ HOẠCH TRIỂN KHAI CHUẨN HÓA CHI TIẾT (7 GIAI ĐOẠN)

### Giai đoạn 1: Chuẩn hóa tầng Domain Enums (`com.tramtruyen.entity.enums`)
* **Mục tiêu**: Xóa bỏ hoàn toàn Magic Strings, tạo các Java Enum chuẩn với đầy đủ chú thích tiếng Việt và helper methods.
* **Các file tạo/sửa**:
  1. `NovelStatus.java`: Định nghĩa `ONGOING`, `COMPLETED`, `ON_HOLD`, `ARCHIVED`.
  2. `ChapterStatus.java`: Định nghĩa `DRAFT`, `PUBLISHED_REGULAR`, `PUBLISHED_VIP`, `LOCKED`.
  3. `ReportStatus.java`: Định nghĩa `RECEIVED`, `PROCESSED`, `REJECTED` (với display name `RECEIVED` là "Chờ xử lý").
  4. `TransactionType.java`: Bổ sung `GIFT_COIN` bên cạnh `DEPOSIT` và `UNLOCK_CHAPTER`.
  5. `AuthProvider.java`: Định nghĩa `LOCAL`, `GOOGLE`.
  6. `UserStatus.java`: Định nghĩa `ACTIVE`, `PENDING_VERIFICATION`, `BANNED`.

### Giai đoạn 2: Cập nhật Entity & DTO Mapping
* **Mục tiêu**: Đảm bảo Entity sử dụng Enum an toàn qua JPA `@Enumerated(EnumType.STRING)` hoặc getter/setter chuyển đổi tương thích ngược.
* **Các file cập nhật**:
  * [Chapter.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/entity/Chapter.java): Chuyển trường `status` sang `ChapterStatus` (hoặc method tương thích `ChapterStatus.valueOf(status)`).
  * [Novel.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/entity/Novel.java): Chuyển trường `status` sang `NovelStatus`.
  * [User.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/entity/User.java): Sử dụng `UserStatus` và `AuthProvider`.
  * [ContentReport.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/entity/ContentReport.java) & [CommentReport.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/entity/CommentReport.java): Sử dụng `ReportStatus`.
  * [ChapterForm.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/dto/ChapterForm.java), [NovelForm.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/dto/NovelForm.java): Cập nhật validation annotations.

### Giai đoạn 3: Tái cấu trúc Logic Nghiệp vụ tại tầng Service
* **Mục tiêu**: Khắc phục dứt điểm logic phân loại chương và quy trình khóa chương.
* **Các file cập nhật**:
  * [ChapterServiceImpl.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/service/impl/ChapterServiceImpl.java):
    * Sửa `resolveStatus`:
      ```java
      private ChapterStatus resolveStatus(ChapterSaveAction action, int price) {
          if (action != ChapterSaveAction.PUBLISHED) {
              return ChapterStatus.DRAFT;
          }
          return price > 0 ? ChapterStatus.PUBLISHED_VIP : ChapterStatus.PUBLISHED_REGULAR;
      }
      ```
    * Cập nhật danh sách công khai:
      ```java
      PUBLIC_STATUSES = List.of(ChapterStatus.PUBLISHED_REGULAR.name(), ChapterStatus.PUBLISHED_VIP.name());
      ```
    * Hiện thực hóa UC-40 (Khóa / Mở khóa chương): Khi khóa chương -> chuyển sang `ChapterStatus.LOCKED` (tự động biến mất khỏi danh sách độc giả).
  * [HomeServiceImpl.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/service/impl/HomeServiceImpl.java#L80) & [NovelServiceImpl.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/service/impl/NovelServiceImpl.java#L214):
    * Sửa method lấy 3 chương mới nhất: Đổi từ tìm cố định `"PUBLISHED"` sang tìm `status IN ('PUBLISHED_REGULAR', 'PUBLISHED_VIP')` để đưa cả chương VIP mới nhất lên trang chủ.
  * [DailyCheckinServiceImpl.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/service/impl/DailyCheckinServiceImpl.java#L140):
    * Dùng `TransactionType.GIFT_COIN.name()` thay cho Magic String `"GIFT_COIN"`.
  * [AuditLogFieldFormatter.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/util/AuditLogFieldFormatter.java):
    * Hoàn thiện map mô tả tiếng Việt cho tất cả các Enum mới.

### Giai đoạn 4: Cập nhật Repository Queries
* **Mục tiêu**: Đảm bảo các truy vấn Spring Data JPA tìm kiếm theo đúng tập trạng thái công khai.
* **Các file cập nhật**:
  * [ChapterRepository.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/repository/ChapterRepository.java):
    * Cập nhật hoặc bổ sung query: `findTop3ByNovelIdAndIsDeletedFalseAndStatusInOrderByChapterNumberDesc(Integer novelId, List<String> statuses)`.
  * [NovelRepository.java](file:///c:/Users/HP/Documents/tram-truyen/src/main/java/com/tramtruyen/repository/NovelRepository.java): Đồng bộ các query lọc `NovelStatus`.

### Giai đoạn 5: Chuẩn hóa Giao diện Thymeleaf & Controller
* **Mục tiêu**: Hiển thị badge, nhãn và bộ lọc thống nhất cho người dùng và quản trị viên.
* **Các file cập nhật**:
  * `home/search.html`: Bổ sung tùy chọn lọc "Tạm ngưng" (`ON_HOLD`) cho độc giả.
  * `admin/chapters/list.html` & `staff/chapters/list.html`: Hiển thị badge tương ứng:
    * `DRAFT` -> Nhãn xám: "Bản nháp"
    * `PUBLISHED_REGULAR` -> Nhãn xanh lá: "Miễn phí"
    * `PUBLISHED_VIP` -> Nhãn vàng/cam: "Chương VIP"
    * `LOCKED` -> Nhãn đỏ: "Đã khóa (Bảo trì)"
  * `admin/reports/list.html` & `staff/reports/list.html`: Giữ nguyên tab hiển thị 3 trạng thái "Chờ xử lý" (`RECEIVED`), "Đã xử lý" (`PROCESSED`), "Từ chối" (`REJECTED`).

### Giai đoạn 6: Đồng bộ Cơ sở dữ liệu (Migration & Seed Data)
* **Mục tiêu**: Cập nhật dữ liệu mẫu và schema trong các file SQL.
* **Các file cập nhật**:
  * [novels.sql](file:///c:/Users/HP/Documents/tram-truyen/database/novels.sql): Cập nhật comment giải thích trạng thái cột `status` trong bảng `chapters` và `type` trong bảng `transactions`.
  * [seed.sql](file:///c:/Users/HP/Documents/tram-truyen/database/seed.sql): Cập nhật các dòng INSERT chương mẫu từ `PUBLISHED` sang `PUBLISHED_REGULAR` (đối với price = 0) hoặc `PUBLISHED_VIP` (đối với price > 0).

### Giai đoạn 7: Kiểm thử & Hồi quy (Testing & Verification)
* **Mục tiêu**: Đảm bảo 100% test suites vượt qua, không gây break các tính năng hiện tại.
* **Các file cập nhật & chạy test**:
  * [ChapterServiceImplTest.java](file:///c:/Users/HP/Documents/tram-truyen/src/test/java/com/tramtruyen/service/impl/ChapterServiceImplTest.java): Cập nhật các assert trạng thái từ `PUBLISHED`/`LOCKED` sang `PUBLISHED_REGULAR`/`PUBLISHED_VIP`.
  * [HomeServiceImplTest.java](file:///c:/Users/HP/Documents/tram-truyen/src/test/java/com/tramtruyen/service/impl/HomeServiceImplTest.java): Kiểm thử việc lấy top 3 chương mới nhất chứa cả chương thường và chương VIP.
  * [WalletServiceImplTest.java](file:///c:/Users/HP/Documents/tram-truyen/src/test/java/com/tramtruyen/service/impl/WalletServiceImplTest.java): Kiểm thử các loại giao dịch `DEPOSIT`, `UNLOCK_CHAPTER`, `GIFT_COIN`.

---

## 4. TỔNG KẾT ĐỀ XUẤT ĐIỀU CHỈNH TÀI LIỆU (DOCS UPDATE)

Đề xuất điều chỉnh 2 dòng tại [danh_sach_chuc_nang.txt:L83-L85](file:///c:/Users/HP/Documents/tram-truyen/docs/Plan/danh_sach_chuc_nang.txt#L83-L85) để khớp hoàn toàn với kiến trúc hệ thống:
```diff
- REPORT_STATUS: PENDING (Chờ xử lý), RECEIVED (Đã nhận), PROCESSED (Đã xử lý), REJECTED (Từ chối)
+ REPORT_STATUS: RECEIVED (Đã nhận / Chờ xử lý), PROCESSED (Đã xử lý), REJECTED (Từ chối)

- TRANSACTION_TYPE: DEPOSIT (Nạp tiền vào ví qua VNPay), UNLOCK_CHAPTER (Dùng Coin mở khóa chương VIP)
+ TRANSACTION_TYPE: DEPOSIT (Nạp tiền vào ví qua VNPay), UNLOCK_CHAPTER (Dùng Coin mở khóa chương VIP), GIFT_COIN (Thưởng Coin điểm danh)
```
