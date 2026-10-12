---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch căn chỉnh trang danh sách novel và thao tác"
  Status: "Implemented"
---

# Kế hoạch căn chỉnh trang danh sách novel và thao tác

## 1. Function Analysis

- **Function ID:** M1-F01/M1-F02/M1-F05.
- **Function:** Quản lý danh sách novel và truy cập/quản lý chapter.
- **Actor:** Staff, Admin.
- **Mục tiêu:** Giữ ba thao tác **Chương, Sửa, Xóa** trên trang novel và sửa các điểm lệch khung trong bảng theo giao diện quản trị hiện tại.

## 2. Existing Architecture

- **Presentation:** `src/main/resources/templates/admin/novels/list.html`
- **Shared CSS:** `src/main/resources/static/css/novel-admin.css`
- **Routes:** Giữ nguyên route quản lý chapter, sửa novel và xóa mềm novel.
- **Backend:** Không thay đổi Controller, Service, Repository, Entity, DTO hoặc Database.

## 3. Findings

1. Trang novel đã có đúng ba route/action nhưng đang dùng `icon-button`.
2. `icon-button` không có style trong `novel-admin.css`, nên trình duyệt hiển thị button mặc định và kích thước/icon không đồng nhất.
3. Bảng dùng `min-width: 980px`, dễ tạo scroll ngang hoặc cảm giác các cột bị lệch ở viewport hẹp.
4. Cột tác phẩm có `max-width` nhưng chưa có quy tắc overflow/min-width để phối hợp ổn định với các cột còn lại.

## 4. Implementation Plan

### 4.1 Actions

- Giữ nguyên ba action:
  - `Chương` → `/admin/novels/{id}/chapters`
  - `Sửa` → `/admin/novels/{id}/edit`
  - `Xóa` → POST `/admin/novels/{id}/delete`
- Chuyển markup sang `action-button` và `action-button danger` đã có style chung.
- Giữ icon, title, confirm dialog và form method hiện tại.
- Căn các nút cùng hàng, không làm thay đổi route hoặc backend.

### 4.2 Table alignment

- Thêm class riêng cho bảng novel để chỉ ảnh hưởng trang này.
- Quy định kích thước cột ổn định bằng `table-layout: fixed`/col sizing phù hợp hoặc selector tương đương.
- Cho cột tác phẩm co giãn; tiêu đề/tóm tắt không tràn ra ngoài.
- Cho cột thao tác đủ rộng để ba nút hiển thị cùng hàng ở desktop và wrap có kiểm soát ở mobile.
- Giữ `table-wrap` để viewport nhỏ vẫn cuộn ngang thay vì phá layout.

### 4.3 Page spacing and responsive behavior

- Kiểm tra khoảng cách content, table caption, cell padding và action alignment.
- Không thay đổi header/sidebar hoặc các trang admin khác.
- Chỉ thêm selector scoped cho novel list nếu cần.

## 5. OOP/SOLID Check

- **SRP:** Chỉ chỉnh presentation/CSS.
- **Reuse:** Tái sử dụng style `action-button` có sẵn thay vì tạo button style trùng lặp.
- **DIP:** Không thêm dependency.
- **No layer bypass:** Không thay đổi luồng Controller → Service → Repository.
- **Security:** Giữ POST delete và CSRF/form behavior.

## 6. Acceptance Criteria

- Cột thao tác hiển thị đúng ba nút **Chương, Sửa, Xóa**.
- Ba nút có cùng kích thước, border, khoảng cách và căn hàng.
- Bảng không bị lệch cột hoặc tràn nội dung ở desktop.
- Ở viewport hẹp, bảng cuộn trong `table-wrap`, không phá sidebar/content.
- Route và hành vi thao tác không thay đổi.

## 7. Implementation Result

- Đã đổi các action thành nút có nhãn **Chương**, **Sửa**, **Xóa** và giữ nguyên route/form.
- Đã thêm class scoped `.novels-page` và `.novel-table` để ổn định độ rộng cột, căn chỉnh nội dung và tránh tràn text.
- Đã giữ scroll ngang có kiểm soát trong `.table-wrap` cho viewport hẹp.
- Không thay đổi Controller, Service, Repository, Entity, DTO, Database hoặc Security.

## 8. Validation

- `mvn test`: passed, 22 tests.
- `git diff --check`: passed.
