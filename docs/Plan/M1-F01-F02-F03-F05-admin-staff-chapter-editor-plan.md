---
ArtifactMetadata:
  RequestFeedback: true
  Title: "Kế hoạch mở quyền Staff và nâng cấp trình soạn chương"
  Status: "Awaiting review"
---

# Kế hoạch: Điều hướng Admin/Staff và trình soạn chương

## 1. Function Analysis

| Function | Actor | Use case | Trách nhiệm nghiệp vụ |
|---|---|---|---|
| M1-F01/M1-F02 | Staff, Admin | Tạo/Cập nhật truyện | Sau khi đăng nhập, người có quyền nội dung truy cập CMS và được điều hướng tới danh sách truyện để quản lý tác phẩm. |
| M1-F03 | Staff, Admin | Tạm ẩn truyện | Giữ nguyên luồng soft-delete/trạng thái hiện có, chỉ mở quyền truy cập đúng cho Staff theo đặc tả. |
| M1-F05 | Staff, Admin | Tạo/Cập nhật chương | Soạn nội dung chương bằng editor có định dạng văn bản cơ bản, nhưng vẫn gửi dữ liệu qua `ChapterForm` và `ChapterService`. |

## 2. Existing Architecture Inspection

### Presentation / Controller

- [`NovelAdminController`](../../src/main/java/com/tramtruyen/controller/NovelAdminController.java) đã phụ trách danh sách, tạo, cập nhật và xóa mềm truyện.
- [`ChapterAdminController`](../../src/main/java/com/tramtruyen/controller/ChapterAdminController.java) đã phụ trách danh sách, tạo, cập nhật và xóa mềm chương.
- Thymeleaf templates hiện có:
  - [`admin/novels/list.html`](../../src/main/resources/templates/admin/novels/list.html)
  - [`admin/novels/form.html`](../../src/main/resources/templates/admin/novels/form.html)
  - [`admin/chapters/list.html`](../../src/main/resources/templates/admin/chapters/list.html)
  - [`admin/chapters/form.html`](../../src/main/resources/templates/admin/chapters/form.html)

### Service / Repository / Entity / DTO

- [`NovelService`](../../src/main/java/com/tramtruyen/service/NovelService.java) và [`NovelServiceImpl`](../../src/main/java/com/tramtruyen/service/impl/NovelServiceImpl.java) đã là ranh giới nghiệp vụ của truyện.
- [`ChapterService`](../../src/main/java/com/tramtruyen/service/ChapterService.java) và [`ChapterServiceImpl`](../../src/main/java/com/tramtruyen/service/impl/ChapterServiceImpl.java) đã kiểm tra truyện tồn tại, số chương trùng và lưu chương.
- [`NovelRepository`](../../src/main/java/com/tramtruyen/repository/NovelRepository.java) và [`ChapterRepository`](../../src/main/java/com/tramtruyen/repository/ChapterRepository.java) tiếp tục là lớp persistence; không cần truy cập trực tiếp từ controller.
- [`ChapterForm`](../../src/main/java/com/tramtruyen/dto/ChapterForm.java) là DTO phù hợp để nhận số chương, tiêu đề và nội dung; không cần đổi entity/database chỉ để hỗ trợ định dạng.

### Security

- [`SecurityConfig`](../../src/main/java/com/tramtruyen/security/SecurityConfig.java) hiện giới hạn `/admin/**` bằng `ROLE_ADMIN`, không phù hợp với đặc tả các use case quản lý truyện/chương dành cho cả Staff và Admin.
- [`CustomAuthenticationSuccessHandler`](../../src/main/java/com/tramtruyen/security/CustomAuthenticationSuccessHandler.java) hiện ưu tiên saved request hoặc `/`. Cần bổ sung fallback theo quyền: Staff/Admin về `/admin/novels` khi không có đích đến hợp lệ; các role khác vẫn về `/`.
- [`CustomUserDetails`](../../src/main/java/com/tramtruyen/security/CustomUserDetails.java) là nguồn authority để kiểm tra quyền; không đưa logic quyền vào Thymeleaf hoặc controller.

## 3. Implementation Plan

### Security and navigation

1. Đổi matcher `/admin/**` thành quyền `ROLE_ADMIN` hoặc `ROLE_STAFF` bằng API Spring Security phù hợp, giữ nguyên các matcher khác.
2. Trong success handler, chỉ dùng fallback `/admin/novels` khi authentication có một trong hai authority nội dung và không có saved request/redirect hợp lệ.
3. Giữ kiểm tra authorization ở SecurityFilterChain; template chỉ hiển thị UI, không được xem là cơ chế bảo mật.
4. Bổ sung/điều chỉnh test security hoặc unit test cho các trường hợp Admin, Staff, Member và redirect đã lưu.

### Chapter editor

