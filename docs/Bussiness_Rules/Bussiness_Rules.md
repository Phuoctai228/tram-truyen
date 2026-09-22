# Business Rules (Quy Tắc Nghiệp Vụ)

## 1. Vietnamese Version (Tiếng Việt)

<table style="width: 100%; border-collapse: collapse; font-family: 'Segoe UI', Arial, sans-serif; font-size: 14px; text-align: left; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
  <thead>
    <tr style="background-color: #f8f9fa; color: #212529; text-transform: uppercase; border-bottom: 2px solid #dee2e6;">
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 8%; text-align: center;">Mã BR</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 18%;">Tên Quy Tắc</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 44%;">Mô Tả / Công Thức</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 15%;">Đối Tượng (Actor)</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 15%;">Ghi Chú</th>
    </tr>
  </thead>
  <tbody>
    <!-- BR-01 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-01</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Quản lý số dư Coin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li><code>Coin Balance mới = Coin Balance hiện tại + Coin nạp - Coin sử dụng</code></li>
          <li>Số dư Coin của người dùng <strong>không được phép âm</strong>.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member, System</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Không hỗ trợ hoàn tiền hoặc rút tiền mặt.</td>
    </tr>
    <!-- BR-02 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-02</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Mở khóa chương VIP</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Điều kiện đọc: Chương phải là <em>Miễn phí</em> <strong>HOẶC</strong> <em>Đã được mở khóa</em>.</li>
          <li>Điều kiện thanh toán: <code>Coin Balance >= Giá chương VIP</code>.</li>
          <li>Sau thanh toán: <code>Coin Balance mới = Coin Balance - Giá chương VIP</code>.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Quyền đọc được lưu vĩnh viễn cho user, không cần mua lại.</td>
    </tr>
    <!-- BR-03 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-03</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Quy đổi Tiền sang Coin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li><code>Coin nhận được = Số tiền thanh toán × Tỷ lệ quy đổi (Tỉ giá)</code></li>
          <li>Tỉ giá này có thể được Admin điều chỉnh linh hoạt trong Cấu hình hệ thống.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">System, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Cổng thanh toán: VNPay / Momo.</td>
    </tr>
    <!-- BR-04 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-04</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Khóa tài khoản (Ban)</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Lần đầu vi phạm: Khóa 24 giờ.</li>
          <li>Các lần tiếp theo: Thời gian khóa gấp đôi lần trước đó.</li>
          <li>Thời gian khóa tối đa: 1 năm.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Admin, Member</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Admin có quyền chủ động mở khóa (Enable User) trước thời hạn.</td>
    </tr>
    <!-- BR-05 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-05</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Xử lý báo cáo/khiếu nại</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Quy trình đổi trạng thái: <code>Chưa xử lý (Pending) -> Đã xử lý (Processed)</code> hoặc <code>Từ chối (Rejected)</code>.</li>
          <li><strong>Yêu cầu lưu vết bắt buộc:</strong> Người xử lý, Thời gian xử lý, Quyết định, Lý do/Kết quả.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Staff, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Bắt buộc phải thông báo kết quả cho người gửi.</td>
    </tr>
    <!-- BR-06 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-06</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Quyền xuất bản nội dung</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Chỉ Staff và Admin mới được phép tạo, cập nhật, xuất bản truyện và chương.</li>
          <li>Nội dung xuất bản phải đảm bảo bản quyền và vượt qua khâu kiểm duyệt trước khi hiển thị (Public).</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Staff, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Guest/Member không có quyền đăng nội dung.</td>
    </tr>
    <!-- BR-07 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-07</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Ràng buộc xóa Thể loại</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        Hệ thống <strong>từ chối</strong> thao tác Xóa Thể loại (Category) nếu trong CSDL vẫn còn các Truyện (Novels) đang được gán (mapping) với Thể loại đó.
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Đảm bảo tính toàn vẹn dữ liệu (Constraint).</td>
    </tr>
    <!-- BR-08 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-08</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Xóa nội dung (Soft Delete)</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        Hành động xóa Truyện (Novel) hoặc Chương (Chapter) là xóa mềm.
        <br/>Hệ thống cập nhật cờ <code>is_deleted = true</code> (ẩn khỏi giao diện Public) thay vì xóa vật lý khỏi CSDL.
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Staff, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Bảo vệ dữ liệu lịch sử nạp/đọc của độc giả.</td>
    </tr>
    <!-- BR-09 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-09</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Tính điểm đánh giá</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Mỗi Member chỉ được phép đánh giá (Rating) <strong>1 lần duy nhất</strong> cho mỗi bộ truyện.</li>
          <li>Hệ thống tự động tính lại điểm trung bình <code>average_rating</code> ngay sau mỗi lượt đánh giá mới.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member, System</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Cơ chế chống spam vote.</td>
    </tr>
    <!-- BR-10 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-10</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Thưởng điểm danh</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        Mỗi Member đăng nhập và thực hiện thao tác Điểm danh (Check-in) trong khoảng thời gian từ <code>00:00:00</code> đến <code>23:59:59</code> mỗi ngày sẽ nhận được Coin thưởng tự động.
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Chỉ áp dụng 1 lần/ngày.</td>
    </tr>
  </tbody>
