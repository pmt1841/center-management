package com.codegym.model.tuition;

import com.codegym.model.user.student.Student;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tuition_payments")
@Data
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TuitionPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amount;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String note;

    @Enumerated(EnumType.STRING)
    private TuitionStatus tuitionStatus = TuitionStatus.DEBT;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void setStatus(TuitionStatus tuitionStatus) {
        this.tuitionStatus = tuitionStatus;
    }
}
