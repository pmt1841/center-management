package com.codegym.model.user.student;

import com.codegym.model.classroom.Classroom;
import com.codegym.model.user.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class Student extends AppUser {
    @Column(unique = true)
    private String studentCode;

    @Enumerated(EnumType.STRING)
    private StudentStatus studentStatus = StudentStatus.WAITING_TRANSFER;

    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
}
