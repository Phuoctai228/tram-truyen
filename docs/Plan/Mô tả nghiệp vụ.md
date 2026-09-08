## **Mô tả nghiệp vụ (Business Description Template)**

### **1. Tổng quan nghiệp vụ (Business Overview)**
Hệ thống là một nền tảng đọc truyện chữ trực tuyến (Trạm Truyện) hỗ trợ độc giả và các thành viên sáng tác/dịch truyện.
Người dùng có thể:
- Đọc truyện, tìm kiếm, lọc truyện theo thể loại và xem các bảng xếp hạng (Top Ngày/Tuần/Tháng/Toàn thời gian).
- Tạo tài khoản để quản lý tủ sách cá nhân, tự động theo dõi tiến độ đọc và đánh dấu (bookmark) vị trí chương đang đọc dở để "Đọc tiếp" thuận tiện.
- Tương tác thông qua bình luận và gửi báo cáo khi phát hiện chương truyện bị lỗi.
- Đăng tải truyện mới (sáng tác hoặc dịch) và gửi yêu cầu kiểm duyệt để xuất bản tới độc giả mà không cần xin cấp quyền tác giả riêng biệt.
- Nạp tiền quy đổi thành Coin để mở khóa các chương VIP trả phí. Người đăng truyện có chương VIP được hưởng chia sẻ doanh thu Coin tự động từ hệ thống và có thể rút tiền về tài khoản ngân hàng thực tế.
- Ban quản trị (Staff/Admin) kiểm duyệt nội dung (truyện, bình luận, báo cáo lỗi), phê duyệt yêu cầu rút tiền tác quyền và cấu hình vận hành hệ thống.

### **2. Mục tiêu nghiệp vụ (Business Objectives)**
Hệ thống được xây dựng nhằm:
- Cung cấp một nền tảng đọc truyện mượt mà, tiện lợi cho độc giả đam mê truyện chữ.
- Thúc đẩy nền kinh tế sáng tạo (Creator Economy) bằng cách cho phép mọi thành viên đăng truyện, nhận chia sẻ doanh thu Coin từ các chương VIP và rút tiền mặt về tài khoản ngân hàng.
- Xây dựng kho truyện phong phú, đa dạng thể loại với cơ chế quản lý và kiểm duyệt chặt chẽ để đảm bảo chất lượng nội dung.
- Tối ưu hóa trải nghiệm người dùng thông qua các tính năng lưu tủ sách, ghi nhận chương đã đọc và đánh dấu vị trí đọc tiếp tự động.

### **3. Stakeholders (Các bên liên quan)**
- **Khách truy cập (Guest):** Tìm kiếm, duyệt và đọc các chương truyện miễn phí mà không cần tài khoản.
- **Thành viên (Member):** Người dùng đã đăng ký tài khoản. Có đầy đủ các quyền:
  - Đọc truyện, tự động đánh dấu các chương đã đọc, lưu truyện vào Tủ sách cá nhân, theo dõi tiến độ đọc và click nút "Đọc tiếp" để nhảy đến đúng chương gần nhất.
  - Bình luận, báo cáo lỗi chương truyện.
  - Nạp tiền quy đổi thành Coin qua cổng thanh toán (VNPay sandbox / Momo), dùng Coin để mở khóa trực tiếp các chương VIP.
  - **Tự do sáng tác & Kiếm thu nhập tác quyền:** Mọi Member đều có thể tạo truyện mới, tải lên ảnh bìa, thêm chương nháp và gửi yêu cầu xuất bản mà không cần thủ tục xin cấp quyền tác giả riêng. Khi độc giả mở khóa chương VIP của truyện mình đăng, Member được tự động cộng Coin chia sẻ doanh thu vào ví cá nhân và có quyền gửi yêu cầu rút tiền về tài khoản ngân hàng khi đạt hạn mức tối thiểu.
- **Nhân viên kiểm duyệt (Staff):** Tiếp nhận và kiểm duyệt truyện đăng mới lần đầu, kiểm duyệt/ẩn bình luận vi phạm và tiếp nhận, giải quyết các báo cáo lỗi chương từ người dùng.
- **Quản trị viên (Admin):** Quản lý toàn bộ hệ thống: phân quyền tài khoản (Staff/Admin), khóa/mở khóa tài khoản, quản lý danh mục thể loại (Category), kiểm tra và phê duyệt các yêu cầu rút tiền của tác giả, tra cứu đối soát toàn bộ giao dịch nạp/tiêu Coin, và cấu hình các thông số hệ thống (tỷ giá quy đổi VNĐ sang Coin, tỷ lệ % chia sẻ doanh thu cho tác giả, hạn mức rút tiền tối thiểu).

