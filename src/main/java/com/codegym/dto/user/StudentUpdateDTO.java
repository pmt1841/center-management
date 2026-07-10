package com.codegym.dto.user;

import com.codegym.model.user.student.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentUpdateDTO {

    private Long id;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String studentCode;

    @NotNull(message = "Trạng thái không được để trống")
    private StudentStatus studentStatus;

    private Long classroomId;
}
