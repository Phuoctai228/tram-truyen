---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Sửa khoảng trống và mất chữ trong bảng novel"
  Status: "Implemented"
---

# Sửa khoảng trống và mất chữ trong bảng novel

## Phân tích

Ảnh cho thấy bảng novel bị giới hạn bởi `min-width: 1080px` trong vùng nội dung hẹp sau sidebar. Các cột trạng thái, đánh giá và thao tác bị đẩy ra ngoài viewport, khiến người dùng thấy khoảng trắng và tưởng rằng chữ bị mất.

## Kế hoạch và triển khai

- Chỉ scope thay đổi cho `.novels-page`.
- Loại bỏ độ rộng tối thiểu quá lớn của bảng novel.
- Cho bảng co theo vùng hiển thị và cho nội dung dài tự cắt bằng ellipsis.
- Cho cột thao tác wrap hợp lý thay vì đẩy toàn bộ bảng ra ngoài.
- Giữ nguyên ba action **Chương, Sửa, Xóa**, route và backend.

## Validation

- `mvn test`
- `git diff --check`