### **4. Quy trình nghiệp vụ hiện tại (As-Is)**
Hiện tại, việc đọc truyện và đăng tải truyện thường gặp một số hạn chế:
- **Người đọc:** Phải tìm kiếm truyện ở nhiều nguồn rời rạc, dễ bị quên hoặc mất tiến độ đọc khi đổi thiết bị. Không có nơi quản lý tập trung những bộ truyện đang theo dõi và khó quay lại đúng chương đọc dở.
- **Người viết/Người dịch:** Thủ tục đăng ký làm tác giả ở nhiều nền tảng rườm rà, thiếu cơ chế chia sẻ doanh thu và thanh toán tác quyền minh bạch khiến người viết không có động lực cống hiến.
- **Quản lý nội dung:** Các tác phẩm đăng tải tự do thường không qua kiểm duyệt lần đầu, dẫn đến tình trạng sai chính tả, nội dung phản cảm hoặc chương bị lỗi (thiếu text, lỗi định dạng) mà không có quy trình xử lý kịp thời.

**Pain point:**
- Độc giả khó theo dõi tiến độ đọc và chương đang đọc dở.
- Thành viên muốn đăng truyện gặp rào cản phân quyền rườm rà.
- Chưa có cơ chế phân chia doanh thu và chi trả thu nhập tác quyền minh bạch.

### **5. Quy trình nghiệp vụ mong muốn (To-Be)**
Sau khi hệ thống Trạm Truyện được triển khai:
- Độc giả tìm kiếm truyện dễ dàng qua phân loại, thêm truyện vào tủ sách; hệ thống tự động ghi nhận chương đã đọc và đánh dấu (bookmark) vị trí chương gần nhất để nhấn "Đọc tiếp" mọi lúc mọi nơi.
- Thành viên (Member) tự do tạo truyện mới, tải lên ảnh bìa, thêm ít nhất 3 chương nháp và gửi yêu cầu kiểm duyệt xuất bản lần đầu (Submit for Review).
- Staff nhận thông báo, tiến hành đọc thử và duyệt/từ chối. Truyện đạt yêu cầu sẽ được xuất bản (Published) tới mọi người.
- Với các chương đăng thêm sau này, người đăng có thể lưu nháp hoặc xuất bản trực tiếp để phục vụ độc giả kịp thời.
- Độc giả nạp Coin để mở khóa chương VIP $\rightarrow$ Hệ thống tự động chia sẻ doanh thu Coin cho tác giả theo tỷ lệ quy định.
- Tác giả đạt hạn mức Coin tối thiểu có thể tạo yêu cầu rút tiền về tài khoản ngân hàng $\rightarrow$ Admin đối soát và phê duyệt chuyển khoản.
- Khi người đọc phát hiện chương bị lỗi, họ sử dụng tính năng "Báo lỗi chương" (Report Issue). Staff tiếp nhận report và phân loại xử lý.

**Lưu ý nghiệp vụ (Quy trình xuất bản & Rút tiền):**
- *Quy trình Xuất bản Truyện Lần Đầu (Workflow 1):* Member tạo truyện -> Cập nhật thông tin & ảnh bìa -> Đăng ít nhất 3 chương nháp -> Gửi yêu cầu xuất bản -> Staff kiểm duyệt -> Xuất bản truyện (hoặc từ chối kèm lý do).
- *Quy trình Xuất bản Chương Các Lần Sau:* Người đăng truyện thêm chương mới (có thể lưu nháp hoặc tick chọn xuất bản trực tiếp) mà không cần Staff duyệt lại từng chương. Staff/Admin chỉ can thiệp khi có báo cáo vi phạm.
- *Quy trình Rút tiền tác quyền (Workflow 2):* Member đạt hạn mức Coin $\ge$ Min Withdrawal $\rightarrow$ Điền thông tin ngân hàng và số Coin muốn rút $\rightarrow$ Hệ thống tạm khóa số Coin và tạo yêu cầu PENDING $\rightarrow$ Admin kiểm tra và thực hiện chuyển khoản ngoài đời $\rightarrow$ Bấm Phê duyệt (hoặc Từ chối thì hệ thống hoàn lại Coin vào ví).
- *Cơ chế ghi nhận tiến độ & Bookmark:* Mỗi khi người dùng click vào đọc một chương, hệ thống tự động đánh dấu chương đó là "Đã đọc", đồng thời cập nhật chương đó làm vị trí Bookmark gần nhất của truyện.

### **6. Phạm vi hệ thống (In-Scope / Out-of-Scope)**

