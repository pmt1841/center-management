package com.codegym.dto.user;

import com.codegym.model.tuition.TuitionPayment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class StudentDebtDTO {
    private Long studentId;
    private String studentCode;
    private String fullName;
    private String className;
    private Integer unpaidMonths;
    private Long totalDebt;

    public StudentDebtDTO(Long studentId, String studentCode, String fullName, String className, Long unpaidMonths, Long totalDebt) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.className = className;
        this.unpaidMonths = unpaidMonths != null ? unpaidMonths.intValue() : 0;
        this.totalDebt = totalDebt;
    }
}