1. Giữ `ChapterAdminController -> ChapterService -> ChapterRepository` không thay đổi trách nhiệm.
2. Cải thiện [`admin/chapters/form.html`](../../src/main/resources/templates/admin/chapters/form.html) bằng editor nội bộ không thêm dependency build bắt buộc:
   - thanh công cụ cho đậm, nghiêng, gạch chân, tiêu đề/đoạn, danh sách, căn lề, màu/chữ và xóa định dạng;
   - vùng soạn thảo hỗ trợ thao tác bàn phím, paste an toàn và trạng thái có thể gửi form;
   - nút chuyển đổi giữa chế độ trực quan và HTML/text để người dùng vẫn kiểm soát được nội dung.
3. Đồng bộ nội dung editor vào `ChapterForm.content` trước khi submit; nếu chọn lưu HTML, phải giới hạn tag/attribute được phép và không chèn script/event handler.
4. Đặt JavaScript/CSS riêng trong `src/main/resources/static` hoặc fragment phù hợp, tránh nhúng một khối script lớn trong template nếu có thể tái sử dụng.
5. Giữ validation `@NotBlank` và xử lý lỗi duplicate ở controller/service hiện có. Không đưa quy tắc số chương vào JavaScript thay cho service.
6. Xác nhận nội dung đọc chương hiện tại render đúng định dạng sau thay đổi; nếu đang dùng `th:text`, cần có thiết kế render an toàn trước khi đổi sang HTML.

### Novel form / CMS UX

1. Không tạo controller/service mới; giữ nút và đường dẫn hiện tại.
2. Chỉ điều chỉnh nhãn/điều hướng nếu cần để Staff nhìn thấy và dùng được cùng CMS.
3. Không thay đổi schema, upload cover, category assignment hoặc nghiệp vụ trạng thái ngoài phạm vi yêu cầu.

## 4. Database and Transaction Impact

- Không cần migration database nếu nội dung chương tiếp tục lưu trong cột hiện tại.
- Không thay đổi transaction boundary của `ChapterServiceImpl` hoặc `NovelServiceImpl`.
- Nếu chọn lưu HTML, dữ liệu được xem là rich text; cần kiểm tra tương thích với cột hiện tại và quy tắc hiển thị trước khi triển khai.

## 5. OOP / SOLID Validation

- **SRP:** Controller vẫn chỉ bind request/điều hướng; Service giữ nghiệp vụ; editor chỉ chịu trách nhiệm trải nghiệm nhập liệu.
- **OCP:** Tách editor behavior thành asset/fragment có thể mở rộng toolbar mà không làm phình controller/service.
- **LSP:** Không thêm hierarchy mới; giữ các service interface hiện tại.
- **ISP:** Không mở rộng interface service bằng các phương thức UI/editor.
- **DIP:** Security handler phụ thuộc `Authentication`; persistence tiếp tục được inject qua service, không tự khởi tạo repository.
- **Encapsulation:** Không expose entity trực tiếp cho form mới; tiếp tục dùng `ChapterForm`.

## 6. Risks and Decisions Requiring Review

1. **Định dạng lưu trữ:** editor có thể lưu HTML an toàn hoặc chuyển về Markdown/plain text. Kế hoạch ưu tiên HTML có whitelist để giữ định dạng giống Word, nhưng cần xác nhận trước khi code.
2. **Thư viện editor:** ưu tiên không thêm npm/build dependency; nếu dùng thư viện CDN thì phải cân nhắc CSP, offline deployment và supply-chain. Kế hoạch mặc định dùng editor nhẹ bằng tài sản tĩnh của project.
3. **Sanitization:** render rich text cần whitelist cả lúc lưu và lúc hiển thị; không được dùng `th:utext` cho dữ liệu chưa được sanitize.
4. **Redirect đã lưu:** saved request hợp lệ vẫn được ưu tiên; `/admin/novels` chỉ là fallback sau đăng nhập, tránh phá luồng người dùng truy cập URL cụ thể.

## 7. Pre-coding Checklist

- [x] Function và actor đã xác định.
- [x] Use case liên quan đã đối chiếu với `danh_sach_chuc_nang.txt`.
- [x] Controller, Service, Repository, Entity/DTO và Security đã được kiểm tra.
- [x] Không cần tạo lớp trùng trách nhiệm.
- [x] Không cần thay đổi database nếu giữ định dạng cột hiện tại.
- [x] Authorization nằm ở Spring Security, không dựa vào ẩn UI.
- [x] Transaction và exception flow hiện tại được giữ lại.
- [ ] Cần người dùng duyệt lựa chọn định dạng lưu trữ và hướng triển khai editor trước khi sửa code.

## 8. Validation After Approval

1. Chạy test service/security liên quan.
2. Build bằng Maven.
3. Kiểm tra thủ công: Staff login -> `/admin/novels`; Admin login -> `/admin/novels`; Member không truy cập được `/admin/**`.
4. Tạo/sửa chương với toolbar, submit lỗi validation, số chương trùng và nội dung có markup độc hại.
5. Kiểm tra trang đọc chương không thực thi script và vẫn hiển thị đúng nội dung hợp lệ.

**Trạng thái:** Chưa triển khai code. Chờ phản hồi/duyệt kế hoạch.