**In-Scope:**
- Quản lý Truyện (Novel) & Chương truyện (Chapter).
- Quản lý Tủ sách (Bookshelf) & Đánh dấu tiến độ đọc (Bookmark & Reading Progress).
- Quản lý tương tác: Bình luận (Comment) & Báo cáo lỗi chương (Chapter Report).
- Quản lý Danh mục (Category) & Tìm kiếm/Lọc truyện (Search/Filter theo thể loại, trạng thái, Bảng xếp hạng Top Ngày/Tuần/Tháng/Toàn thời gian).
- Quản lý người dùng, phân quyền thành viên (Guest, Member, Staff, Admin), Quên/Đặt lại mật khẩu qua Email.
- Chức năng kiểm duyệt nội dung (Staff duyệt truyện đăng lần đầu, xử lý báo cáo chương lỗi, ẩn bình luận).
- **Thanh toán, Chia sẻ doanh thu & Rút tiền (Monetization, Revenue Sharing & Payout):**
  - Hỗ trợ nạp tiền quy đổi thành Coin qua cổng thanh toán VNPay sandbox / Momo.
  - Độc giả dùng Coin trong ví để mở khóa các chương VIP.
  - **Cơ chế ăn chia doanh thu Coin:** Khi độc giả mở khóa một chương VIP, số Coin chi trả sẽ được hệ thống phân chia tự động: Phần người đăng truyện (Royalty Share %, ví dụ 70%) cộng trực tiếp vào ví tác giả; Phần nền tảng (Platform Fee %, ví dụ 30%) giữ lại cho chi phí vận hành hệ thống.
  - **Quy trình rút tiền về ngân hàng:** Tác giả gửi yêu cầu rút tiền khi đạt hạn mức tối thiểu; Admin đối soát thông tin tài khoản ngân hàng và duyệt chuyển khoản thủ công.
  - Admin có quyền cấu hình tỷ giá quy đổi (VNĐ -> Coin), tỷ lệ % chia sẻ doanh thu và hạn mức rút tiền tối thiểu trong mục Cấu hình hệ thống (Manage System Settings).
  - Member và Admin đều có thể tra cứu số dư ví và lịch sử biến động số dư.

**Quy định xử lý ngoại lệ (Exception Handling):**
- *Tình huống 1:* Truyện bị từ chối kiểm duyệt.
  *Cách xử lý:* Hệ thống chuyển trạng thái truyện thành "Bị từ chối" kèm lý do phản hồi của Staff. Member có thể chỉnh sửa lại nội dung truyện/ảnh bìa/chương nháp và gửi yêu cầu kiểm duyệt lại.
- *Tình huống 2:* Người đọc vào một chương truyện bị lỗi hiển thị.
  *Cách xử lý:* Giao diện cung cấp nút "Báo lỗi chương", người đọc gửi báo cáo. Staff xử lý:
    - **Nếu truyện do Admin/Staff đăng:** Staff trực tiếp vào chỉnh sửa nội dung chương bị lỗi và đóng (Resolve) báo cáo.
    - **Nếu truyện do Member đăng:** Staff không tự ý can thiệp sửa văn phong của tác giả. Thay vào đó, Staff chuyển trạng thái chương thành "Bản nháp" (Draft) để tạm ẩn khỏi độc giả, đồng thời gửi thông báo yêu cầu Member tự vào chỉnh sửa. Khi Member sửa xong, chương sẽ được hiển thị lại và Staff đóng báo cáo.
- *Tình huống 3:* Yêu cầu rút tiền bị từ chối.
  *Cách xử lý:* Nếu thông tin tài khoản ngân hàng không hợp lệ hoặc phát hiện gian lận, Admin bấm Từ chối kèm lý do phản hồi. Hệ thống tự động hoàn lại số Coin đã tạm giữ về ví của Member.

**Out-of-Scope (Tính năng ngoài phạm vi hiện tại):**
- **Cổng chi hộ ngân hàng tự động (Automated Banking Payout API):** Để phù hợp với môi trường đồ án sinh viên và không yêu cầu giấy phép doanh nghiệp trung gian thanh toán, quy trình rút tiền được xử lý theo cơ chế: Người dùng nộp yêu cầu -> Admin đối soát và duyệt chuyển khoản thủ công trên hệ thống.
- **Hệ thống SPA/REST API độc lập:** Phiên bản này sử dụng kiến trúc Multi-layer MVC (Spring Boot + Thymeleaf) kết xuất trực tiếp HTML. Không xây dựng Frontend riêng biệt kết nối qua REST API.
- **Ứng dụng di động (Mobile App Native):** Chỉ tập trung vào giao diện Web Responsive để hiển thị tốt trên thiết bị di động, không phát triển ứng dụng Native iOS/Android.

