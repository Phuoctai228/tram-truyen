---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch sửa hành vi Word-like của trình soạn chương"
  Status: "Implemented"
---

# Kế hoạch sửa hành vi trình soạn chương

## 1. Function Analysis

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Mục tiêu:** Không tự đảo trạng thái đậm khi click vào vùng soạn thảo; cỡ chữ phải là giá trị người dùng chọn/nhập và áp dụng giống Word.

## 2. Existing Architecture

- Chỉ chỉnh presentation behavior trong:
  - [`chapter-editor.js`](../../src/main/resources/static/js/admin/chapter-editor.js)
  - [`form.html`](../../src/main/resources/templates/admin/chapters/form.html)
  - [`novel-admin.css`](../../src/main/resources/static/css/novel-admin.css)
- Không thay đổi `ChapterAdminController`, `ChapterService`, DTO, entity, repository hoặc database.
- Logic sanitize HTML server-side hiện tại tiếp tục được giữ nguyên.

## 3. Root Cause Assessment

1. `selectionchange` hiện đang gọi cập nhật toolbar quá thường xuyên và có thể khôi phục selection trong lúc browser đang chuyển focus giữa editor và toolbar.
2. `document.execCommand('fontSize')` chỉ hỗ trợ các mức 1–7, không phải kích thước pixel như Word.
3. Dropdown cỡ chữ hiện hiển thị nhãn “Bình thường/Lớn/Rất lớn”, không cho người dùng nhập số cụ thể.
4. Trạng thái active cần chỉ được đọc từ selection hiện tại, không được thực hiện thao tác làm thay đổi selection khi người dùng chỉ click vào editor.

## 4. Implementation Plan

1. Tách rõ hai thao tác:
   - `readToolbarState`: chỉ đọc trạng thái selection, không gọi `restoreSelection`.
   - `applyCommand`: chỉ khôi phục selection khi người dùng thực sự bấm/chọn công cụ.
2. Không xử lý `selectionchange` khi event phát sinh từ toolbar; chỉ cập nhật trạng thái khi selection nằm trong editor.
3. Dùng `mousedown.preventDefault()` cho toolbar button/dropdown để giữ selection như Word trước khi `change/click` xảy ra.
4. Thay cỡ chữ preset bằng ô nhập số `px` kèm nút áp dụng:
   - giá trị hợp lệ từ 8 đến 96 px;
   - hiển thị giá trị hiện tại theo selection;
   - áp dụng bằng wrapper `<span style="font-size: ...px">`.
5. Với selection rỗng, cỡ chữ mới áp dụng cho vị trí gõ tiếp theo; không đảo `bold` ngoài thao tác người dùng nhấn nút Bold.
6. Vẫn đồng bộ HTML vào textarea và giữ sanitizer server-side.
7. Cải thiện trạng thái hiển thị cỡ chữ theo giá trị thực tế, ví dụ `18 px`, thay vì chỉ “Lớn”.

## 5. OOP/SOLID and Risk Check

- Đây là thay đổi UI thuần túy, không tạo class nghiệp vụ mới.
- Không thay đổi boundary Controller → Service → Repository.
- Rủi ro chính là browser selection API khác nhau; cần kiểm tra cả selection có chữ và caret rỗng.
- Giá trị style do người dùng nhập phải được giới hạn số và đơn vị `px`, không đưa chuỗi CSS tùy ý vào DOM.

## 6. Validation

- Chọn một đoạn chữ rồi click vào editor nhiều lần: không được tự đổi đậm.
- Chọn chữ, nhập `24`, áp dụng cỡ chữ: nội dung phải nhận `24px`.
- Đặt caret cuối đoạn, nhập cỡ chữ mới: chữ gõ tiếp theo nhận cỡ đó.
- Chọn chữ đậm rồi click vị trí chữ thường: nút Bold chỉ đổi trạng thái hiển thị, không thay đổi nội dung.
- Chạy `mvn test` và `git diff --check`.

**Trạng thái:** Đã triển khai và kiểm tra bằng Maven test.
