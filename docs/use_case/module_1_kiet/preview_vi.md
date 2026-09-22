# Preview Use Case Module 1 - Quản trị nội dung truyện và chương

**Người tạo:** Kiệt  
**Ngày tạo:** 2026-09-20  
**Trạng thái:** Bản xem trước, chưa xuất HTML

## UC-01-Tạo truyện
- **Mã và tên UC:** UC-01-Tạo truyện
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Cloudinary
- **Kích hoạt:** Actor chọn chức năng tạo truyện.
- **Mô tả:** Cho phép Actor khởi tạo một bộ truyện với thông tin nhận dạng và mô tả ban đầu.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Hệ thống kết nối được với cơ sở dữ liệu.
- **Điều kiện hậu quyết:**
  - **POST-1.** Một truyện mới được lưu với trạng thái ban đầu phù hợp.
  - **POST-2.** Truyện có thể được quản lý trong danh sách nội bộ.
- **Luồng cơ bản:**
  - **A. Tạo truyện thành công**
  1. Actor truy cập biểu mẫu tạo truyện.
  2. Actor nhập tiêu đề, tác giả gốc và tóm tắt.
  3. Hệ thống kiểm tra dữ liệu bắt buộc.
  4. Hệ thống tạo bản ghi truyện.
  5. Hệ thống hiển thị thông báo tạo truyện thành công.
- **Luồng thay thế:**
  - **A.2 Hủy tạo truyện**
  1. Actor chọn thao tác hủy.
  2. Hệ thống không lưu dữ liệu.
  3. Hệ thống đưa Actor về danh sách nội bộ.
  - **A.3 Dữ liệu không hợp lệ**
  1. Hệ thống xác định dữ liệu biểu mẫu không hợp lệ.
  2. Hệ thống hiển thị các trường cần chỉnh sửa.
  3. Quay lại bước 2.
- **Ngoại lệ:**
  - **EX-4 Lỗi lưu truyện**
  1. Hệ thống không thể lưu bản ghi.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Trung bình
- **Quy tắc nghiệp vụ:** Tiêu đề và tác giả là bắt buộc; truyện mới không được đánh dấu xóa.
- **Thông tin khác:** Ảnh bìa có thể được bổ sung khi cập nhật truyện.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-02-Cập nhật truyện
- **Mã và tên UC:** UC-02-Cập nhật truyện
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Cloudinary
- **Kích hoạt:** Actor chọn một truyện và chọn thao tác cập nhật.
- **Mô tả:** Cho phép Actor chỉnh sửa thông tin, trạng thái phát hành và ảnh bìa của truyện.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Truyện tồn tại và chưa bị xóa mềm.
- **Điều kiện hậu quyết:**
  - **POST-1.** Thông tin mới được lưu cho truyện.
  - **POST-2.** Ảnh bìa mới được liên kết nếu Actor tải ảnh lên.
- **Luồng cơ bản:**
  - **A. Cập nhật truyện thành công**
  1. Actor truy cập biểu mẫu chỉnh sửa truyện.
  2. Hệ thống hiển thị thông tin hiện tại.
  3. Actor cập nhật dữ liệu truyện.
  4. Hệ thống kiểm tra dữ liệu cập nhật.
  5. Hệ thống tải ảnh bìa mới lên dịch vụ lưu trữ nếu có.
  6. Hệ thống lưu thông tin truyện.
  7. Hệ thống hiển thị kết quả cập nhật.
- **Luồng thay thế:**
  - **A.3 Không thay đổi dữ liệu**
  1. Actor giữ nguyên dữ liệu hiện tại.
  2. Hệ thống không tạo thay đổi mới.
  3. Hệ thống đưa Actor về trang chi tiết nội bộ.
  - **A.4 Dữ liệu không hợp lệ**
  1. Hệ thống xác định dữ liệu cập nhật không hợp lệ.
  2. Hệ thống hiển thị trường cần chỉnh sửa.
  3. Quay lại bước 3.
- **Ngoại lệ:**
  - **EX-2 Truyện không tồn tại**
  1. Hệ thống không tìm thấy truyện.
  2. Hệ thống hiển thị thông báo dữ liệu không còn tồn tại.
  3. Hệ thống đưa Actor về danh sách nội bộ.
  - **EX-5 Lỗi lưu hoặc tải ảnh**
  1. Hệ thống dừng cập nhật.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Cao
