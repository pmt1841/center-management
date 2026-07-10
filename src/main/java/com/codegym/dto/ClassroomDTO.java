package com.codegym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassroomDTO {
    private Long id;

    @NotBlank(message = "Tên lớp không được để trống")
    private String className;

    @NotNull(message = "Vui lòng chọn môn học")
    private Long courseId;

    private Long lecturerId;
}
