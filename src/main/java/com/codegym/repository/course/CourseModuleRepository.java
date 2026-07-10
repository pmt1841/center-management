package com.codegym.repository.course;

import com.codegym.model.course.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    @Query("SELECT MAX(m.orderNumber) FROM CourseModule m WHERE m.course.id = :courseId")
    Integer findMaxOrderNumberByCourseId(@Param("courseId") Long courseId);
    
    boolean existsByCourseIdAndModuleNameAndIdNot(Long courseId, String moduleName, Long moduleId);
}
