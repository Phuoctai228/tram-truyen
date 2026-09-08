## **Mô tả nghiệp vụ (Business Description Template)**

### **1. Tổng quan nghiệp vụ (Business Overview)**
Hệ thống là một nền tảng đọc truyện chữ trực tuyến (Trạm Truyện) hỗ trợ độc giả và các tác giả/dịch giả.
Người dùng có thể:
- Đọc truyện, tìm kiếm và phân loại truyện theo thể loại.
- Tạo tài khoản để quản lý tủ sách cá nhân và theo dõi lịch sử đọc.
- Tương tác thông qua bình luận và báo lỗi chương truyện.
- Đăng tải truyện mới (sáng tác hoặc dịch) và chờ kiểm duyệt để xuất bản.
- Ban quản trị có thể kiểm duyệt nội dung (truyện, chương, bình luận, báo cáo) và quản lý hệ thống.

### **2. Mục tiêu nghiệp vụ (Business Objectives)**
Hệ thống được xây dựng nhằm:
- Cung cấp một nền tảng đọc truyện mượt mà, tiện lợi cho độc giả đam mê truyện chữ.
- Tạo không gian kết nối giữa người đọc và tác giả/người dịch thông qua tính năng bình luận.
- Xây dựng kho truyện phong phú, đa dạng thể loại với cơ chế quản lý và kiểm duyệt chặt chẽ để đảm bảo chất lượng nội dung.
- Tối ưu hóa trải nghiệm người dùng thông qua các tính năng lưu tủ sách và theo dõi tiến độ đọc tự động.

### **3. Stakeholders (Các bên liên quan)**
- **Khách truy cập (Guest):** Tìm kiếm, duyệt và đọc truyện tự do mà không cần tài khoản.
- **Thành viên (Member):** Đọc truyện, quản lý tủ sách, lịch sử đọc, bình luận, báo cáo lỗi. Có thể nộp form xin cấp quyền Tác giả/Dịch giả (ROLE_AUTHOR) để được phép đăng tải tác phẩm.
- **Tác giả/Dịch giả (Author):** Là Member đã được cấp quyền, có toàn quyền tạo mới và quản lý danh sách truyện, chương truyện của mình.
- **Nhân viên kiểm duyệt (Staff):** Kiểm duyệt truyện đăng mới, kiểm duyệt chương truyện, bình luận và giải quyết các báo cáo lỗi từ người dùng.
- **Quản trị viên (Admin):** Quản lý toàn bộ hệ thống, phân quyền (role), quản lý người dùng, cấu hình hệ thống (tỷ giá quy đổi tiền tệ...) và các danh mục (category) truyện.

### **4. Quy trình nghiệp vụ hiện tại (As-Is)**
Hiện tại, việc đọc truyện và đăng tải truyện thường gặp một số hạn chế:
- **Người đọc:** Phải tìm kiếm truyện ở nhiều nguồn rời rạc, dễ bị quên hoặc mất tiến độ đọc khi đổi thiết bị. Không có một nơi quản lý tập trung những bộ truyện đang theo dõi.
- **Tác giả/Người dịch:** Thiếu một nền tảng quy củ để đăng tải tác phẩm, khó quản lý danh sách chương và tiếp nhận phản hồi từ độc giả một cách có hệ thống.
- **Quản lý nội dung:** Các tác phẩm đăng tải ở nhiều nơi thường không qua kiểm duyệt, dẫn đến tình trạng sai chính tả, nội dung kém chất lượng hoặc chương bị lỗi (thiếu text, sai định dạng) mà không có người xử lý kịp thời.

**Pain point:**
- Độc giả khó theo dõi tiến độ đọc của bản thân.
- Tác giả khó quản lý tác phẩm và khó tương tác hiệu quả với độc giả.
- Chất lượng truyện không đồng đều do thiếu cơ chế kiểm duyệt và xử lý lỗi chương.

