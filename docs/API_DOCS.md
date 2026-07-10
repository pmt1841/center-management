# Tài Liệu API & Routes (API & Routes Reference)

Do dự án **Center Management** sử dụng kiến trúc Server-Side Rendering (SSR) với **Thymeleaf**, đa số các endpoint (routes) sẽ trả về giao diện HTML (Views) thay vì dữ liệu JSON như RESTful API thông thường. Tuy nhiên, việc nắm vững các đường dẫn (URL Routes) là vô cùng quan trọng để điều hướng hệ thống.

Dưới đây là danh sách các Routes chính trong hệ thống, được phân chia theo từng Module (dựa trên các Controller hiện có).

## 1. Authentication Module (`LoginController`)
Module chịu trách nhiệm xử lý đăng nhập, xác thực và khôi phục mật khẩu.
- `GET /login` : Hiển thị trang đăng nhập.
- `GET /forgot-password` : Hiển thị trang quên mật khẩu.
- `POST /forgot-password` : Xử lý gửi email khôi phục mật khẩu.
- `POST /logout` : Đăng xuất (Được xử lý ngầm bởi Spring Security).

## 2. Dashboard Module (`DashboardController`)
Các trang chủ/tổng quan hiển thị thông tin riêng biệt cho từng loại tài khoản (Role).
- `GET /dashboard` : Route chuyển hướng chung, tự động redirect người dùng dựa trên quyền hạn.
- `GET /admin/dashboard` : Dashboard dành cho Admin (Thống kê doanh thu, số lượng người dùng).
- `GET /ministry/dashboard` : Dashboard dành cho Giáo vụ.
- `GET /lecturer/{lecturerCode}/dashboard` : Dashboard dành cho Giảng viên.
- `GET /student/{studentCode}/dashboard` : Dashboard dành cho Học viên.

## 3. Quản lý Tài Khoản & Người Dùng (`UserController`, `AdminController`, `ProfileController`)
Quản trị hệ thống, quản lý thông tin tài khoản nội bộ và cá nhân.
- `GET /users`, `GET /users/` : Danh sách tài khoản người dùng (Dành cho Admin).
- `GET /users/create` : Giao diện tạo mới tài khoản.
- `POST /users/create` : Xử lý lưu tài khoản mới.
- `GET /users/{id}/edit` : Giao diện chỉnh sửa tài khoản.
- `POST /users/{id}/edit` : Xử lý cập nhật tài khoản.
- `GET /admin/settings` : Cài đặt hệ thống (Admin).
- `GET /admin/system-logs` : Xem lịch sử hoạt động hệ thống.
- `GET /profile` : Xem thông tin cá nhân của người dùng đang đăng nhập.
- `POST /profile/update` : Cập nhật thông tin cá nhân.
- `POST /profile/change-password` : Đổi mật khẩu.

## 4. Quản lý Sinh Viên & Học Viên (`StudentController`)
Quản lý hồ sơ, điểm số và thông tin học tập của học viên.
- `GET /students` : Danh sách học viên.
- `GET /students/{studentCode}/edit` : Giao diện chỉnh sửa thông tin học viên.
- `POST /students/{studentCode}/update-info` : Cập nhật thông tin cá nhân học viên.
- `POST /students/{studentCode}/update-scores` : Cập nhật điểm số.
- `POST /students/{studentCode}/add-diary` : Thêm nhận xét, nhật ký học tập.
- `POST /students/{studentCode}/delete` : Xóa (hoặc vô hiệu hóa) học viên.
- `POST /students/{studentCode}/restore` : Khôi phục học viên.

## 5. Quản lý Giảng Viên (`LecturerController`)
Các chức năng dành riêng cho vai trò Giảng viên.
- `GET /lecturer/students` : Danh sách học viên thuộc lớp của giảng viên quản lý.
- `GET /lecturer/classrooms` : Danh sách các lớp học giảng viên đang phụ trách.

## 6. Quản lý Khóa Học & Lớp Học (`CourseController`, `ClassroomController`)
- `GET /courses` : Danh sách khóa học.
- `GET /courses/create` : Trang tạo khóa học.
- `GET /classrooms` : Danh sách lớp học.
- *(Và các endpoint CRUD tương tự dành cho Lớp học & Khóa học)*

## 7. Quản lý Học Phí (`TuitionController`)
Quản lý việc thu học phí, gia hạn học phí và lịch sử thanh toán.
- `GET /admin/tuitions` : Danh sách học phí tổng quan.
- `GET /admin/tuitions/students/{studentCode}/pay` : Giao diện thu tiền học phí cho một học viên cụ thể.
- `POST /admin/tuitions/process-payment` : Xử lý giao dịch thanh toán học phí.
- `POST /admin/tuitions/setting-day` : Thiết lập ngày thu học phí định kỳ.
- `GET /admin/tuitions/students/{code}/detail` : Xem chi tiết lịch sử đóng học phí của học viên.

## 8. Quản lý Mã Giảm Giá (`DiscountController`)
Quản lý các chương trình khuyến mãi, voucher áp dụng khi đóng học phí.
- `GET /admin/discounts` : Danh sách mã giảm giá.
- `POST /admin/discounts/create` : Tạo mã giảm giá mới.
- `GET /admin/discounts/edit/{id}` : Giao diện sửa mã giảm giá.
- `POST /admin/discounts/edit/{id}` : Xử lý sửa mã giảm giá.
- `POST /admin/discounts/toggle/{id}` : Bật/Tắt (Kích hoạt/Hủy) mã giảm giá.

---
**Ghi chú dành cho Developer mới**:
- Các request thay đổi dữ liệu (Create, Update, Delete) luôn sử dụng method `POST` (Do hạn chế của HTML form truyền thống không hỗ trợ PUT/DELETE trực tiếp, trừ khi cấu hình HiddenHttpMethodFilter).
- Các routes bắt đầu bằng `/admin/*` mặc định được bảo vệ bởi Spring Security và yêu cầu người dùng phải có `ROLE_ADMIN`.
- Thông số `{id}` hay `{studentCode}` là các **Path Variable**, chúng sẽ được tự động map vào tham số của Controller bằng `@PathVariable`.
