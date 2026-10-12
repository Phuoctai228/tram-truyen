---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch xóa header khỏi trang danh sách novel"
  Status: "Implemented"
---

# Kế hoạch xóa header khỏi trang danh sách novel

## 1. Function Analysis

- **Function ID:** M1-F01/M1-F02, giao diện quản lý novel.
- **Actor:** Staff, Admin.
- **Mục tiêu:** Chỉ xóa thanh header search/profile khỏi trang danh sách novel theo yêu cầu; giữ nguyên toàn bộ layout, sidebar, bảng dữ liệu, nút thao tác và các thay đổi hiện tại khác.

## 2. Existing Architecture

- **Page:** `src/main/resources/templates/admin/novels/list.html`
- **Shared fragment:** `src/main/resources/templates/admin/fragments/layout.html`
- **Styles:** `src/main/resources/static/css/novel-admin.css`
- **Backend:** Không thay đổi Controller, Service, Repository, Entity, DTO hoặc Database.

## 3. Implementation Plan

1. Xóa duy nhất dòng `topbar` được replace trong `novels/list.html`.
2. Giữ nguyên `main.page-shell`, sidebar, `section.content`, bảng novel, filters và actions hiện tại.
3. Không xóa hoặc sửa fragment `topbar` trong `layout.html`, vì các trang admin khác vẫn sử dụng.
4. Bổ sung/điều chỉnh spacing chỉ khi cần để nội dung bắt đầu đúng vị trí sau khi header được bỏ.
5. Không hoàn tác các thay đổi trước đó trên trang novel.

## 4. Acceptance Criteria

- Trang danh sách novel không còn header search/profile.
- Sidebar và toàn bộ nội dung trang novel vẫn giữ nguyên.
- Các trang admin khác vẫn có thể sử dụng `topbar`.
- Không thay đổi route hoặc logic backend.

## 5. Approval Gate

Đã triển khai sau khi người dùng review artifact này và chọn **Proceed**.

## 6. Implementation Result

- Đã xóa duy nhất `topbar` khỏi `novels/list.html`.
- Sidebar, bảng novel, filters và actions được giữ nguyên.
- Fragment `topbar` dùng chung không bị thay đổi.
- Nút truy cập chương và giao diện novel nguyên bản được khôi phục.
- `git diff --check`: PASS.