### **5. Quy trình nghiệp vụ mong muốn (To-Be)**
Sau khi hệ thống Trạm Truyện được triển khai:
- Độc giả có thể tìm kiếm truyện dễ dàng qua hệ thống phân loại, thêm truyện vào tủ sách và hệ thống tự động lưu lại "Reading Progress" (tiến độ đọc).
- Tác giả/Dịch giả tự do tạo truyện mới, tải lên ảnh bìa, thêm chương và gửi yêu cầu kiểm duyệt (Submit for Review).
- Staff nhận thông báo, tiến hành đọc thử và duyệt/từ chối. Truyện đạt yêu cầu sẽ được xuất bản (Published) tới mọi người.
- Khi người đọc phát hiện chương bị lỗi, họ sử dụng tính năng "Báo lỗi chương" (Report Issue). Staff tiếp nhận report và phân loại xử lý dựa trên quyền sở hữu (nguồn gốc) của truyện.

**Lưu ý nghiệp vụ (Quy trình xuất bản & Kiểm duyệt):**
- *Quy trình Xuất bản Truyện Lần Đầu (Workflow 1):* Tác giả đăng truyện -> Cập nhật thông tin & ảnh bìa -> Đăng ít nhất 3 chương nháp -> Submit yêu cầu xuất bản -> Staff kiểm duyệt -> Xuất bản truyện (hoặc bị từ chối nếu vi phạm).
- *Quy trình Xuất bản Chương Các Lần Sau:* Tác giả đăng thêm chương (có thể tạo nháp và tick chọn publish một hoặc nhiều chương cùng lúc) -> Chương tự động được xuất bản mà không cần Staff duyệt. Staff/Admin chỉ can thiệp khi truyện bị báo cáo (report).
- *Cơ chế lưu tiến độ:* Tiến độ đọc được lưu tự động xuống database mỗi khi Member đọc một chương mới, giúp họ có thể tiếp tục đọc trên bất kỳ thiết bị nào.

### **6. Phạm vi hệ thống (In-Scope / Out-of-Scope)**

**In-Scope:**
- Quản lý Truyện (Novel) & Chương truyện (Chapter).
- Quản lý Tủ sách (Bookshelf) & Lịch sử đọc (Reading History).
- Quản lý tương tác: Bình luận (Comment) & Báo cáo lỗi chương (Chapter Report).
- Quản lý Danh mục (Category) & Tìm kiếm/Lọc truyện (Search/Filter).
- Quản lý người dùng, phân quyền thành viên (Guest, Member, Author, Staff, Admin). Cấp/Duyệt quyền Tác giả/Dịch giả.
- Chức năng kiểm duyệt nội dung (Moderation workflow).
- **Thanh toán / Trả phí (Monetization):** Hỗ trợ nạp tiền quy đổi thành Coin qua cổng thanh toán (hệ thống không có tài khoản VIP hay cửa hàng vật phẩm). Người đọc dùng Coin để mở khóa trực tiếp các chương VIP. Phân loại chương thường (miễn phí) và chương VIP. Tác giả/Dịch giả có thể cấu hình chương VIP tự động mở khóa sau X ngày kể từ khi xuất bản, hoặc mở khóa thủ công. Admin có quyền cấu hình tỷ giá quy đổi giữa tiền thật (VNĐ) và Coin, đồng thời quản lý, đối soát các giao dịch nạp tiền.

**Quy định xử lý ngoại lệ (Exception Handling):**
- *Tình huống 1:* Truyện bị từ chối kiểm duyệt.
  *Cách xử lý:* Hệ thống chuyển trạng thái truyện thành "Bị từ chối" kèm lý do. Người đăng có thể chỉnh sửa lại nội dung và gửi yêu cầu kiểm duyệt lại.
