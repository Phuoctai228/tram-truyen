---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch sửa cỡ chữ mặc định và đồng bộ selection"
  Status: "Implemented"
---

# Kế hoạch sửa cỡ chữ mặc định và đồng bộ selection

## Function Analysis

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Lỗi:** Văn bản ban đầu có cỡ 10px, người dùng đổi selection lên 12px nhưng khi bôi đen lại ô cỡ chữ quay về 10px và thao tác tiếp theo bị sai.
- **UI cleanup:** Xóa dòng hướng dẫn nằm bên ngoài editor theo yêu cầu; không thêm ghi chú thay thế.

## Existing Architecture

- Chỉ thay đổi presentation:
  - `src/main/resources/static/js/admin/chapter-editor.js`
  - `src/main/resources/templates/admin/chapters/form.html`
- Không thay đổi Controller, Service, Repository, DTO, Entity, sanitizer hoặc database.

## Implementation Plan

1. Xác định một cỡ chữ mặc định có chủ đích là `16px`, dùng thống nhất cho:
   - giá trị ban đầu của input;
   - style của vùng editor khi chưa có inline font-size;
   - nội dung mới được nhập.
2. Khi selection có inline `font-size`, hiển thị đúng cỡ chữ của selection.
3. Khi selection không có inline `font-size`, giữ giá trị mặc định `16px`, không lấy lại giá trị style cũ từ anchor node.
4. Khi người dùng áp dụng cỡ chữ cho vùng chọn, ghi đè mọi cỡ chữ cũ trong fragment và giữ selection mới.
5. Không cập nhật lại input cỡ chữ từ selection trong lúc toolbar đang giữ selection để người dùng nhập giá trị mới.
6. Xóa `editor-help` bên ngoài editor; giữ thông báo lỗi validation nếu có.

## Acceptance Criteria

- Cỡ chữ mặc định hiển thị rõ là `16px`.
- Văn bản mới nhập bắt đầu ở 16px.
- Đổi `10px → 12px`, bôi đen lại không tự quay về 10px nếu selection đã nhận 12px.
- Có thể tiếp tục đổi cỡ chữ trên selection nhiều lần.
- Không còn dòng hướng dẫn bên ngoài editor.
- `node --check`, `mvn clean test`, `git diff --check` đều đạt.
