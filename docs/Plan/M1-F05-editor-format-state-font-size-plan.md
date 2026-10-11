---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch sửa trạng thái Bold và giảm cỡ chữ editor"
  Status: "Implemented"
---

# Kế hoạch sửa trạng thái định dạng editor

## 1. Function Analysis

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Regression:** Sau khi đã nhập chữ, click đổi vị trí caret làm trạng thái Bold hiển thị bật/tắt ngoài ý muốn; áp dụng cỡ chữ lớn hoạt động nhưng giảm về cỡ nhỏ/bình thường không luôn cập nhật nội dung.

## 2. Existing Architecture

- Chỉ thay đổi presentation/editor behavior:
  - `src/main/resources/static/js/admin/chapter-editor.js`
  - Có thể cập nhật `src/main/resources/static/css/novel-admin.css` nếu cần hiển thị trạng thái rõ ràng.
- Không thay đổi Controller, Service, Repository, DTO, Entity, database hoặc sanitizer.
- Vẫn đồng bộ HTML qua textarea và giữ sanitizer server-side hiện có.

## 3. Root Cause Direction

1. `queryCommandState('bold')` được đọc trong nhiều sự kiện selection/caret khi nội dung đã có inline formatting, khiến trạng thái toolbar phản ánh các chuyển tiếp focus/selection không ổn định.
2. Cơ chế đổi cỡ chữ hiện chỉ thay thế `font[size="7"]`; tùy trình duyệt, `execCommand('fontSize')` có thể tạo `span`/style khác hoặc giữ wrapper cỡ chữ cũ, nên thao tác giảm cỡ không tạo kết quả rõ ràng.

## 4. Implementation Plan

1. Tách việc cập nhật trạng thái toolbar khỏi việc khôi phục selection:
   - Chỉ cập nhật trạng thái sau khi selection ổn định trong editor.
   - Không dùng việc cập nhật trạng thái để thay đổi selection hoặc nội dung.
   - Giữ trạng thái nút Bold phản ánh caret/selection hiện tại, không tự thực thi lệnh khi người dùng click editor.
2. Chuẩn hóa thao tác cỡ chữ:
   - Áp dụng marker cỡ chữ cho cả text selection và caret hiện tại.
   - Xử lý tất cả markup marker mà browser có thể tạo.
   - Loại bỏ/ghi đè style cỡ chữ cũ trong vùng được chọn để giảm cỡ cũng có hiệu lực.
   - Giữ giới hạn an toàn 8–96px và đồng bộ HTML sau thao tác.
3. Bảo toàn hành vi dropdown, submit form và selection preservation đang hoạt động.
4. Chạy `mvn clean test` và `git diff --check`; kiểm tra browser các trường hợp gõ chữ, click đổi caret, tăng rồi giảm cỡ chữ.

## 5. Architecture/OOP/SOLID Check

- Presentation-only change, không bypass Controller → Service → Repository.
- Không tạo class mới hoặc lặp business logic.
- Không ảnh hưởng authorization, transaction hoặc persistence boundary.

## 6. Acceptance Criteria

- Gõ chữ rồi click các vị trí khác nhau không tự thay đổi nội dung Bold.
- Nút Bold chỉ thay đổi nội dung khi người dùng bấm nút hoặc dùng phím tắt.
- Chọn đoạn đã phóng to rồi giảm xuống 8px/giá trị nhỏ hơn, nội dung thực sự nhận cỡ mới.
- Có thể tiếp tục gõ sau khi áp dụng cỡ chữ mới.
- Dropdown và submit form tiếp tục hoạt động.
