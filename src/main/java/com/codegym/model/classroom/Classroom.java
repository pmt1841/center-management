package com.codegym.model.classroom;

import com.codegym.model.course.Course;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "classrooms")
@Data
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;
    @Column(unique = true)
    private String classCode;

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    @OneToMany(mappedBy = "classroom")
    private List<Student> students;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "opened_at")
    private LocalDate openedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
