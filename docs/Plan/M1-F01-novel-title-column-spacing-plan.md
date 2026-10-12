---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Cân lại chiều rộng cột tên tác phẩm"
  Status: "Implemented"
---

# Cân lại chiều rộng cột tên tác phẩm

## Phân tích

Tên tác phẩm đang bị bẻ giữa từ do cột đầu chưa phân bổ đủ không gian cho ảnh và nội dung, trong khi các cột phụ chiếm nhiều khoảng trống. Cần ưu tiên chiều rộng cho cột tác phẩm và chỉ cho phép xuống dòng tại khoảng trắng.

## Triển khai

- Tăng tỷ lệ cột **Tác phẩm**.
- Giảm hợp lý tỷ lệ các cột tác giả, trạng thái, đánh giá và thao tác.
- Cho phần nội dung trong ô tác phẩm co giãn bằng `flex: 1`.
- Bỏ `overflow-wrap: anywhere` để không bẻ giữa chữ.
- Giữ nguyên action, route và dữ liệu.

## Validation

- `mvn test`
- `git diff --check`
