# Architecture Documentation

Dự án **Center Management** được thiết kế theo kiến trúc Monolith (nguyên khối) kết hợp với Server-Side Rendering (SSR) sử dụng Thymeleaf, Spring Boot và cơ sở dữ liệu MySQL.

## 1. System Context Diagram (C4 Model)
Sơ đồ dưới đây mô tả ngữ cảnh hệ thống, cách người dùng tương tác với hệ thống Quản Lý Trung Tâm.

```mermaid
C4Context
    title System Context Diagram - Center Management

    Person(admin, "Admin", "Quản trị viên hệ thống. Quản lý cài đặt, tài khoản, khóa học, và học phí.")
    Person(ministry, "Giáo vụ (Ministry)", "Quản lý lớp học, điểm số, học viên, lịch học.")
    Person(lecturer, "Giảng viên (Lecturer)", "Xem lớp học được phân công, cập nhật điểm, sổ đầu bài.")
    Person(student, "Học viên (Student)", "Xem lịch học, kết quả học tập và thông báo học phí.")

    System(centerManagement, "Center Management System", "Hệ thống quản lý trung tâm giáo dục cốt lõi.")
    System_Ext(emailSystem, "Gmail SMTP", "Gửi email thông báo (học phí, lịch học, quên mật khẩu).")
    System_Ext(storageSystem, "Supabase Storage", "Lưu trữ hình ảnh, tài liệu đính kèm.")

    Rel(admin, centerManagement, "Quản lý toàn diện")
    Rel(ministry, centerManagement, "Quản lý hoạt động hàng ngày")
    Rel(lecturer, centerManagement, "Giảng dạy & chấm điểm")
    Rel(student, centerManagement, "Theo dõi học tập")

    Rel(centerManagement, emailSystem, "Gửi email qua SMTP")
    Rel(centerManagement, storageSystem, "Lưu & lấy files")
```

## 2. Container Diagram
Kiến trúc bên trong của hệ thống Monolith.

```mermaid
C4Container
    title Container Diagram - Center Management

    Person(user, "User", "Admin / Ministry / Lecturer / Student")

    Container_Boundary(monolith, "Spring Boot Monolith") {
        Container(webApp, "Web Application", "Java, Spring MVC, Thymeleaf", "Cung cấp giao diện HTML được render từ server.")
        Container(security, "Security Layer", "Spring Security", "Xác thực (Authentication) và Phân quyền (Authorization).")
        Container(service, "Business Logic Layer", "Spring Service", "Chứa các quy tắc nghiệp vụ (Quản lý khóa học, học phí...).")
        Container(dataAccess, "Data Access Layer", "Spring Data JPA, Hibernate", "Tương tác với cơ sở dữ liệu.")
    }

    ContainerDb(database, "Database", "MySQL 8.0+", "Lưu trữ thông tin người dùng, khóa học, lớp học, học phí.")

    Rel(user, webApp, "Sử dụng trình duyệt (HTTPS/HTTP)")
    Rel(webApp, security, "Kiểm tra quyền truy cập")
    Rel(security, service, "Chuyển tiếp yêu cầu hợp lệ")
    Rel(service, dataAccess, "Đọc/Ghi dữ liệu")
    Rel(dataAccess, database, "SQL Queries")
```

## 3. Code & Component Architecture
Trong Spring Boot, dự án áp dụng mô hình thiết kế **Layered Architecture**:

1. **Controller Layer (`com.codegym.controller.*`)**:
   - Nhận HTTP Request từ client.
   - Trả về các file giao diện `.html` (Thymeleaf templates) hoặc chuyển hướng (Redirect).
   
2. **Service Layer (`com.codegym.service.*`)**:
   - Đảm nhận toàn bộ Business Logic.
   - Transaction Management (quản lý giao dịch) thông qua `@Transactional`.

3. **Data Access Layer (`com.codegym.repository.*`)**:
   - Sử dụng các Interface kế thừa `JpaRepository`.
   - Sinh các câu query tự động hoặc custom query bằng `@Query`.

4. **DTO & Mapping (`com.codegym.dto.*`, `com.codegym.mapper.*`)**:
   - Sử dụng **MapStruct** để map giữa Entity (Database) và DTO (Data Transfer Object).
   - Tách biệt dữ liệu Database ra khỏi Controller/View để bảo mật và dễ format.

## 4. Công nghệ bảo mật (Security Architecture)
- Sử dụng **Spring Security 6** (tương thích với Spring Boot 3.x/4.x).
- Session-based Authentication: Lưu trữ phiên đăng nhập.
- Phân quyền theo Role-based Access Control (RBAC): `ROLE_ADMIN`, `ROLE_MINISTRY`, `ROLE_LECTURER`, `ROLE_STUDENT`.
- Mật khẩu được mã hóa một chiều bằng `BCryptPasswordEncoder`.

## 5. Xử lý ngoại lệ (Exception Handling)
- **Global Controller Advice**: Sử dụng `@ControllerAdvice` để gom tất cả exception (như `NotFoundException`, `AccessDeniedException`) về một nơi và trả về trang báo lỗi thân thiện (404, 403, 500) thay vì stack trace thô.
