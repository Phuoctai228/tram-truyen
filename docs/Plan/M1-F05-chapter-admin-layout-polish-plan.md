---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch tinh chỉnh giao diện trang quản lý chương"
  Status: "Implemented"
---

# Kế hoạch tinh chỉnh giao diện trang quản lý chương

## 1. Function Analysis

- **Function ID:** M1-F05 / UC-38, UC-39.
- **Function:** Tạo/Cập nhật chương và định dạng nội dung.
- **Actor:** Staff, Admin.
- **Mục tiêu UI:** Đồng bộ trang chương với layout quản lý novel, loại bỏ header không cần thiết, bỏ mục Chương dư thừa trên sidebar và sửa khoảng trắng dư ở dropdown cỡ chữ.

## 2. Existing Architecture

- **Shared presentation fragment:** `src/main/resources/templates/admin/fragments/layout.html`
  - Chứa sidebar dùng chung.
  - Chứa topbar dùng chung.
- **Chapter pages:**
  - `src/main/resources/templates/admin/chapters/list.html`
  - `src/main/resources/templates/admin/chapters/form.html`
- **Novel reference page:** `src/main/resources/templates/admin/novels/list.html`
- **Shared styles:** `src/main/resources/static/css/novel-admin.css`
- **Backend:** Không cần thay đổi Controller, Service, Repository, Entity hoặc DTO.

## 3. Findings

1. Sidebar đang có nav item “Chương” trỏ tới `#`, trong khi chapter luôn được truy cập từ từng novel.
2. Chapter form/list đang render topbar có search/profile như ảnh người dùng cung cấp; yêu cầu mới là bỏ header này khỏi giao diện trang chương.
3. Dropdown cỡ chữ nằm trong `.font-size-control`, đồng thời `select` có border/padding toàn cục và wrapper có border/padding riêng, gây cảm giác dư một khoảng/border kép.
4. Trang novel dùng `main.page-shell` và `section.content`; chapter form hiện dùng `form-shell` trực tiếp nên cần điều chỉnh class/layout để cùng nhịp khoảng cách với trang novel.

## 4. Implementation Plan

### 4.1 Sidebar

- Xóa riêng nav item “Chương” khỏi fragment dùng chung.
- Giữ liên kết “Chương” trong các action của danh sách novel để người dùng vẫn truy cập được chức năng.
- Không thay đổi route chapter hoặc phân quyền.

### 4.2 Header

- Không render `topbar` trong `chapters/list.html` và `chapters/form.html`.
- Không xóa fragment `topbar` khỏi shared layout vì các trang quản trị khác vẫn đang sử dụng.
- Bổ sung class/spacing phù hợp để nội dung bắt đầu đúng vị trí sau sidebar khi không có header.

### 4.3 Chapter page layout

- Đổi chapter list sang cấu trúc `main.page-shell` tương tự `novels/list.html`, với content wrapper nhất quán.
- Đặt form chapter trong layout content tương tự trang novel nhưng vẫn giữ giới hạn chiều rộng phù hợp cho editor.
- Giữ nguyên các trường, route, submit flow và rich-editor behavior.

### 4.4 Dropdown cỡ chữ

- Loại bỏ border/padding dư của wrapper hoặc điều chỉnh wrapper thành container không tạo border kép.
- Để dropdown tự chịu border, chiều cao và padding một lần.
- Giữ kích thước option, keyboard accessibility và selection preservation.
- Không thay đổi cách lưu `font-size` hoặc sanitizer.

## 5. OOP/SOLID Check

- **SRP:** Chỉ thay đổi presentation fragment/template/CSS; không đưa logic vào Controller.
- **DIP:** Không thêm dependency.
- **Reuse:** Tiếp tục dùng shared sidebar fragment và shared stylesheet.
- **No layer bypass:** Không ảnh hưởng luồng Controller → Service → Repository.
- **Database/Security:** Không thay đổi schema hoặc authorization.

## 6. Risks

- Xóa nav item “Chương” cần bảo đảm action “Quản lý chương” trong danh sách novel vẫn rõ ràng.
- Bỏ topbar có thể làm thay đổi khoảng cách đầu trang; sẽ dùng layout class riêng cho chapter để tránh ảnh hưởng trang khác.
- CSS dùng chung; mọi selector mới sẽ giới hạn trong chapter/layout cần thiết.

## 7. Acceptance Criteria

- Không còn mục “Chương” trên sidebar.
- Trang chapter list/form không hiển thị header search/profile như ảnh.
- Nội dung trang chapter căn chỉnh tương tự trang quản lý novel.
- Dropdown cỡ chữ không còn khoảng trắng/border kép dư thừa.
- Dropdown vẫn chọn được cỡ chữ và không làm mất selection.
- Các trang admin khác vẫn giữ header/sidebar hợp lệ.

## 8. Approval Gate

Đã triển khai sau khi người dùng review artifact này và chọn **Proceed**.

## 9. Implementation Result

- Đã xóa mục “Chương” khỏi sidebar dùng chung.
- Đã bỏ topbar search/profile khỏi trang danh sách chương và form chương.
- Đã đồng bộ chapter list/form với layout content của trang quản lý novel.
- Đã loại bỏ border/padding kép của dropdown cỡ chữ.
- `node --check`: PASS.
- `mvn clean test`: PASS.
- `git diff --check`: PASS.
