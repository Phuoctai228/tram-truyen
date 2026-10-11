---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch sửa regression form và dropdown editor"
  Status: "Implemented"
---

# Kế hoạch sửa regression editor chương

## 1. Function Analysis

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Lỗi hiện tại:** form editor bị vỡ layout; click vào vùng soạn thảo làm trạng thái Bold bị cập nhật/đảo ngoài ý muốn; hai dropdown kiểu đoạn và font không mở hoặc không áp dụng.

## 2. Existing Architecture

- Chỉ sửa presentation:
  - [`chapter-editor.js`](../../src/main/resources/static/js/admin/chapter-editor.js)
  - [`form.html`](../../src/main/resources/templates/admin/chapters/form.html)
  - [`novel-admin.css`](../../src/main/resources/static/css/novel-admin.css)
- Không thay đổi Controller, Service, Repository, DTO, Entity hoặc database.
- Giữ sanitizer server-side hiện có.

## 3. Root Cause

1. `preventDefault()` đang áp dụng cho cả `<select>`, nên browser không mở native dropdown.
2. `selectionchange` và `updateToolbarState()` chạy trong lúc click/focus editor, khiến trạng thái nút bị đọc lại ở thời điểm không ổn định.
3. `label` chứa input và button trong toolbar làm markup form dễ bị submit/nesting hành vi ngoài ý muốn.
4. Dropdown format/font chưa được cập nhật theo selection một cách an toàn.

## 4. Implementation Plan

1. Chỉ chặn `mousedown` trên button thao tác định dạng; không chặn trên `<select>` và input cỡ chữ.
2. Với dropdown, lưu selection ở `mousedown` nhưng để browser mở menu native; áp dụng lệnh ở `change`.
3. Không gọi `queryCommandState` khi editor chỉ nhận click/caret; chỉ cập nhật active state sau khi selection ổn định hoặc sau khi lệnh đã thực thi.
4. Không dùng `label` làm wrapper cho nút áp dụng; chuyển thành `div` để tránh submit/label interaction lỗi.
5. Bọc mỗi dropdown bằng container riêng, sửa CSS để không bị rule `.novel-form label` áp dụng nhầm.
6. Giữ cỡ chữ nhập px và đồng bộ textarea trước submit.
7. Kiểm tra format block/font dropdown, Bold không tự đổi khi click, layout toolbar và form submit.

## 5. Validation

- Mở được dropdown kiểu đoạn và font bằng chuột.
- Chọn một đoạn rồi đổi font/kiểu đoạn, kết quả áp dụng đúng selection.
- Click nhiều vị trí trong editor không tự bật/tắt Bold.
- Nút Bold chỉ thay đổi nội dung khi người dùng chủ động nhấn.
- Submit form không bị kích hoạt bởi thao tác toolbar.
- Chạy `mvn clean test` và `git diff --check`.

**Trạng thái:** Đã triển khai và kiểm tra bằng Maven test.
