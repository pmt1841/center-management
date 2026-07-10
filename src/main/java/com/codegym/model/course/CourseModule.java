package com.codegym.model.course;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_modules")
@Data
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CourseModule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String moduleName; // Tên bài học/học phần

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả nội dung bài học

    @Column(nullable = false)
    private Integer orderNumber; // Thứ tự bài học (1, 2, 3...)

    // Mối quan hệ N-1 với Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Course course;

    @OneToMany(mappedBy = "courseModule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC") // Tự động sắp xếp bài học theo thứ tự khi query
    private List<Lesson> lessons = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
