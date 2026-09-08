# Trạm Truyện - Nền tảng đọc truyện chữ trực tuyến

## 1. Project Introduction
Trạm Truyện là nền tảng đọc truyện chữ trực tuyến, được phát triển trong khuôn khổ môn học SWP391. Hệ thống cung cấp các chức năng tương tự như các website đọc truyện hiện nay, bao gồm đọc truyện, quản lý tủ sách, đăng truyện, bình luận, và kiểm duyệt.

## 2. Features
- **Guest**: Tìm kiếm, lọc và đọc truyện.
- **Member**: Đăng ký, đăng nhập, quản lý tủ sách, lịch sử đọc, bình luận, báo lỗi chương và đăng tải truyện mới.
- **Staff**: Kiểm duyệt truyện, kiểm duyệt chương, quản lý bình luận và xử lý báo cáo lỗi.
- **Admin**: Quản lý người dùng, phân quyền, quản lý thể loại và theo dõi dashboard hệ thống.

## 3. Technology Stack
- **Backend**: Java 17/21, Spring Boot 3.x, Spring MVC, Spring Data JPA, Hibernate, Spring Security.
- **Frontend**: HTML5, CSS3, JavaScript, Tailwind CSS, Thymeleaf.
- **Database**: PostgreSQL.
- **Image Storage**: Cloudinary.
- **Deployment**: Docker, Docker Compose.

## 4. Architecture
Dự án sử dụng kiến trúc **MVC + Multi-layer Architecture** theo chuẩn của SWP391.
Luồng xử lý chính: `Browser -> Spring MVC Controller -> Service -> Repository -> PostgreSQL`.

## 5. Database
Dự án sử dụng **PostgreSQL** với cấu trúc bao gồm 12-14 bảng (users, roles, novels, chapters, categories, bookshelf, comments, v.v.). Cơ sở dữ liệu đáp ứng chuẩn hóa, có đầy đủ khóa chính (PK) và khóa ngoại (FK).

## 6. Installation

### Yêu cầu hệ thống:
- Cài đặt **Docker Desktop** (Để chạy local thông qua Container).
- Cài đặt **Git**.

### Thiết lập Environment Variables (`.env`)
Tạo file `.env` ở thư mục gốc của dự án để thiết lập biến môi trường:
```env
CLOUDINARY_URL=cloudinary://<API_KEY>:<API_SECRET>@<CLOUD_NAME>
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123456
POSTGRES_DB=tramtruyen
```

## 7. Running Instructions

### Bước 1: Tắt PostgreSQL mặc định trên máy (Chỉ làm 1 lần duy nhất)
*Lý do: Để tránh bị trùng cổng (port 5432) với Database PostgreSQL trong Docker.*
1. Bấm nút Windows, gõ tìm và mở ứng dụng **Services**.
2. Tìm đến dòng nào bắt đầu bằng chữ **postgresql...**
3. Chuột phải vào nó > Chọn **Properties**.
4. Chỉnh **Startup type** thành `Manual`.
5. Bấm nút **Stop** ở phần Service status.
6. Bấm **OK** để lưu lại.

### Bước 2: Chạy Project
- Mở ứng dụng **Docker Desktop** và đợi icon chuyển sang màu xanh (Báo hiệu Docker đã sẵn sàng).
- Mở thư mục chứa project (`tram-truyen`).
- Đè phím `Shift` + Click chuột phải vào khoảng trống trong thư mục > Chọn **Open in Terminal** (hoặc **Open PowerShell window here**).
- Tại màn hình dòng lệnh đen, **chọn copy và chạy 1 trong các trường hợp sau** tuỳ vào mục đích của bạn:

**1. Chạy lần đầu tiên HOẶC Muốn xoá sạch toàn bộ dữ liệu cũ để làm lại từ đầu:**
```bash
docker compose down -v
docker compose up --build -d
```

**2. Chạy bình thường hằng ngày (Nhanh nhất):**
```bash
docker compose up -d
```

**3. Khi Database có thay đổi cấu trúc (Cập nhật Schema):**
- **Cách A: Cho phép xoá hết dữ liệu cũ (Cách đơn giản nhất)**
  Bạn dùng lệnh xoá hoàn toàn Volume cũ rồi build lại từ đầu:
  ```bash
  docker compose down -v
  docker compose up --build -d
  ```
- **Cách B: Giữ lại dữ liệu hiện tại (Không muốn xoá)**
  Bạn phải tự cập nhật DB thủ công (chạy script update SQL). Sau khi update DB xong, bạn chạy lệnh build lại bình thường:
  ```bash
  docker compose up --build -d
  ```