- **Quy tắc nghiệp vụ:** Trạng thái chỉ thuộc ONGOING, COMPLETED, ON_HOLD hoặc ARCHIVED.
- **Thông tin khác:** Ảnh bìa được lưu bằng URL dịch vụ lưu trữ.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-03-Tạm ẩn truyện
- **Mã và tên UC:** UC-03-Tạm ẩn truyện
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Actor chọn thao tác tạm ẩn trên một truyện đang quản lý.
- **Mô tả:** Chuyển truyện sang trạng thái ARCHIVED để truyện không còn xuất hiện trong danh mục công khai.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Truyện tồn tại và chưa bị xóa mềm.
- **Điều kiện hậu quyết:**
  - **POST-1.** Truyện có trạng thái ARCHIVED.
  - **POST-2.** Truyện bị loại khỏi danh sách công khai.
- **Luồng cơ bản:**
  - **A. Tạm ẩn truyện thành công**
  1. Actor mở trang quản lý truyện.
  2. Actor chọn truyện cần tạm ẩn.
  3. Actor xác nhận thao tác tạm ẩn.
  4. Hệ thống kiểm tra trạng thái truyện.
  5. Hệ thống cập nhật trạng thái ARCHIVED.
  6. Hệ thống hiển thị kết quả cập nhật.
- **Luồng thay thế:**
  - **A.3 Hủy tạm ẩn**
  1. Actor hủy hộp thoại xác nhận.
  2. Hệ thống giữ nguyên trạng thái truyện.
  3. Quay lại bước 1.
  - **A.4 Truyện đã được tạm ẩn**
  1. Hệ thống xác định truyện đã ở trạng thái ARCHIVED.
  2. Hệ thống giữ nguyên dữ liệu.
  3. Quay lại bước 1.
- **Ngoại lệ:**
  - **EX-5 Lỗi cập nhật trạng thái**
  1. Hệ thống không thể cập nhật truyện.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Thấp
- **Quy tắc nghiệp vụ:** Truyện ARCHIVED không được hiển thị ở khu vực công khai.
- **Thông tin khác:** Tạm ẩn không xóa dữ liệu truyện.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-04-Xóa truyện
- **Mã và tên UC:** UC-04-Xóa truyện
- **Tác nhân chính:** Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Admin chọn thao tác xóa truyện.
- **Mô tả:** Đánh dấu truyện là đã xóa để gỡ truyện khỏi danh sách hoạt động mà không xóa vật lý bản ghi.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Admin đã đăng nhập.
  - **PRE-2.** Truyện tồn tại.
- **Điều kiện hậu quyết:**
  - **POST-1.** Truyện có is_deleted = true.
  - **POST-2.** Truyện không còn xuất hiện trong các danh sách hoạt động.
- **Luồng cơ bản:**
  - **A. Xóa truyện thành công**
  1. Admin truy cập danh sách truyện nội bộ.
  2. Admin chọn truyện cần xóa.
  3. Admin xác nhận thao tác xóa.
  4. Hệ thống kiểm tra bản ghi truyện.
  5. Hệ thống đánh dấu truyện đã bị xóa.
  6. Hệ thống loại truyện khỏi danh sách hoạt động.
  7. Hệ thống hiển thị kết quả xóa.
- **Luồng thay thế:**
  - **A.3 Hủy xóa truyện**
  1. Admin hủy thao tác xác nhận.
  2. Hệ thống giữ nguyên truyện.
  3. Quay lại bước 1.
- **Ngoại lệ:**
  - **EX-4 Truyện không tồn tại**
  1. Hệ thống không tìm thấy truyện.
  2. Hệ thống hiển thị thông báo dữ liệu không còn tồn tại.
  3. Quay lại bước 1.
  - **EX-5 Lỗi xóa mềm**
  1. Hệ thống không thể cập nhật cờ xóa.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Admin đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Thấp
- **Quy tắc nghiệp vụ:** Chức năng chỉ dành cho Admin; hệ thống dùng xóa mềm.
- **Thông tin khác:** Dữ liệu đã xóa có thể được giữ lại cho mục đích quản trị.
- **Giả định:** Admin có kết nối internet ổn định.

