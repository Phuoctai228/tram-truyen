---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch giữ selection khi áp dụng cỡ chữ"
  Status: "Implemented"
---

# Kế hoạch giữ selection khi áp dụng cỡ chữ

## Function Analysis

- **Function:** M1-F05 Tạo/Cập nhật chương.
- **Actor:** Staff, Admin.
- **Lỗi:** Bôi đen văn bản rồi click ô cỡ chữ/nút Áp dụng làm mất selection; giá trị 10px/20px không được áp dụng và input quay về 16px.

## Existing Architecture

- Chỉ sửa presentation:
  - `src/main/resources/static/js/admin/chapter-editor.js`
- Không thay đổi Controller, Service, Repository, DTO, Entity, sanitizer hoặc database.

## Implementation Plan

1. Tách rõ hai loại state:
   - `savedRange`: selection cuối cùng trong editor, chỉ thay đổi bởi tương tác với editor hoặc khi command hoàn tất.
   - `pendingFontSize`: giá trị người dùng đang nhập, không bị `updateToolbar()` ghi đè trong lúc ô cỡ chữ đang được chỉnh.
2. Lưu selection tại `pointerdown/mousedown` của ô cỡ chữ và nút Áp dụng, trước khi focus chuyển khỏi editor.
3. Không cho `selectionchange`/`updateToolbar()` ghi đè selection đã lưu hoặc giá trị input khi toolbar đang thao tác.
4. Khi bấm Áp dụng:
   - Đọc giá trị input trước khi restore selection.
   - Restore đúng range đã lưu.
   - Áp dụng font-size vào selection/caret.
   - Chỉ sau khi DOM được cập nhật mới đồng bộ toolbar.
5. Sau khi áp dụng thành công, giữ input ở giá trị vừa áp dụng; chỉ đọc lại font-size từ selection khi người dùng quay lại click/chọn trong editor.
6. Giữ selection sau khi apply để người dùng nhìn thấy vùng vừa định dạng vẫn được bôi đen.

## Acceptance Criteria

- Bôi đen văn bản, nhập `20`, bấm Áp dụng: selection không mất và chữ thành 20px.
- Bôi đen lại, nhập `10`, bấm Áp dụng: chữ thành 10px, không quay về 16px.
- Input không bị reset trong lúc người dùng đang nhập.
- Click editor sau đó mới cập nhật lại cỡ chữ theo caret/selection mới.
- Không ảnh hưởng Bold, dropdown hoặc submit.
- `node --check`, `mvn clean test`, `git diff --check` đạt.
