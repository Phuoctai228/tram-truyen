---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch tinh chỉnh thao tác và danh sách chương"
  Status: "Implemented"
---

# Kế hoạch tinh chỉnh thao tác và danh sách chương

## 1. Function Analysis

- **Function ID:** M1-F05 / UC-38, UC-39, UC-41.
- **Function:** Quản lý danh sách chương trong Novel CMS.
- **Actor:** Staff, Admin.
- **Mục tiêu:** Bỏ nút truy cập chương khỏi danh sách novel, hiển thị thao tác chapter thống nhất với danh sách novel bằng nhãn “Sửa” và “Xóa”, đồng thời không hiển thị nội dung/raw HTML bên dưới tiêu đề chương.

## 2. Existing Architecture

- **Novel list presentation:** `src/main/resources/templates/admin/novels/list.html`
- **Chapter list presentation:** `src/main/resources/templates/admin/chapters/list.html`
- **Chapter controller:** `ChapterAdminController` giữ nguyên route edit/delete.
- **Chapter service/repository:** Giữ nguyên business và persistence logic.
- **CSS:** `src/main/resources/static/css/novel-admin.css`
- **Database:** Không thay đổi.

## 3. Findings

1. Novel list hiện có action “Chương” trỏ tới danh sách chapter của novel.
2. Chapter list đang dùng `icon-button` chỉ có icon, khác style `action-button` có nhãn của novel list.
3. Chapter list đang render `chapter.content` bằng `th:text` dưới tiêu đề, khiến raw HTML xuất hiện như ảnh.

## 4. Implementation Plan

### 4.1 Novel list actions

- Xóa action “Chương” khỏi cột thao tác trong `novels/list.html`.
- Giữ lại action “Sửa” và “Xóa”.
- Không xóa route quản lý chapter; route vẫn được dùng bởi các luồng nội bộ khác.

### 4.2 Chapter list actions

- Đổi link chỉnh sửa sang class `action-button`, icon bút và nhãn `Sửa`.
- Đổi nút xóa sang class `action-button danger`, icon xóa và nhãn `Xóa`.
- Giữ nguyên URL edit, POST delete, confirm dialog và CSRF behavior.
- Dùng cùng pattern HTML/CSS với action trong novel list.

### 4.3 Chapter title column

- Bỏ span preview đang hiển thị `chapter.content`.
- Chỉ hiển thị số chương và tiêu đề chương trong bảng.
- Không sanitize hoặc thay đổi nội dung được lưu; chỉ ngừng render preview ở danh sách.
- Có thể giữ CSS `.chapter-preview` nếu không còn dùng ở bề mặt khác; ưu tiên xóa selector nếu không còn tham chiếu.

## 5. OOP/SOLID Check

- **SRP:** Chỉ thay đổi presentation.
- **Reuse:** Tái sử dụng action pattern hiện có ở novel list.
- **DIP:** Không thêm dependency.
- **No layer bypass:** Không thay đổi Controller → Service → Repository.
- **Security:** Giữ POST delete và confirm hiện tại.

## 6. Acceptance Criteria

- Danh sách novel không còn nút “Chương”.
- Chapter list hiển thị nút “Sửa” và “Xóa” có icon, giống style novel list.
- Cột “Tiêu đề” không hiển thị nội dung chapter hoặc raw HTML bên dưới.
- Edit/delete vẫn hoạt động đúng route.
- Không thay đổi dữ liệu chapter hoặc backend.

## 7. Approval Gate

Đã triển khai sau khi người dùng review artifact này và chọn **Proceed**.

## 8. Implementation Result

- Đã xóa action “Chương” khỏi danh sách novel.
- Đã đổi thao tác chapter thành “Sửa” và “Xóa” theo cùng style với novel list.
- Đã bỏ hiển thị `chapter.content` dưới tiêu đề, nên raw HTML không còn xuất hiện.
- Đã xóa CSS preview không còn được sử dụng.
- `mvn clean test`: PASS.
- `git diff --check`: PASS.
