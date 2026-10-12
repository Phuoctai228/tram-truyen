---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch sửa chọn cỡ chữ kiểu Word trong trình soạn thảo chương"
  Status: "Implemented"
---

# Kế hoạch sửa chọn cỡ chữ kiểu Word trong trình soạn thảo chương

## 1. Function Analysis

- **Function ID:** M1-F05 / UC-38, UC-39.
- **Function:** Tạo/Cập nhật chương - định dạng cỡ chữ nội dung chương.
- **Actor:** Staff, Admin.
- **Business responsibility:** Cho phép người quản trị bôi đen một phần nội dung, chọn cỡ chữ từ dropdown theo cách quen thuộc như Microsoft Word, giữ nguyên vùng chọn khi thao tác toolbar và lưu đúng định dạng sau khi submit.

## 2. Existing Architecture

- **Presentation:** `src/main/resources/templates/admin/chapters/form.html` và
  `src/main/resources/static/js/admin/chapter-editor.js`.
- **Controller:** `ChapterAdminController` chỉ bind `ChapterForm`, gọi `ChapterService` và render/redirect.
- **Service:** `ChapterService` / `ChapterServiceImpl` xử lý tạo, cập nhật và sanitize nội dung.
- **Repository:** `ChapterRepository` persistence-only.
- **Entity:** `Chapter` chứa nội dung HTML của chương.
- **DTO:** `ChapterForm.content` nhận HTML từ editor.
- **Security:** Không thay đổi phân quyền hiện tại của endpoint quản trị; nội dung vẫn phải đi qua sanitizer.
- **Database:** Không đổi schema; chỉ bảo toàn style hợp lệ trong trường `chapter.content`.

## 3. Findings Before Implementation

1. Toolbar hiện có input số và nút “Áp dụng”, chưa phải dropdown chọn nhanh như Word.
2. Selection được lưu trước khi mất focus, nhưng thao tác chọn size cần được gói thành một control duy nhất để tránh mất range khi mở dropdown.
3. `chapter-editor.js` tạo span có cả `font-size` và `line-height`.
4. `RichTextSanitizer` đang dùng regex toàn chuỗi cho thuộc tính `style`; vì vậy style có nhiều declaration có thể bị xóa, làm cỡ chữ biến mất sau khi lưu.

## 4. Implementation Plan

### 4.1 Template

- Thay control nhập số/nút áp dụng bằng một `<select data-font-size>` có các lựa chọn:
  `8`, `10`, `12`, `14`, `16`, `18`, `20`, `24`, `28`, `32`, `36`, `48`, `72`, `96` px.
- Có option mặc định/placeholder rõ ràng; giữ `aria-label` và khả năng dùng bàn phím.
- Không thay đổi binding `ChapterForm.content` hoặc submit flow.

### 4.2 Rich editor JavaScript

- Tái sử dụng `savedRange`, `saveSelection()` và `restoreSelection()` hiện có.
- Lưu range tại `pointerdown`/`mousedown` trước khi dropdown lấy focus.
- Khi `change`:
  1. Validate size nằm trong 8–96 px.
  2. Restore đúng range đã bôi đen.
  3. Áp dụng size cho toàn bộ text node trong range; nếu caret đang collapsed thì giữ marker để chữ gõ tiếp theo dùng size đã chọn.
  4. Giữ selection sau khi định dạng để hành vi giống Word.
  5. Đồng bộ source HTML nhưng không để `updateToolbar()` reset lựa chọn vừa chọn.
- Khi người dùng click/bôi đen lại trong editor, dropdown mới đồng bộ theo vị trí mới.
- Loại bỏ event và state chỉ phục vụ input/nút “Áp dụng”, không ảnh hưởng Bold, Italic, font name, danh sách, căn lề hoặc source mode.

### 4.3 Sanitizer

- Mở rộng xử lý style theo từng declaration, chỉ giữ các thuộc tính được cho phép (`font-size` trong khoảng 8–96 px và `line-height: inherit` nếu cần).
- Không cho phép style/property khác lọt qua sanitizer.
- Bảo đảm các span cỡ chữ hợp lệ vẫn tồn tại sau create/update và khi mở lại form.

### 4.4 Tests and verification

- Kiểm tra JavaScript bằng `node --check`.
- Chạy test backend bằng `mvn clean test`.
- Chạy `git diff --check`.
- Kiểm tra thủ công hoặc browser flow:
  - Bôi đen một đoạn → chọn 20 px → đoạn vẫn được chọn và hiển thị 20 px.
  - Chọn lại đoạn → chọn 10 px → không bị reset về 16 px.
  - Lưu chương → mở lại form → cỡ chữ vẫn đúng.
  - Chọn size tại caret → gõ thêm chữ → chữ mới dùng size đã chọn.
  - Chuyển HTML mode và submit không làm mất định dạng hợp lệ.

## 5. OOP/SOLID Check

- **SRP:** UI selection/formatting giữ ở JavaScript; sanitize HTML giữ ở `RichTextSanitizer`; không đưa logic vào Controller.
- **OCP:** Tập size được cấu hình ở template; thêm size không yêu cầu thay đổi tầng nghiệp vụ.
- **LSP:** Không thêm inheritance.
- **ISP:** Không thay đổi interface service hiện tại.
- **DIP:** Không thêm dependency mới; Spring DI hiện tại của sanitizer được giữ nguyên.
- **Encapsulation:** Không expose thêm entity/repository; DTO và service contract không đổi.

## 6. Risks and Mitigations

- **Selection phức tạp qua nhiều node:** dùng range clone/restore hiện có và xử lý từng text node.
- **HTML bị mất style sau lưu:** thêm test sanitizer cho style có nhiều declaration.
- **Trình duyệt khác nhau với `execCommand`:** chỉ giữ `execCommand` cho các command đang hoạt động; cỡ chữ dùng range DOM hiện tại.
- **CSS/editor default size không đồng nhất:** thống nhất default 16 px giữa template và JavaScript.

## 7. Acceptance Criteria

- Người dùng chọn cỡ chữ qua dropdown, không cần nhập số hoặc bấm nút áp dụng.
- Vùng bôi đen được định dạng toàn bộ, không mất selection trước khi áp dụng.
- Các size 8–96 px hợp lệ được lưu và hiển thị lại sau submit.
- Không bypass Controller → Service → Repository.
- Không thay đổi database schema hoặc cơ chế phân quyền.

## 8. Approval Gate

Đã triển khai sau khi người dùng review artifact này và chọn **Proceed**.

## 9. Implementation Result

- Đã thay input số và nút áp dụng bằng dropdown cỡ chữ.
- Đã giữ selection khi dropdown lấy focus và sau khi áp dụng.
- Đã sửa sanitizer để giữ `font-size` hợp lệ cùng `line-height: inherit`.
- Đã thêm test hồi quy cho style cỡ chữ.
- `node --check`: PASS.
- `mvn clean test`: PASS.
- `git diff --check`: PASS.
