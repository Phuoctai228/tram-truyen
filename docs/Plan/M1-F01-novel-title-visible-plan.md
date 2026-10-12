---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Hiển thị đầy đủ tên tác phẩm trong bảng novel"
  Status: "Implemented"
---

# Hiển thị đầy đủ tên tác phẩm trong bảng novel

## Phân tích

Tên tác phẩm đang bị cắt bởi selector `.novels-page .novel-cell strong, .novels-page .novel-cell span`. Selector này áp dụng `overflow: hidden` và `text-overflow: ellipsis` cho cả tiêu đề, nên tên dài hiển thị thành `Po...`.

## Triển khai

- Chỉ giới hạn summary bằng line clamp.
- Cho tên tác phẩm hiển thị đầy đủ và tự xuống dòng khi cần.
- Giữ nguyên layout, nút thao tác, route và backend.

## Validation

- `mvn test`
- `git diff --check`
