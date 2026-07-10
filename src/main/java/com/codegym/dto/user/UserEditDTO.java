package com.codegym.dto.user;

import com.codegym.model.user.student.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UserEditDTO {
    // --- CÁC TRƯỜNG CHUNG (AppUser) ---
    private Long id;
    private String email;

    @NotBlank(message = "Tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có độ dài từ 10-11 chữ số")
    private String phoneNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String address;

    @NotBlank(message = "Căn cước công dân không được để trống")
    private String identity;

    private String roleDescription;

    private String avatarUrl;

    // --- CÁC TRƯỜNG RIÊNG (Sinh viên) ---
    private String studentCode;
    private StudentStatus studentStatus;
    private Long classroomId;
}