</table>

<br/><br/>

## 2. English Version

<table style="width: 100%; border-collapse: collapse; font-family: 'Segoe UI', Arial, sans-serif; font-size: 14px; text-align: left; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
  <thead>
    <tr style="background-color: #f8f9fa; color: #212529; text-transform: uppercase; border-bottom: 2px solid #dee2e6;">
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 8%; text-align: center;">Rule ID</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 18%;">Rule Name</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 44%;">Description / Formula</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 15%;">Actors</th>
      <th style="border: 1px solid #dee2e6; padding: 12px; width: 15%;">Notes</th>
    </tr>
  </thead>
  <tbody>
    <!-- BR-01 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-01</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Coin Balance Management</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li><code>New Coin Balance = Current Coin Balance + Deposited Coin - Used Coin</code></li>
          <li>User's Coin balance <strong>must not be negative</strong>.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member, System</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Non-refundable, no cash withdrawals supported.</td>
    </tr>
    <!-- BR-02 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-02</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Unlock VIP Chapter</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Read condition: Chapter must be <em>Free</em> <strong>OR</strong> <em>Unlocked</em>.</li>
          <li>Payment condition: <code>Coin Balance >= VIP Chapter Price</code>.</li>
          <li>After payment: <code>New Coin Balance = Coin Balance - VIP Chapter Price</code>.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Reading rights are granted permanently, no repurchase required.</td>
    </tr>
    <!-- BR-03 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-03</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Currency to Coin Conversion</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li><code>Received Coin = Paid Amount × Exchange Rate</code></li>
          <li>Exchange rate can be flexibly adjusted by Admin in System Settings.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">System, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Payment gateways: VNPay / Momo.</td>
    </tr>
    <!-- BR-04 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-04</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Account Ban</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>First violation: 24-hour ban.</li>
          <li>Subsequent violations: Ban duration doubles the previous one.</li>
          <li>Maximum ban duration: 1 year.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Admin, Member</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Admin can manually unlock the account before the ban expires.</td>
    </tr>
    <!-- BR-05 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-05</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Report & Complaint Handling</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Status workflow: <code>Pending -> Processed</code> or <code>Rejected</code>.</li>
          <li><strong>Mandatory tracking:</strong> Processor, Processing Time, Decision, Reason/Result.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Staff, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Must notify the sender about the resolution.</td>
    </tr>
    <!-- BR-06 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-06</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Content Publishing Rights</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Only Staff and Admin can create, update, and publish novels/chapters.</li>
          <li>Content must be copyright-verified and approved before becoming Public.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Staff, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Guests and Members cannot post content.</td>
    </tr>
    <!-- BR-07 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-07</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Category Deletion Constraint</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        The system <strong>rejects</strong> the deletion of a Category if there are still Novels mapped/assigned to it.
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Ensures data integrity (Database Constraint).</td>
    </tr>
    <!-- BR-08 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-08</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Content Soft Delete</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        Deleting a Novel or Chapter is a soft delete.
        <br/>The system updates the flag <code>is_deleted = true</code> (hidden from the Public view) instead of physically deleting the record from the database.
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Staff, Admin</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Protects user reading and transaction histories.</td>
    </tr>
    <!-- BR-09 -->
    <tr>
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-09</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Rating Calculation</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        <ul style="margin: 0; padding-left: 18px;">
          <li>Each Member can rate (1-5 stars) <strong>only ONCE</strong> per novel.</li>
          <li>The system automatically recalculates the <code>average_rating</code> immediately after a new rating is submitted.</li>
        </ul>
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member, System</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Anti-spam voting mechanism.</td>
    </tr>
    <!-- BR-10 -->
    <tr style="background-color: #fdfdfe;">
      <td style="border: 1px solid #dee2e6; padding: 12px; text-align: center; font-weight: bold; color: #0d6efd;">BR-10</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; font-weight: 500;">Daily Check-in Reward</td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">
        Members who log in and complete the Check-in action between <code>00:00:00</code> and <code>23:59:59</code> daily will receive automatic Coin rewards.
      </td>
      <td style="border: 1px solid #dee2e6; padding: 12px;">Member</td>
      <td style="border: 1px solid #dee2e6; padding: 12px; color: #6c757d;">Applicable only once per day.</td>
    </tr>
  </tbody>
</table>
