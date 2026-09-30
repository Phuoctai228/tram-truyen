# Kế Hoạch Triển Khai Homepage (Iter 1)

**Tài liệu này mô tả kế hoạch tích hợp HTML tĩnh của Homepage vào hệ thống Spring Boot.**

## 1. Phân tích Use Case khả thi

Dựa vào danh sách tính năng (UC) của hệ thống, khi làm Homepage, chúng ta có thể tích hợp **code liền** các chức năng sau:

### 🌟 Ưu tiên 1: Tích hợp cốt lõi (Phải làm)
* **UC-06: View Novel List (Xem danh sách truyện)**
  * **Mô tả:** Homepage yêu cầu hiển thị các danh sách: Truyện nổi bật (Banner), Mới cập nhật (6 truyện), Truyện hot tuần này (5 truyện), Đề cử cho bạn (6 truyện).
  * **Hành động:** Viết các query JPA tương ứng trong `NovelRepository` để lấy dữ liệu động và map qua Thymeleaf.

### 🚀 Ưu tiên 2: Tích hợp mở rộng (Có thể làm chung ngay)
* **UC-13: View Categories (Xem danh sách thể loại)**
  * **Mô tả:** Homepage có khu vực "Khám phá thể loại" và thanh điều hướng. 
  * **Hành động:** Truy vấn `CategoryRepository` lấy danh sách thể loại ném ra giao diện.
* **UC-20: Add Novel to Bookshelf (Thêm vào tủ sách)**
  * **Mô tả:** Trên Hero Banner có nút "+ Thêm vào Tủ sách". 
  * **Hành động:** Viết thêm 1 API RESTful để nhận request lưu truyện vào tủ sách của User đang đăng nhập (dùng AJAX/Fetch gọi từ client).
* **UC-07: Search Novels (Tìm kiếm truyện)**
  * **Mô tả:** Trên Header có thanh Search.
  * **Hành động:** Gắn form submit điều hướng sang trang Tìm kiếm (hoặc API suggest nhanh).

*Các chức năng khác như Đọc truyện (UC-11), Chi tiết truyện (UC-10), Đăng nhập (UC-02)... sẽ thuộc về các page riêng biệt, chỉ gắn link điều hướng từ Homepage.*

---

## 2. Thiết kế Kiến trúc (Theo mandatory-architecture-oop-solid)

### Controller
* `HomeController.java`: Cập nhật phương thức `GET /` để gọi `HomeService` và truyền đối tượng `HomePageDTO` xuống model Thymeleaf.
* *(Tùy chọn)* `BookshelfRestController.java`: Chứa API `POST /api/bookshelf/add` (Phục vụ UC-20).

### Service
* `HomeService.java`: Nơi tập trung business logic (tổng hợp dữ liệu cho trang chủ để tránh God Controller).

### Repository
* `NovelRepository.java`: 
  * `findTopByOrderByUpdatedAtDesc(Pageable pageable)` -> Cho "Mới cập nhật".
  * `findTopByOrderByViewCountDesc(Pageable pageable)` -> Cho "Truyện hot".
  * `findRecommendedNovels(...)` -> Cho "Đề cử".
* `CategoryRepository.java`: Lấy toàn bộ thể loại.

### View (Thymeleaf)
* `src/main/resources/templates/home/index.html`: Ghi đè toàn bộ nội dung HTML mới do Tài cung cấp.
* Gắn thẻ `th:each`, `th:text`, `th:src` vào các vòng lặp danh sách truyện và thể loại.

---

## 3. Các lựa chọn triển khai (Vui lòng chọn ở hộp thoại)

* **Lựa chọn A (Chỉ UC-06, UC-13):** Chỉ đắp HTML vào, load dữ liệu động cho các danh sách truyện và danh sách thể loại. 
* **Lựa chọn B (UC-06, UC-13 + UC-20):** Làm Lựa chọn A + Viết thêm API để nút "Thêm vào tủ sách" trên màn hình có thể click và hoạt động thật (bằng Javascript fetch).
* **Lựa chọn C (Chỉ gắn HTML tạm):** Chỉ thay thế file HTML để kiểm tra giao diện, chưa cần load dữ liệu từ Database.
