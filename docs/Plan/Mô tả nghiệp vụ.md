# Mô tả nghiệp vụ (Business Description Template)

## 1. Tổng quan nghiệp vụ (Business Overview)
Nền tảng kinh doanh dịch vụ đọc truyện chữ trực tuyến - “Trạm Truyện”
Người dùng có thể:
- Xem thông tin, tìm kiếm và lọc truyện theo thể loại / bảng xếp hạng
- Quản lý tủ sách cá nhân, tự động lưu tiến độ đọc và bấm "Đọc tiếp"
- Nạp Coin qua cổng thanh toán online để mở khóa các chương VIP trả phí
- Tương tác bình luận, gửi báo lỗi chương/truyện và gửi khiếu nại bản quyền tới website

## 2. Mục tiêu nghiệp vụ (Business Objectives)
Hệ thống được xây dựng nhằm:
- Cung cấp nền tảng đọc truyện chữ trực tuyến mượt mà, tiện lợi và hiện đại cho độc giả
- Tối ưu hóa doanh thu từ việc bán quyền truy cập các chương truyện VIP chất lượng ( mặc định là có bản quyền thì bên quản trị mới cho phép đăng)
- Quản lý tập trung truyện – chương – người dùng – giao dịch Coin nội bộ hiệu quả
- Thiết lập quy trình tiếp nhận và xử lý khiếu nại rõ ràng, minh bạch.

## 3. Stakeholders (Các bên liên quan)
- **Khách vãng lai (Guest):** Đọc truyện miễn phí, tìm kiếm, lọc theo thể loại, và đăng ký tài khoản.
- **Thành viên (Member):** Lưu tủ sách, theo dõi tiến độ đọc, bình luận, báo lỗi chương/truyện, nạp Coin và mở khóa chương VIP.
- **Nhân viên (Staff):** Đăng tải truyện, cập nhật chương, đặt giá VIP, sửa lỗi nội dung và kiểm duyệt bình luận vi phạm.
- **Quản trị viên (Admin):** Quản trị người dùng, danh mục thể loại, tiếp nhận và xử lý báo cáo vi phạm (bản quyền, nội dung truyện, bình luận), đối soát nạp Coin và cấu hình hệ thống.

## 4. Quy trình nghiệp vụ hiện tại (As-Is)
Hiện tại việc đọc truyện và vận hành website đọc truyện thường gặp một số hạn chế:
Đọc truyện qua:
- Các website tràn ngập quảng cáo độc hại, popup khó chịu
- Diễn đàn rời rạc, khó đồng bộ tiến độ đọc trên nhiều thiết bị
- Nội dung truyện dễ dính tranh chấp bản quyền do lấy nguồn tràn lan và thiếu kênh tiếp nhận, gỡ bỏ kịp thời
- Người đọc khó phản hồi khi gặp chương bị lỗi định dạng, thiếu chữ hoặc die link

**Pain point:**
- Khó theo dõi tiến độ đọc và dễ mất chương đang đọc dở
- Hệ thống vận hành khó khăn nếu không có kênh tiếp nhận báo cáo lỗi/vi phạm từ cộng đồng

## 5. Quy trình nghiệp vụ mong muốn (To-Be)
Sau khi có hệ thống:
- Độc giả đọc truyện trực tuyến, tự động lưu tiến độ đọc và bấm "Đọc tiếp" tức thì
- Ban quản trị (Admin/Staff) chủ động biên tập, đăng tải truyện mới và xuất bản các chương VIP
- Độc giả nạp Coin qua cổng thanh toán (VNPay/Momo) để mở khóa đọc chương VIP vĩnh viễn (không hoàn tiền, không rút tiền)
- Khi phát hiện sai sót, độc giả gửi báo lỗi chương/truyện để ban quản trị trực tiếp xử lý
- Khi có báo cáo vi phạm (bản quyền, nội dung, bình luận), Admin tiếp nhận qua biểu mẫu, thẩm tra và có thể tạm ẩn truyện hoặc từ chối báo cáo.
- Đối với tài khoản vi phạm, quy trình xử lý bao gồm: cảnh cáo, khóa tạm thời (mặc định lần đầu là 24 giờ, lần sau gấp đôi lần trước, tăng dần lên đến 1 năm) hoặc Admin chủ động mở khóa trước thời hạn.
- Hệ thống tự động lưu lại toàn bộ lịch sử xử lý (người báo cáo, nội dung vi phạm, hình thức và thời gian xử lý) để minh bạch quá trình quản lý.

## 6. Phạm vi hệ thống (In-Scope / Out-of-Scope)
**In-Scope**
- Quản lý truyện, chương và danh mục thể loại (do Admin/Staff đăng tải và quản lý)
- Tủ sách cá nhân & đánh dấu tiến độ đọc tự động (Bookmark)
- Bình luận, báo cáo bình luận vi phạm và báo lỗi chương/truyện
- Nạp Coin qua cổng thanh toán online (VNPay sandbox / Momo)
- Mở khóa chương VIP bằng Coin
- Biểu mẫu gửi & tiếp nhận xử lý khiếu nại, bao gồm:
  - Biểu mẫu để người dùng gửi khiếu nại (sẽ được mapping để biết nó đến từ truyện nào)
  - Tiếp nhận và đánh dấu trạng thái xử lý (Chưa xử lý / Đã xử lý / Từ Chối)
  - Ghi nhận người xử lý, thời gian và kết quả xử lý cho từng khiếu nại
  - Thông báo kết quả/lý do từ chối đến người gửi khiếu nại
- Quản trị người dùng, phân quyền (Staff/Admin) và cấu hình hệ thống

**Out-of-Scope (giai đoạn đầu)**
- Chính sách hoàn tiền Coin đã nạp
- Ứng dụng di động Native (tập trung Web Responsive)

## 7. Giá trị mang lại (Business Benefits)
- Nâng cao trải nghiệm đọc truyện: giao diện sạch, không quảng cáo rác, đọc tiếp liền mạch
- Đảm bảo 100% chất lượng nội dung do đội ngũ ban quản trị trực tiếp biên tập và kiểm soát
- Nâng cao chất lượng nội dung nhờ quy trình tiếp nhận và xử lý báo cáo lỗi/vi phạm rõ ràng
- Doanh thu ổn định từ dịch vụ mở khóa chương VIP với quy trình nạp Coin tiện lợi, an toàn
