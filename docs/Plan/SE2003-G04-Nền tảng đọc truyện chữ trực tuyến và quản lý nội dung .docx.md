**1\. Tên đề tài**

**Trạm Truyện – Nền tảng đọc truyện chữ trực tuyến và quản lý nội dung trả phí bằng Coin**

**2\. Mục tiêu**

Xây dựng nền tảng web đọc truyện chữ trực tuyến, tập trung vào:

1. **Đọc truyện thuận tiện** 

   * Tìm kiếm, lọc và xem truyện. 

   * Đọc miễn phí/VIP. 

   * Tự động lưu tiến độ và hỗ trợ **Đọc tiếp**. 

2. **Kinh doanh chương VIP** 

   * Người dùng nạp Coin qua cổng thanh toán online. 

   * Dùng Coin để mở khóa chương VIP. 

   * Chương đã mở khóa được đọc vĩnh viễn. 

3. **Quản lý nội dung** 

   * Staff/Admin quản lý truyện, chương, thể loại. 

   * Kiểm soát nội dung trước khi xuất bản. 

4. **Tương tác và kiểm soát chất lượng** 

   * Bình luận. 

   * Báo lỗi chương/truyện. 

   * Báo cáo bình luận vi phạm. 

   * Khiếu nại/tố cáo nội dung. 

5. **Quản trị hệ thống** 

   * Quản lý tài khoản và phân quyền. 

   * Xử lý báo cáo/khiếu nại. 

   * Đối soát giao dịch Coin. 

   * Lưu lịch sử xử lý để đảm bảo minh bạch. 

**3\. Các công thức / Business Rules chính**

**BR-01 – Coin**

**Số dư Coin sau giao dịch:**

Coin Balance mới \= Coin Balance hiện tại \+ Coin nạp \- Coin sử dụng

Không cho phép số dư Coin âm.

**BR-02 – Mở khóa chương VIP**

Có thể đọc chương VIP \= Đã mở khóa chương OR chương miễn phí

Khi Member mở khóa:

Coin Balance \>= Giá chương VIP

Sau khi thanh toán thành công:

Coin Balance mới \= Coin Balance \- Giá chương

Quyền đọc chương VIP được lưu lại và **không cần thanh toán lại**.

**BR-03 – Tiền nạp → Coin**

Có thể định nghĩa theo bảng quy đổi, ví dụ:

Coin nhận được \= Số tiền thanh toán × Tỷ lệ quy đổi

**BR-04 – Khóa tài khoản vi phạm**

Admin có thể chủ động đặt hẹn mở khóa hoặc mở khóa.

**BR-05 – Trạng thái khiếu nại**

Có thể sử dụng:

Chưa xử lý → Đã xử lý

hoặc

Chưa xử lý → Từ chối

Kết quả xử lý phải lưu:

* Người xử lý 

* Thời gian xử lý 

* Quyết định 

* Lý do/kết quả xử lý 

**BR-06 – Quyền đăng nội dung**

**Chỉ Admin/Staff được phép quản lý và xuất bản truyện/chương.**

Theo nghiệp vụ hiện tại, nội dung được đăng phải được kiểm soát bản quyền trước khi cho phép xuất bản.

**4\. Danh sách chức năng theo Workflow – ưu tiên giảm dần**

**Chức năng cơ bản:**

* Login 

* CRUD thể loại 

* CRUD truyện 

* CRUD chương 

* Thiết lập Free/VIP 

* Thiết lập giá chương VIP 

* Publish/Unpublish 

* Upload/quản lý nội dung chương 

**Đọc truyện**

* Trang chủ 

* Danh sách truyện 

* Tìm kiếm truyện 

* Lọc theo thể loại 

* Xem bảng xếp hạng 

* Xem chi tiết truyện 

* Xem danh sách chương 

* Đọc chương 

* Phân biệt Free/VIP 

* Kiểm tra quyền đọc VIP 

**Nạp Coin → Mở khóa VIP**

* Xem số dư Coin 

* Tạo yêu cầu nạp Coin 

* Tích hợp VNPay/Momo Sandbox 

* Payment callback 

* Xác nhận giao dịch 

* Lịch sử giao dịch 

* Cộng Coin 

* Mở khóa chương VIP 

* Trừ Coin 

* Lưu lịch sử mở khóa 

**Tủ sách & Đọc tiếp**

* Thêm truyện vào tủ sách 

* Xóa khỏi tủ sách 

* Tự động lưu tiến độ 

* Cập nhật tiến độ khi đọc 

* Danh sách truyện đang đọc 

* Đọc tiếp 

* Đánh dấu chapter đã đọc 

**Bình luận & kiểm duyệt**

* Thêm bình luận 

* Sửa/xóa bình luận của mình 

* Xem bình luận 

* Báo cáo bình luận 

* Staff/Admin xem báo cáo 

* Xử lý báo cáo 

* Ẩn/xóa bình luận 

* Lưu lịch sử xử lý 

**Báo lỗi chương/truyện**

* Gửi báo lỗi 

* Chọn loại lỗi 

* Mô tả lỗi 

* Xem danh sách báo lỗi 

* Phân công/người xử lý 

* Cập nhật trạng thái 

* Ghi nhận kết quả xử lý 

**Quản lý tài khoản & vi phạm**

* CRUD/quản lý User 

* Xem lịch sử vi phạm 

* Cảnh cáo 

* Khóa tài khoản 

* Tính thời gian khóa 

* Mở khóa 

* Quản lý Staff 

* Phân quyền Staff/Admin 

* Lưu lịch sử xử lý 

**Dashboard & thống kê**

* Top truyện 

* Top chương 

* Doanh thu Coin 

* Số lượt đọc 

* Số lượt mở khóa VIP 

* Số lượng báo cáo theo trạng thái 

\=\> Cần liệt kê chức năng chính cảu hệ thống, diễn đạt và làm rõ chức năng. Ví dụ **Bình luận & kiểm duyệt** là 2 feature riêng biệt. Chính xác là chức năng quản lý Bình Luật và Quản lý kiểm duyệt bình luận là từ phía người dùng, còn kiểm duyệt là từ phía người quản lý hệ thống. Tương tự với **Tủ sách & Đọc tiếp**

