---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch chuẩn hóa áp dụng cỡ chữ editor"
  Status: "Implemented"
---

# Kế hoạch chuẩn hóa áp dụng cỡ chữ editor

## Function Analysis

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Lỗi:** Tăng cỡ chữ có lúc hoạt động nhưng giảm không ổn định; selection có thể tạo wrapper lồng nhau, gây khoảng trắng/chiều cao dòng bất thường; áp dụng tại caret tạo markup khác nhau do `execCommand('fontSize')`.

## Existing Architecture

- Chỉ sửa presentation:
  - `src/main/resources/static/js/admin/chapter-editor.js`
  - Có thể bổ sung CSS editor trong `src/main/resources/static/css/novel-admin.css`.
- Không thay đổi Controller, Service, Repository, DTO, Entity, sanitizer hoặc database.

## Root Cause

1. `Range.extractContents()` giữ lại các phần tử font-size ở nhiều cấp, sau đó wrapper mới tạo ra cấu trúc lồng nhau.
2. Nhánh selection rỗng vẫn gọi `execCommand('fontSize')`, để browser tự sinh `<font>` hoặc `<span>` theo engine.
3. Khi wrapper chứa block element hoặc style cũ, line-height và khoảng trống hiển thị có thể thay đổi ngoài ý muốn.

## Implementation Plan

1. Dùng một hằng số `DEFAULT_FONT_SIZE = 16` và `DEFAULT_LINE_HEIGHT` thống nhất.
2. Khi áp dụng cho selection:
   - Tách fragment đã chọn.
   - Gỡ toàn bộ `font-size` inline trong fragment, kể cả root element và descendants.
   - Chỉ tạo một wrapper `span` duy nhất với `font-size: Npx`.
   - Không bọc block element không cần thiết; giữ cấu trúc paragraph/list/heading.
   - Chuẩn hóa các wrapper font-size liền kề để không lồng hoặc tạo khoảng trống.
3. Khi áp dụng tại caret:
   - Không gọi `execCommand('fontSize')`.
   - Chèn một marker span rỗng có `font-size: Npx`, đặt caret vào marker.
   - Khi người dùng nhập, chữ được tạo trong marker; marker rỗng không được lưu thừa.
4. Khi đọc giá trị toolbar:
   - Ưu tiên style của caret/selection hiện tại.
   - Nếu không có style riêng, hiển thị 16px.
   - Không tự thay đổi nội dung trong các event selectionchange.
5. Dọn wrapper rỗng và style cũ trước khi sync textarea.
6. Bổ sung `line-height: inherit` cho editor và wrapper cỡ chữ nếu cần để không tạo khoảng trắng bất thường.

## Acceptance Criteria

- Nhập văn bản ở 16px, đổi lên 24px rồi xuống 15px vẫn áp dụng đúng.
- Áp dụng cỡ chữ tại caret rồi nhập tiếp không tạo khoảng cách lớn.
- Không còn `<font size>` hoặc wrapper font-size lồng nhau do thao tác editor.
- Bôi đen lại selection không làm input nhảy về cỡ cũ sai.
- Không ảnh hưởng Bold, dropdown, HTML mode và submit.
- `node --check`, `mvn clean test`, `git diff --check` đạt.
