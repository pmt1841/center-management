# Hướng Dẫn Dành Cho Lập Trình Viên Mới (Developer Onboarding Guide)

Chào mừng bạn đến với dự án **Center Management** (Quản Lý Trung Tâm). Tài liệu này cung cấp cho bạn những thông tin cần thiết nhất để có thể setup môi trường, hiểu được kiến trúc dự án và bắt tay vào viết code ngay lập tức.

## 1. Giới thiệu dự án (Project Overview)
- **Tên dự án**: Center Management.
- **Mục tiêu**: Xây dựng hệ thống quản lý trung tâm giáo dục, bao gồm phân quyền người dùng, quản lý lớp học/khóa học, học phí và thống kê doanh thu.
- **Tech Stack chính**: Java 17, Spring Boot 3.x/4.x, Spring Security, MySQL, MapStruct, Thymeleaf (dành cho server-side rendering).

## 2. Chuẩn bị môi trường (Prerequisites)
Để chạy được project này, máy tính của bạn cần có:
- **Java Development Kit (JDK) 17**: Bắt buộc.
- **IDE**: Khuyên dùng IntelliJ IDEA (Ultimate hoặc Community) hoặc Eclipse.
- **Database**: MySQL Server (phiên bản 8.0+).
- **Git** để quản lý mã nguồn.

## 3. Cài đặt và Chạy ứng dụng (Setup & Run)
1. **Clone mã nguồn**:
   ```bash
   git clone https://github.com/pmt1841/center-management.git
   cd center-management
   ```

2. **Cấu hình biến môi trường**: 
   - Tìm file `.env.example` ở thư mục gốc dự án, copy và đổi tên thành `.env`.
   - Cập nhật thông tin MySQL của bạn vào file `.env` (`DB_USERNAME`, `DB_PASSWORD`). Hệ thống sẽ tự động tạo database nếu chưa có nhờ vào tham số `createDatabaseIfNotExist=true` trong URL.
   - Cập nhật thông tin Supabase (lưu trữ file) và Gmail SMTP (cho chức năng gửi mail) nếu bạn cần test các tính năng đó.

3. **Cài đặt dependencies và chạy ứng dụng**:
   - Mở terminal tại thư mục gốc của dự án.
   - Chạy lệnh Gradle (nếu dùng terminal): 
     ```bash
     ./gradlew bootRun   # Trên Linux/Mac
     gradlew.bat bootRun # Trên Windows
     ```
   - *Lưu ý*: Hoặc bạn có thể bấm nút Run trực tiếp trên IntelliJ IDEA thông qua class chứa hàm `main`.
   - Ứng dụng sẽ chạy tại: `http://localhost:8080`.

## 4. Cấu trúc thư mục (Project Structure)
Dự án được tổ chức theo kiến trúc Controller - Service - Repository quen thuộc của Spring Boot. Các package quan trọng nằm trong `src/main/java/com/codegym/`:

- `config/`: Chứa các class cấu hình hệ thống (Spring Security, Cors, Beans...).
- `controller/`: Nơi định nghĩa các endpoint (API) và route trả về giao diện.
- `dto/`: (Data Transfer Object) Các class chứa dữ liệu giao tiếp giữa client và server.
- `entity/`: Các class được ánh xạ 1-1 với các bảng trong MySQL thông qua Hibernate (JPA).
- `mapper/`: Chứa các interface của MapStruct, dùng để chuyển đổi tự động giữa `Entity` và `DTO`.
- `repository/`: Các interface kế thừa từ `JpaRepository` dùng để tương tác với Database.
- `service/`: Nơi chứa toàn bộ business logic. Bao gồm các Interface và các class `Impl` cài đặt chi tiết.
- `exception/`: Chứa các class xử lý ngoại lệ (Global Exception Handler).

Đối với giao diện Frontend:
- `src/main/resources/templates/`: Chứa các file HTML sử dụng cú pháp Thymeleaf.
- `src/main/resources/static/`: Chứa CSS, JavaScript, hình ảnh, Bootstrap...

## 5. Quy trình phát triển tính năng mới (Development Workflow)
Để code một tính năng mới một cách chuẩn xác, bạn hãy thực hiện theo thứ tự sau (Ví dụ với tính năng Quản lý Sinh viên - Student):

1. **Database Layer (Entity & Repo)**: 
   - Khai báo class `Student` trong thư mục `entity`.
   - Tạo interface `StudentRepository` extends `JpaRepository` trong thư mục `repository`.
2. **Data Transfer Layer (DTO & Mapper)**: 
   - Khai báo class `StudentDto` trong thư mục `dto`.
   - Khai báo `StudentMapper` interface trong thư mục `mapper`, gắn `@Mapper(componentModel = "spring")`. MapStruct sẽ tự động sinh code chuyển đổi khi build.
3. **Business Logic Layer (Service)**: 
   - Tạo interface `IStudentService` trong thư mục `service`.
   - Tạo class `StudentServiceImpl` implements interface trên trong thư mục `service/impl`. Tại đây, gọi tới `StudentRepository` để xử lý logic.
4. **Presentation Layer (Controller & View)**: 
   - Viết các API hoặc Endpoint trả về view tại `StudentController` ở thư mục `controller`.
   - Thiết kế giao diện Thymeleaf tương ứng bên trong `src/main/resources/templates/`.

## 6. Quy chuẩn Code (Coding Conventions)
Để đảm bảo source code đồng nhất và dễ maintain, team áp dụng các quy chuẩn sau:
- **Naming**: Tên biến, hàm dùng `camelCase`. Tên class, interface dùng `PascalCase`.
- **Lombok**: Tích cực sử dụng các annotation của Lombok như `@Data`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` để giảm tải mã boilerplate.
- **Dependency Injection**: 
  - Khuyến khích dùng Dependency Injection thông qua Constructor thay vì dùng `@Autowired` trực tiếp trên field. 
  - *Cách làm nhanh*: Thêm `@RequiredArgsConstructor` của Lombok lên đầu class và khai báo các bean cần inject dưới dạng `private final`.

---
**Bạn đã sẵn sàng để bắt đầu code!** 🎉
Hãy chọn một ticket, tạo branch mới từ nhánh chính (ví dụ: `feature/ten-tinh-nang`) và code thôi. 
Nếu gặp bất kỳ vấn đề gì về setup hoặc lỗi không xác định, đừng ngần ngại liên hệ team leader hoặc các thành viên khác để được hỗ trợ.
