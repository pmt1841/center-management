package com.codegym.model.classroom;

import com.codegym.model.user.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_diaries")
@Data
@RequiredArgsConstructor
public class ClassDiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate createdDate = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private AppUser author;
}