### Bước 3: Cách truy cập sau khi chạy thành công
- **Trang chủ Website (Frontend)**: Truy cập [http://localhost:8080](http://localhost:8080)
- **Database (Xem dữ liệu bằng pgAdmin/DBeaver)**:
  1. Mở phần mềm **pgAdmin** (biểu tượng con voi).
  2. Chuột phải vào chữ **Servers** (cột bên trái) > **Register** > **Server...**
  3. Tab **General**: Mục Name nhập là `docker`.
  4. Tab **Connection** nhập như sau:
     - Host name/address: `localhost`
     - Port: `5432`
     - Maintenance database: `postgres`
     - Username: `postgres`
     - Password: `123456`
     - Save password?: Bật lên.
  5. Bấm **Save**. Lúc này ở cột trái sẽ xuất hiện server tên `docker`, mở ra bạn sẽ thấy database tên `tramtruyen` để sử dụng.

## 8. Default Accounts
*Hệ thống sẽ được khởi tạo với các tài khoản mặc định thông qua file `database/seed.sql`.*
- **Admin**: `admin@tramtruyen.com` / `123456`
- **Staff**: `staff@tramtruyen.com` / `123456`
- **Member**: `member@tramtruyen.com` / `123456`

## 9. Project Team
- **Member 1**: Novel Management
- **Member 2**: Chapter & Reader Management
- **Member 3**: Authentication & User Management
- **Member 4**: Category & Search
- **Member 5**: Interaction & Moderation

## 10. Branch Strategy
Tuyệt đối không ai được push trực tiếp vào branch `main`. Quy trình chia nhánh:
- `main`: Chứa bản release chính thức.
- `feature/[Member-Name]`: Chứa code chức năng do từng thành viên thực hiện. Ví dụ: `feature/phuoctai`.

## 11. Development Workflow
1. Tạo **GitHub Issue** cho tính năng bạn sẽ làm (VD: `[M1-F01] Create Novel`).
2. Cập nhật code mới nhất từ `main` và chuyển sang nhánh làm việc riêng của bạn (VD: `feature/phuoctai`).
3. Code chức năng, sử dụng Commit Convention chuẩn (`feat: ...`, `fix: ...`, `docs: ...`).
4. Đẩy code lên nhánh của mình trên GitHub và tạo **Pull Request (PR)** vào nhánh `main`.
5. Yêu cầu một thành viên khác thực hiện **Code Review**.
6. PR được review và test thành công thì mới được phép Merge.

## 12. Test Information
- **Mục tiêu**: Đạt 30-40 test cases (Coverage > 80%).
- Các kịch bản kiểm thử xoay quanh: Xác thực tài khoản, Quy trình đăng truyện, Quản lý chương, Tương tác bình luận, Quản lý tủ sách, v.v.
- Được thực hiện kết hợp giữa AI Testing và Manual Testing.

---

## 13. Hướng dẫn dọn dẹp và giải phóng dung lượng Docker
*Lý do: Dùng Docker lâu ngày ổ C: sẽ bị đầy. Quá trình này giúp xóa các rác thừa và thu nhỏ ổ cứng ảo của Docker.*

### Phần 1: Dọn rác bên trong Docker
Docker sẽ xóa TẤT CẢ mọi thứ đang bị tắt. Do đó chúng ta cần "bảo vệ" project này bằng cách bật nó lên trước khi dọn dẹp.
1. Mở Docker Desktop, vào tab **Containers**.
2. Bấm nút ▶ (Play) để khởi động toàn bộ các container của project này (app, db). Đợi chúng sáng đèn xanh hết.
3. Mở Terminal (CMD), chạy lần lượt 2 lệnh sau:
   ```bash
   docker system df
   docker system prune -a --volumes
   ```
4. Hệ thống sẽ hỏi `Are you sure you want to continue?`, bạn gõ `y` và nhấn Enter.
5. Sau khi xoá xong, **Tắt hoàn toàn Docker Desktop** (Chuột phải vào icon con cá voi ở góc phải dưới cùng màn hình > Chọn `Quit Docker Desktop`).

### Phần 2: Thu nhỏ dung lượng ổ cứng ảo (Nâng cao)
1. Mở thư mục (File Explorer), copy và dán đường dẫn này vào thanh địa chỉ bên trên rồi Enter:
   ```cmd
   %localappdata%\Docker\wsl\disk
   ```
2. Bạn sẽ thấy 1 file tên là `docker_data.vhdx`. Bấm **Shift + Chuột phải** vào file đó > Chọn **Copy as path**.
3. Mở lại Terminal (CMD), chạy lần lượt 2 lệnh:
   ```bash
   wsl --shutdown
   diskpart
   ```
4. Nếu Windows hỏi có cho phép chạy không, hãy chọn **Yes** (Allow). Một cửa sổ mới màu đen tên là `DISKPART` sẽ hiện lên.
5. Tại cửa sổ mới này, bạn copy lần lượt từng lệnh dưới đây dán vào và Enter. 
   *(Lưu ý: Thay chữ `[DẪN_PATH_VÀO_ĐÂY]` bằng đường dẫn bạn vừa Copy ở bước 2)*

   ```cmd
   select vdisk file=[DẪN_PATH_VÀO_ĐÂY]
   attach vdisk readonly
   compact vdisk
   detach vdisk
   exit
   ```
   **Ví dụ câu lệnh đúng sẽ trông như thế này:** 
   `select vdisk file="C:\Users\anhkc\AppData\Local\Docker\wsl\disk\docker_data.vhdx"`