### **7. Giá trị mang lại (Business Benefits)**
- Cải thiện tối đa trải nghiệm đọc truyện bằng tiện ích tủ sách cá nhân, đánh dấu chương đã đọc và nút "Đọc tiếp" thông minh.
- Thúc đẩy cộng đồng dịch giả, tác giả sáng tác truyện chữ nhờ cơ chế phân chia doanh thu Coin minh bạch và quy trình rút tiền thật về tài khoản ngân hàng rõ ràng.
- Đảm bảo nguồn thu bền vững cho nền tảng từ phí dịch vụ chương VIP và tạo luồng tiền khép kín chuẩn mực.
- Xây dựng thư viện truyện chữ chất lượng cao, "sạch" và ít lỗi thông qua sự tham gia của đội ngũ kiểm duyệt nội dung.

---

### **8. Các Công thức & Quy tắc tính toán (Formulas & Rules)**
Trong quá trình phát triển, các thông số sau cần được hệ thống tính toán tự động dựa trên công thức chuẩn:
1. **Đánh dấu chương đã đọc & Tiến độ đọc (Reading Progress %):**
   - *Quy tắc đánh dấu đã đọc:* Mỗi chương truyện có trạng thái ghi nhận đối với từng Member (`is_read = true` khi Member click vào xem/đọc chương đó).
   - *Công thức tiến độ đọc:*
     `Tiến độ (%) = (Số chương đã đọc / Tổng số chương đã xuất bản) * 100`
     *(Lưu ý: Mẫu số chỉ tính những chương có trạng thái là Published, không tính Draft)*
   - *Bookmark (Vị trí đọc tiếp):* Hệ thống tự động ghi nhớ chương gần nhất mà Member đã đọc. Khi Member truy cập trang Chi tiết truyện hoặc vào Tủ sách cá nhân bấm **"Đọc tiếp" (Continue Reading)**, hệ thống sẽ tự động mapping điều hướng ngay đến đúng vị trí chương đó.
2. **Tổng lượt xem truyện (Total Novel Views):**
   `Tổng lượt xem = Sum(Lượt xem của tất cả các chương thuộc truyện đó)`
   *(Mỗi khi người dùng mở đọc 1 chương, view count của chương đó +1, sau đó cộng dồn lên Novel)*
3. **Bảng xếp hạng / Top lượt xem (Leaderboard / Top Views):**
   - **Top Ngày (Daily Top):** Lọc Top N truyện có lượt xem tăng nhiều nhất trong 24 giờ qua (hoặc trong ngày hiện tại).
   - **Top Tuần / Thịnh hành (Weekly Top / Trending):** Lọc Top N truyện có lượt xem tăng nhiều nhất trong 7 ngày qua.
   - **Top Tháng (Monthly Top):** Lọc Top N truyện có lượt xem tăng nhiều nhất trong 30 ngày qua (hoặc trong tháng hiện tại).
   - **Top Toàn thời gian (All-time Top):** Lọc Top N truyện có tổng lượt xem tích lũy (`Sum(views)`) cao nhất từ trước đến nay.
4. **Quy tắc chia sẻ doanh thu Coin chương VIP (Revenue Sharing Formula):**
   Khi độc giả dùng Coin mở khóa một chương VIP có giá là `P` (Coin), và tỷ lệ chia sẻ doanh thu cho tác giả do Admin cấu hình là `R%` (ví dụ: `R = 70%`):
   - `Coin cộng vào ví tác giả = P * (R / 100)`
   - `Coin phí nền tảng giữ lại = P - Coin cộng vào ví tác giả`
5. **Quy tắc quy đổi rút tiền về tài khoản ngân hàng (Withdrawal Cash-out):**
   - Điều kiện: `Số Coin yêu cầu rút >= Hạn mức tối thiểu (min_withdrawal_coin)`
   - `Số tiền VNĐ chuyển khoản = Số Coin rút * Tỷ lệ quy đổi (coin_to_vnd_rate)`

---
**Phụ lục: Các trạng thái (Status) áp dụng trong hệ thống**
1. *Trạng thái Truyện (Novel Status):* Pending (Chờ duyệt), Published (Đã xuất bản), Rejected (Bị từ chối), Archived (Đã lưu trữ/Ẩn).
2. *Trạng thái Chương (Chapter Visibility):* Draft (Bản nháp), Published (Đã hiển thị - bao gồm Thường và VIP).
3. *Trạng thái Báo cáo (Report Status):* Open (Chờ xử lý), Resolved (Đã giải quyết), Dismissed (Đã bỏ qua).
4. *Trạng thái Yêu cầu Rút tiền (Withdrawal Status):* Pending (Chờ duyệt), Approved (Đã chuyển khoản), Rejected (Từ chối).
5. *Loại giao dịch ví (Transaction Type):* DEPOSIT (Nạp tiền vào ví), UNLOCK_CHAPTER (Dùng Coin mở khóa chương VIP), AUTHOR_RECEIVE (Tác giả nhận Coin chia sẻ doanh thu), WITHDRAWAL (Rút tiền về tài khoản ngân hàng).