## UC-05-Xem danh sách truyện nội bộ
- **Mã và tên UC:** UC-05-Xem danh sách truyện nội bộ
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Actor truy cập khu vực quản trị nội dung.
- **Mô tả:** Hiển thị toàn bộ truyện trong hệ thống và hỗ trợ tìm kiếm, lọc theo trạng thái.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Hệ thống kết nối được với cơ sở dữ liệu.
- **Điều kiện hậu quyết:**
  - **POST-1.** Actor xem được danh sách truyện nội bộ.
- **Luồng cơ bản:**
  - **A. Xem danh sách nội bộ thành công**
  1. Actor truy cập trang danh sách truyện nội bộ.
  2. Hệ thống truy vấn các bản ghi truyện.
  3. Hệ thống áp dụng điều kiện tìm kiếm và lọc nếu có.
  4. Hệ thống tính dữ liệu phân trang.
  5. Hệ thống hiển thị danh sách và trạng thái truyện.
- **Luồng thay thế:**
  - **A.3 Không có kết quả**
  1. Hệ thống xác định không có truyện phù hợp.
  2. Hệ thống hiển thị trạng thái danh sách trống.
  3. Quay lại bước 1.
  - **A.3 Thay đổi bộ lọc**
  1. Actor thay đổi điều kiện tìm kiếm hoặc trạng thái.
  2. Hệ thống truy vấn lại dữ liệu.
  3. Hệ thống hiển thị kết quả mới.
  4. Quay lại bước 5.
- **Ngoại lệ:**
  - **EX-2 Lỗi truy vấn dữ liệu**
  1. Hệ thống không thể lấy danh sách truyện.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo gián đoạn dịch vụ.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Cao
- **Quy tắc nghiệp vụ:** Danh sách nội bộ có thể bao gồm truyện ARCHIVED hoặc đã xóa mềm để phục vụ quản trị.
- **Thông tin khác:** Kết quả cần hiển thị trạng thái truyện.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-06-Tạo chương
- **Mã và tên UC:** UC-06-Tạo chương
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Actor chọn thêm chương cho một truyện.
- **Mô tả:** Cho phép Actor tạo chương mới với tiêu đề, số thứ tự và nội dung văn bản.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Truyện cha tồn tại và chưa bị xóa.
- **Điều kiện hậu quyết:**
  - **POST-1.** Một chương mới được tạo cho truyện.
  - **POST-2.** Chương có trạng thái mặc định phù hợp để tiếp tục cấu hình.
- **Luồng cơ bản:**
  - **A. Tạo chương thành công**
  1. Actor truy cập biểu mẫu tạo chương.
  2. Actor nhập tiêu đề, số thứ tự và nội dung.
  3. Hệ thống kiểm tra dữ liệu chương.
  4. Hệ thống tạo bản ghi chương.
  5. Hệ thống hiển thị kết quả tạo chương.
- **Luồng thay thế:**
  - **A.2 Dữ liệu chương không hợp lệ**
  1. Hệ thống xác định dữ liệu chương không hợp lệ.
  2. Hệ thống hiển thị trường cần chỉnh sửa.
  3. Quay lại bước 2.
  - **A.2 Hủy tạo chương**
  1. Actor hủy thao tác tạo chương.
  2. Hệ thống không lưu dữ liệu.
  3. Hệ thống đưa Actor về danh sách chương.
- **Ngoại lệ:**
  - **EX-4 Lỗi lưu chương**
  1. Hệ thống không thể lưu chương.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Cao
- **Quy tắc nghiệp vụ:** Chương phải thuộc một truyện hợp lệ; số thứ tự không được trùng trong cùng truyện.
- **Thông tin khác:** Chương mới có thể được cấu hình trạng thái và VIP sau khi tạo.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-07-Cập nhật chương
- **Mã và tên UC:** UC-07-Cập nhật chương
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Actor chọn một chương và chọn thao tác cập nhật.
- **Mô tả:** Cho phép Actor chỉnh sửa tiêu đề, số thứ tự hoặc nội dung chương.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Chương tồn tại và chưa bị xóa mềm.
- **Điều kiện hậu quyết:**
  - **POST-1.** Nội dung cập nhật được lưu cho chương.
