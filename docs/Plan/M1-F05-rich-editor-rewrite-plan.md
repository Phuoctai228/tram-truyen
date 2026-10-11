---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch viết lại rich-text editor chương"
  Status: "Implemented"
---

# Kế hoạch viết lại rich-text editor chương

## 1. Function Analysis

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Mục tiêu:** Cung cấp editor định dạng giống Word ở mức cần thiết, không tự thay đổi Bold khi click/caret, dropdown hoạt động, và cỡ chữ tăng/giảm ổn định.

## 2. Existing Architecture

- Thay đổi chỉ ở presentation:
  - `src/main/resources/static/js/admin/chapter-editor.js`
  - Có thể điều chỉnh `src/main/resources/templates/admin/chapters/form.html`
  - Có thể điều chỉnh `src/main/resources/static/css/novel-admin.css`
- Không thay đổi Controller, Service, Repository, DTO, Entity, database hoặc sanitizer.
- HTML vẫn đi qua textarea để submit và sanitizer server-side trước khi lưu/render.

## 3. Rewrite Strategy

1. Bỏ toàn bộ logic trạng thái dựa trên việc khôi phục selection trong lúc đọc toolbar.
2. Tạo một editor state riêng:
   - `savedRange`: selection cuối cùng hợp lệ trong editor.
   - `lastInteraction`: thao tác hiện tại là click editor, chọn dropdown, hay bấm command.
   - Không bao giờ áp dụng command trong sự kiện `selectionchange`, `mouseup`, `keyup` hoặc `input`.
3. Chỉ xử lý nội dung khi người dùng chủ động:
   - Button formatting: restore range rồi thực thi command.
   - Select: lưu range trước khi mở, áp dụng khi `change`.
   - Font size: dùng `Range` để bọc/chuẩn hóa vùng text thành `span[style.font-size]`, không dùng `execCommand('fontSize')`.
4. Trạng thái toolbar chỉ là dữ liệu hiển thị:
   - Đọc style của caret/selection.
   - Không làm thay đổi selection hoặc nội dung.
   - Nếu selection giao nhau với nhiều style khác nhau, hiển thị trạng thái mixed/inactive thay vì tự bật/tắt nội dung.
5. Chuẩn hóa selection và font-size wrapper để thao tác tăng/giảm nhiều lần không lồng wrapper lỗi.
6. Giữ source mode, submit synchronization, dropdown native và giới hạn 8–96px.

## 4. Acceptance Criteria

- Nhập chữ rồi click/caret không làm nội dung Bold thay đổi.
- Nút Bold chỉ thay đổi nội dung khi bấm nút hoặc dùng phím tắt.
- Dropdown kiểu đoạn và font mở được, áp dụng đúng selection.
- Cỡ chữ có thể đổi `24px → 18px → 8px` trên cùng một vùng chọn.
- Không tạo wrapper font-size lồng vô hạn.
- Nhập tiếp sau khi định dạng vẫn dùng đúng style hiện tại.
- Submit gửi đúng HTML qua textarea.
- JavaScript syntax check, Maven tests và diff check đều đạt.

## 5. Architecture/OOP/SOLID Check

- Presentation-only rewrite, không bypass Controller → Service → Repository.
- Không tạo service/UI business logic mới.
- Sanitizer server-side vẫn là boundary bảo mật bắt buộc.
