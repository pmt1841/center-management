package com.codegym.service.course;

import com.codegym.dto.CourseStatsDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.course.Course;
import com.codegym.model.course.CourseModule;
import com.codegym.repository.course.CourseRepository;
import com.codegym.repository.course.CourseModuleRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;

    @Override
    public long count() {
        return courseRepository.count();
    }

    @Override
    public Optional<Course> findByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    @Override
    public void save(Course course) {
        courseRepository.save(course);
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public Page<Course> searchCourses(String keyword, Pageable pageable) {
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        return courseRepository.searchSubjects(safeKeyword, pageable);
    }

    @Override
    public List<CourseStatsDTO> getTopPopularCourses(int limit) {
        return courseRepository.findTopPopularSubjects(PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public void deleteByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode).get();
        if (course.getClassrooms().isEmpty()) {
            courseRepository.delete(course);
        } else {
            course.setDeletedAt(LocalDateTime.now());
            courseRepository.save(course);
        }
    }

    @Override
    @Transactional
    public void restoreByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode).get();
        course.setDeletedAt(null);
        courseRepository.save(course);
    }

    @Override
    public Optional<Course> findById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    @Override
    public Optional<CourseModule> findModuleById(Long moduleId) {
        return courseModuleRepository.findById(moduleId);
    }

    @Transactional(readOnly = true)
    public Course getCourseDetailForEdit(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        // Ép Hibernate tải danh sách Module
        Hibernate.initialize(course.getModules());

        // Ép Hibernate tải danh sách Lesson bên trong từng Module
        if (course.getModules() != null) {
            for (CourseModule module : course.getModules()) {
                Hibernate.initialize(module.getLessons());
            }
        } else {
            course.setModules(new ArrayList<>());
        }

        return course;
    }
}