- *Tình huống 2:* Người đọc vào một chương truyện bị lỗi hiển thị.
  *Cách xử lý:* Giao diện cung cấp sẵn nút "Báo lỗi chương", người đọc gửi báo cáo. Quá trình xử lý của Staff sẽ phụ thuộc vào ai là người đăng truyện:
    - **Nếu truyện do Admin/Staff đăng:** Staff trực tiếp vào chỉnh sửa nội dung chương bị lỗi và đóng (Resolve) báo cáo.
    - **Nếu truyện do Member đăng:** Staff không tự ý can thiệp sửa nội dung của Member. Thay vào đó, Staff sẽ đánh dấu lỗi, chuyển trạng thái chương thành "Bản nháp" (Draft) để ẩn khỏi người đọc thông thường, và hệ thống tự động gửi **Thông báo (Notification)** yêu cầu Member tự vào sửa. Sau khi Member sửa xong và cập nhật, Staff xác nhận và hiển thị lại chương.

**Out-of-Scope (Tính năng ngoài phạm vi hiện tại):**
- **Hệ thống SPA/REST API độc lập:** Phiên bản này sử dụng kiến trúc Multi-layer MVC (Spring Boot + Thymeleaf) kết xuất trực tiếp HTML. Không xây dựng Frontend riêng biệt kết nối qua REST API.
- **Ứng dụng di động (Mobile App Native):** Chỉ tập trung vào giao diện Web Responsive để hiển thị tốt trên thiết bị di động, không phát triển ứng dụng Native iOS/Android.

### **7. Giá trị mang lại (Business Benefits)**
- Cải thiện tối đa trải nghiệm đọc truyện bằng các tiện ích tủ sách cá nhân và lưu tiến độ thông minh.
- Kết nối mạnh mẽ cộng đồng tác giả và độc giả.
- Xây dựng một thư viện truyện chữ chất lượng cao, "sạch" và ít lỗi thông qua sự tham gia của đội ngũ kiểm duyệt nội dung.

---

### **8. Các Công thức & Quy tắc tính toán (Formulas & Rules)**
Trong quá trình phát triển, các thông số sau cần được hệ thống tính toán tự động dựa trên công thức chuẩn:
1. **Tiến độ đọc (Reading Progress %):**
   `Tiến độ (%) = (Số thứ tự chương hiện tại / Tổng số chương đã xuất bản) * 100`
   *(Lưu ý: Chỉ tính mẫu số dựa trên những chương có trạng thái là Published, không tính Draft)*
2. **Tổng lượt xem truyện (Total Novel Views):**
   `Tổng lượt xem = Sum(Lượt xem của tất cả các chương thuộc truyện đó)`
   *(Mỗi khi người dùng mở đọc 1 chương, view count của chương đó +1, sau đó cộng dồn lên Novel)*
3. **Bảng xếp hạng / Top lượt xem (Leaderboard / Top Views):**
   - **Top Ngày (Daily Top):** Lọc Top N truyện có lượt xem tăng nhiều nhất trong 24 giờ qua (hoặc trong ngày hiện tại).
   - **Top Tuần / Thịnh hành (Weekly Top / Trending):** Lọc Top N truyện có lượt xem tăng nhiều nhất trong 7 ngày qua.
   - **Top Tháng (Monthly Top):** Lọc Top N truyện có lượt xem tăng nhiều nhất trong 30 ngày qua (hoặc trong tháng hiện tại).
   - **Top Toàn thời gian (All-time Top):** Lọc Top N truyện có tổng lượt xem tích lũy (`Sum(views)`) cao nhất từ trước đến nay.

---
**Phụ lục: Các trạng thái (Status) áp dụng trong hệ thống**
1. *Trạng thái Truyện (Novel Status):* Pending (Chờ duyệt), Published (Đã xuất bản), Rejected (Bị từ chối), Archived (Đã lưu trữ/Ẩn).
2. *Trạng thái Chương (Chapter Visibility):* Draft (Bản nháp), Published (Đã hiển thị).
3. *Trạng thái Báo cáo (Report Status):* Open (Chờ xử lý), Resolved (Đã giải quyết), Dismissed (Đã bỏ qua).