- **Luồng cơ bản:**
  - **A. Cập nhật chương thành công**
  1. Actor mở biểu mẫu chỉnh sửa chương.
  2. Hệ thống hiển thị dữ liệu hiện tại.
  3. Actor chỉnh sửa dữ liệu chương.
  4. Hệ thống kiểm tra dữ liệu cập nhật.
  5. Hệ thống lưu thay đổi chương.
  6. Hệ thống hiển thị kết quả cập nhật.
- **Luồng thay thế:**
  - **A.3 Dữ liệu không hợp lệ**
  1. Hệ thống xác định dữ liệu cập nhật không hợp lệ.
  2. Hệ thống hiển thị trường cần chỉnh sửa.
  3. Quay lại bước 3.
  - **A.3 Hủy cập nhật**
  1. Actor hủy thao tác cập nhật.
  2. Hệ thống giữ nguyên dữ liệu chương.
  3. Quay lại bước 1.
- **Ngoại lệ:**
  - **EX-2 Chương không tồn tại**
  1. Hệ thống không tìm thấy chương.
  2. Hệ thống hiển thị thông báo dữ liệu không còn tồn tại.
  3. Hệ thống đưa Actor về danh sách chương.
  - **EX-5 Lỗi lưu chương**
  1. Hệ thống không thể lưu thay đổi.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Cao
- **Quy tắc nghiệp vụ:** Số thứ tự phải duy nhất trong phạm vi truyện.
- **Thông tin khác:** Chương đang xuất bản cần được kiểm tra lại sau khi nội dung thay đổi.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-08-Khóa hoặc mở khóa chương
- **Mã và tên UC:** UC-08-Khóa hoặc mở khóa chương
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Actor chọn thao tác thay đổi trạng thái khóa của chương.
- **Mô tả:** Cho phép Actor chuyển chương sang LOCKED hoặc khôi phục trạng thái có thể hiển thị.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Chương tồn tại.
- **Điều kiện hậu quyết:**
  - **POST-1.** Trạng thái khóa của chương được cập nhật.
  - **POST-2.** Quyền truy cập công khai của chương phản ánh trạng thái mới.
- **Luồng cơ bản:**
  - **A. Thay đổi trạng thái khóa thành công**
  1. Actor mở danh sách chương.
  2. Actor chọn chương cần thay đổi.
  3. Actor xác nhận thao tác khóa hoặc mở khóa.
  4. Hệ thống kiểm tra trạng thái hiện tại.
  5. Hệ thống cập nhật trạng thái chương.
  6. Hệ thống hiển thị kết quả thay đổi.
- **Luồng thay thế:**
  - **A.3 Hủy thao tác**
  1. Actor hủy hộp thoại xác nhận.
  2. Hệ thống giữ nguyên trạng thái chương.
  3. Quay lại bước 1.
  - **A.4 Trạng thái đã được cập nhật**
  1. Hệ thống xác định trạng thái yêu cầu đã tồn tại.
  2. Hệ thống giữ nguyên dữ liệu.
  3. Quay lại bước 1.
- **Ngoại lệ:**
  - **EX-4 Chương không tồn tại**
  1. Hệ thống không tìm thấy chương.
  2. Hệ thống hiển thị thông báo dữ liệu không còn tồn tại.
  3. Quay lại bước 1.
  - **EX-5 Lỗi cập nhật trạng thái**
  1. Hệ thống không thể cập nhật chương.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Trung bình
- **Quy tắc nghiệp vụ:** Chương LOCKED không được đọc công khai.
- **Thông tin khác:** Việc khóa chương không xóa nội dung chương.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-09-Xóa chương
- **Mã và tên UC:** UC-09-Xóa chương
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Actor chọn thao tác xóa chương.
- **Mô tả:** Đánh dấu chương đã xóa mềm và gỡ chương khỏi mục lục công khai.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Chương tồn tại.
- **Điều kiện hậu quyết:**
  - **POST-1.** Chương có is_deleted = true.
  - **POST-2.** Chương không còn xuất hiện trong mục lục công khai.
- **Luồng cơ bản:**
  - **A. Xóa chương thành công**
  1. Actor mở danh sách chương.
  2. Actor chọn chương cần xóa.
  3. Actor xác nhận thao tác xóa.
  4. Hệ thống kiểm tra chương.
  5. Hệ thống đánh dấu chương đã bị xóa.
  6. Hệ thống cập nhật mục lục chương.
  7. Hệ thống hiển thị kết quả xóa.
