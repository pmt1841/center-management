package com.codegym.repository.course;

import com.codegym.dto.CourseStatsDTO;
import com.codegym.model.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);

    @Query("SELECT s FROM Course s WHERE " +
                  "LOWER(s.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                  "LOWER(s.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Course> searchSubjects(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.codegym.dto.CourseStatsDTO(s.courseName, COUNT(st.id)) " +
            "FROM Classroom c " +
            "JOIN c.course s " +
            "JOIN c.students st " +
            "GROUP BY s.id, s.courseName " +
            "ORDER BY COUNT(st.id) DESC")
    List<CourseStatsDTO> findTopPopularSubjects(Pageable pageable);
}
