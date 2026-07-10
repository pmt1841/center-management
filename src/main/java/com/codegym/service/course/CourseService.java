package com.codegym.service.course;

import com.codegym.dto.CourseStatsDTO;
import com.codegym.model.course.Course;
import com.codegym.model.course.CourseModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    long count();

    Optional<Course> findByCourseCode(String courseCode);

    void save(Course course);

    List<Course> findAll();

    Page<Course> searchCourses(String keyword, Pageable pageable);

    List<CourseStatsDTO> getTopPopularCourses(int i);

    void deleteByCourseCode(String courseCode);

    void restoreByCourseCode(String courseCode);

    Optional<Course> findById(Long courseId);

    Optional<CourseModule> findModuleById(Long moduleId);

    Course getCourseDetailForEdit(String courseCode);
}