- **Luồng thay thế:**
  - **A.3 Hủy xóa chương**
  1. Actor hủy thao tác xác nhận.
  2. Hệ thống giữ nguyên chương.
  3. Quay lại bước 1.
- **Ngoại lệ:**
  - **EX-4 Chương không tồn tại**
  1. Hệ thống không tìm thấy chương.
  2. Hệ thống hiển thị thông báo dữ liệu không còn tồn tại.
  3. Quay lại bước 1.
  - **EX-5 Lỗi xóa mềm**
  1. Hệ thống không thể cập nhật cờ xóa.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Thấp
- **Quy tắc nghiệp vụ:** Chức năng dùng xóa mềm; chương đã xóa không được hiển thị công khai.
- **Thông tin khác:** Bản ghi có thể được giữ lại cho mục đích quản trị.
- **Giả định:** Actor có kết nối internet ổn định.

## UC-10-Cài đặt chương
- **Mã và tên UC:** UC-10-Cài đặt chương
- **Tác nhân chính:** Staff, Admin
- **Tác nhân phụ:** Không có
- **Kích hoạt:** Actor chọn thao tác cài đặt chương.
- **Mô tả:** Cho phép Actor thiết lập trạng thái xuất bản, đánh dấu VIP và giá Coin của chương.
- **Điều kiện tiên quyết:**
  - **PRE-1.** Actor đã đăng nhập và có quyền quản trị nội dung.
  - **PRE-2.** Chương tồn tại và chưa bị xóa mềm.
- **Điều kiện hậu quyết:**
  - **POST-1.** Cấu hình chương được lưu.
  - **POST-2.** Quyền đọc và giá Coin phản ánh cấu hình mới.
- **Luồng cơ bản:**
  - **A. Cài đặt chương thành công**
  1. Actor mở biểu mẫu cài đặt chương.
  2. Hệ thống hiển thị cấu hình hiện tại.
  3. Actor chọn trạng thái chương.
  4. Actor chọn chế độ VIP hoặc miễn phí.
  5. Actor nhập giá Coin khi chương là VIP.
  6. Hệ thống kiểm tra tính hợp lệ của cấu hình.
  7. Hệ thống lưu cấu hình chương.
  8. Hệ thống hiển thị kết quả cài đặt.
- **Luồng thay thế:**
  - **A.4 Chương miễn phí**
  1. Actor chọn chế độ miễn phí.
  2. Hệ thống bỏ yêu cầu giá Coin.
  3. Quay lại bước 6.
  - **A.6 Cấu hình không hợp lệ**
  1. Hệ thống xác định cấu hình không hợp lệ.
  2. Hệ thống hiển thị trường cần chỉnh sửa.
  3. Quay lại bước 3.
  - **A.3 Hủy cài đặt**
  1. Actor hủy thao tác cài đặt.
  2. Hệ thống giữ nguyên cấu hình hiện tại.
  3. Quay lại bước 1.
- **Ngoại lệ:**
  - **EX-2 Chương không tồn tại**
  1. Hệ thống không tìm thấy chương.
  2. Hệ thống hiển thị thông báo dữ liệu không còn tồn tại.
  3. Hệ thống đưa Actor về danh sách chương.
  - **EX-7 Lỗi lưu cấu hình**
  1. Hệ thống không thể lưu cấu hình.
  2. Hệ thống ghi nhận lỗi.
  3. Hệ thống hiển thị thông báo lỗi thao tác.
  - **EX-1 Phiên không hợp lệ**
  1. Hệ thống hiển thị lỗi xác thực.
  2. Hệ thống chuyển Actor đến trang đăng nhập.
- **Mức độ ưu tiên:** Cao, Must Have
- **Tần suất sử dụng:** Cao
- **Quy tắc nghiệp vụ:** Trạng thái chương thuộc LOCKED, PUBLISHED_REGULAR, PUBLISHED_VIP hoặc DRAFT; chương VIP phải có giá Coin hợp lệ.
- **Thông tin khác:** Cấu hình chương quyết định chương được hiển thị và có yêu cầu mở khóa hay không.
- **Giả định:** Actor có kết nối internet ổn định.
