---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch sửa tương tác và trạng thái trình soạn chương"
  Status: "Implemented"
---

# Kế hoạch sửa editor chương

## 1. Phân tích chức năng

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Vấn đề:** Dropdown không áp dụng lệnh ổn định vì khi chọn option, vùng editor mất selection; toolbar không thể hiện rõ trạng thái đang bật; giao diện còn giống nhóm nút thô, khó hiểu.

## 2. Existing Architecture

- Giữ nguyên [`ChapterAdminController`](../../src/main/java/com/tramtruyen/controller/ChapterAdminController.java), [`ChapterService`](../../src/main/java/com/tramtruyen/service/ChapterService.java) và sanitizer server-side.
- Chỉ chỉnh presentation behavior tại [`chapter-editor.js`](../../src/main/resources/static/js/admin/chapter-editor.js), markup tại [`form.html`](../../src/main/resources/templates/admin/chapters/form.html) và style tại [`novel-admin.css`](../../src/main/resources/static/css/novel-admin.css).
- Không thay đổi DTO, entity, repository, transaction hoặc database.

## 3. Implementation Plan

1. Lưu/khôi phục `Selection` của contenteditable trước khi dropdown nhận focus.
2. Thực thi `formatBlock`, `fontName`, `fontSize` và các lệnh toolbar trên selection đã lưu.
3. Theo dõi `selectionchange` và `input` để cập nhật trạng thái active:
   - Đậm, nghiêng, gạch chân, gạch ngang.
   - Kiểu đoạn, font, cỡ chữ.
   - Danh sách và căn lề.
4. Thêm `aria-pressed`, tooltip rõ nghĩa, nhóm toolbar và trạng thái focus/active dễ nhận biết.
5. Cải thiện bố cục: nhóm công cụ, divider, nhãn trợ giúp, trạng thái “Đang soạn”, nút source rõ hơn và responsive.
6. Giữ đồng bộ textarea hidden trước submit và không thay đổi whitelist sanitize.

## 4. OOP/SOLID và rủi ro

- Đây là thay đổi UI thuần túy, không tạo class nghiệp vụ mới.
- JavaScript editor tiếp tục có một trách nhiệm: quản lý interaction/selection và đồng bộ form.
- Không dùng thư viện ngoài hoặc thay đổi API.
- Cần kiểm tra thủ công việc chọn chữ rồi dùng dropdown, nhấn toolbar, chuyển source mode và submit.

## 5. Validation

- Chạy `mvn test`.
- Kiểm tra `git diff --check`.
- Kiểm tra bằng browser nếu server local đang chạy: dropdown, active state, responsive và submit.

**Trạng thái:** Đã triển khai và kiểm tra bằng Maven test.
