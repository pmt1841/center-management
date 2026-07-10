package com.codegym.model.course;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Tên bài học

    @Column(name = "sort_order")
    private Integer sortOrder; // Thứ tự bài học trong chương

    // Loại bài học: VIDEO, READING (Bài đọc), QUIZ (Trắc nghiệm), ASSIGNMENT (Bài tập)
    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type")
    private LessonType type;

    // Nội dung bài học (Dùng cho bài đọc Text, lưu HTML từ trình soạn thảo CKEditor/TinyMCE)
    @Column(columnDefinition = "TEXT")
    private String content;

    // URL Video (Nếu type là VIDEO - lưu link Youtube/Vimeo hoặc link file MP4)
    @Column(name = "video_url")
    private String videoUrl;

    // Nối về Chương/Module
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule courseModule;
}
