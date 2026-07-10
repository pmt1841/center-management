package com.codegym.service;

import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.course.CourseModule;
import com.codegym.model.course.Lesson;
import com.codegym.model.course.LessonType;
import com.codegym.repository.course.LessonRepository;
import com.codegym.repository.course.CourseModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final CourseModuleRepository courseModuleRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public void createQuickLesson(Long moduleId, String title, LessonType type) {
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Module"));

        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setType(type);
        lesson.setCourseModule(module);

        // Tự động set thứ tự (sortOrder) cho bài học mới vào cuối danh sách
        int currentLessonCount = module.getLessons() != null ? module.getLessons().size() : 0;
        lesson.setSortOrder(currentLessonCount + 1);

        lessonRepository.save(lesson);
    }

    @Transactional(readOnly = true)
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học với ID: " + id));
    }

    @Transactional
    public void updateLessonDetail(Long id, Lesson updatedData) {
        Lesson existingLesson = getLessonById(id);

        existingLesson.setTitle(updatedData.getTitle());
        existingLesson.setType(updatedData.getType());

        // Nếu là VIDEO
        if (updatedData.getType().name().equals("VIDEO")) {
            existingLesson.setVideoUrl(updatedData.getVideoUrl());
            existingLesson.setContent(null);
        }
        // Nếu là READING (Bài đọc)
        else if (updatedData.getType().name().equals("READING")) {
            existingLesson.setContent(updatedData.getContent());
            existingLesson.setVideoUrl(null);
        }

        lessonRepository.save(existingLesson);
    }
}
