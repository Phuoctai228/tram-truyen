---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch viết lại sạch trang Create Chapter"
  Status: "Implemented"
---

# Kế hoạch viết lại sạch trang Create Chapter

## 1. Phạm vi

Viết lại toàn bộ phần giao diện và rich-text editor của trang tạo/chỉnh sửa chương để loại bỏ các lỗi chồng lấn hiện tại:

- Bold tự bật/tắt khi click hoặc nhập nội dung.
- Cỡ chữ nhập rồi bấm áp dụng nhưng không thay đổi nội dung.
- Dropdown font/kiểu đoạn không ổn định.
- Selection bị mất khi tương tác với toolbar.
- Toolbar làm form khó sử dụng hoặc gây hành vi ngoài ý muốn.

Không xóa luồng nghiệp vụ tạo chương ở Controller/Service/Repository và không thay đổi dữ liệu chương đã lưu.

## 2. Existing Architecture

- **Controller:** `ChapterAdminController` giữ binding, validation và điều hướng.
- **Service:** `ChapterService`/`ChapterServiceImpl` giữ business rule và sanitizer.
- **DTO:** `ChapterForm` giữ dữ liệu số chương, tiêu đề, nội dung.
- **Presentation:** viết lại `admin/chapters/form.html`, `admin/chapter-editor.js` và các rule editor liên quan trong `novel-admin.css`.
- **Security/database:** không thay đổi.

## 3. Thiết kế mới

1. Tách form metadata và editor thành các khu vực rõ ràng, không dùng label bao quanh toàn bộ editor.
2. Dùng một `contenteditable` duy nhất làm nguồn hiển thị và một textarea ẩn làm nguồn submit.
3. Dùng một selection snapshot duy nhất, chỉ lưu khi selection thực sự nằm trong editor.
4. Toolbar button có `type="button"` và chỉ chạy command khi click chủ động.
5. Dropdown native không chặn `mousedown`; áp dụng command tại `change`.
6. Bold/Italic/Underline/Strike dùng command chuẩn cho thao tác nội dung, nhưng trạng thái toolbar chỉ đọc để hiển thị, không tự chạy command.
7. Cỡ chữ:
   - Với vùng chọn: tạo một wrapper `span` mới từ `Range.extractContents()`, loại bỏ style cỡ chữ cũ trong fragment, rồi gắn `font-size` mới.
   - Với caret: áp dụng style cho lần nhập tiếp theo bằng cơ chế format hiện tại.
   - Không dùng lại marker `<font size="7">` và không quét thay thế toàn editor.
8. Đồng bộ textarea trước submit và khi nội dung thay đổi.
9. Giữ HTML mode nhưng chỉ cho phép chuyển đổi rõ ràng giữa editor và textarea.

## 4. Files dự kiến

- Rewrite `src/main/resources/templates/admin/chapters/form.html`.
- Rewrite `src/main/resources/static/js/admin/chapter-editor.js`.
- Làm sạch các rule editor liên quan trong `src/main/resources/static/css/novel-admin.css`.
- Cập nhật plan này thành `Implemented` sau khi hoàn tất.

## 5. Validation

- `node --check src/main/resources/static/js/admin/chapter-editor.js`.
- `mvn clean test`.
- `git diff --check`.
- Browser smoke test:
  - nhập chữ, click nhiều vị trí không đổi Bold;
  - bôi chọn chữ, bật/tắt Bold;
  - đổi cỡ `24px → 14px → 8px`;
  - đổi font và kiểu đoạn;
  - nhập tiếp và submit form.

## 6. Architecture/OOP/SOLID

- Presentation-only rewrite, không bypass Controller → Service → Repository.
- Không thêm business logic vào Controller hoặc JavaScript.
- Sanitizer server-side tiếp tục là ranh giới bảo mật HTML.
