package com.codegym.model.user.student;

import com.codegym.model.user.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "student_diaries")
@Data
@RequiredArgsConstructor
public class StudentDiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate createdDate = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private AppUser author;
}
