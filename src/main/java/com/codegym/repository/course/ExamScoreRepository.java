package com.codegym.repository.course;

import com.codegym.dto.ClassAvgScoreDTO;
import com.codegym.model.course.ExamScore;
import com.codegym.model.user.student.Student;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, Integer> {
    List<ExamScore> findAllByStudent(Student student);

    @Query("SELECT MONTH(e.examDate), AVG(e.averageScore) " +
            "FROM ExamScore e " +
            "WHERE YEAR(e.examDate) = :year " +
            "GROUP BY MONTH(e.examDate) " +
            "ORDER BY MONTH(e.examDate)")
    List<Object[]> getMonthlyAverageScoresByYear(@Param("year") int year);

    @Query("SELECT new com.codegym.dto.ClassAvgScoreDTO(c.classCode, AVG(e.averageScore)) " +
            "FROM ExamScore e " +
            "JOIN e.classroom c " +
            "WHERE MONTH(e.examDate) = :month AND YEAR(e.examDate) = :year " +
            "GROUP BY c.id, c.classCode " +
            "ORDER BY AVG(e.averageScore) DESC")
    List<ClassAvgScoreDTO> getAverageScorePerClassByMonthAndYear(@Param("month") int month, @Param("year") int year);

    Optional<ExamScore> findById(Long id);

    @Query("SELECT AVG(e.averageScore) FROM ExamScore e " +
            "WHERE e.id IN (" +
            "    SELECT MAX(e2.id) FROM ExamScore e2 " +
            "    WHERE e2.classroom.id = :classroomId " +
            "    GROUP BY e2.student.id, e2.courseModule.id" +
            ")")
    Double calculateAverageScoreByClassroomId(@Param("classroomId") Long classroomId);

    List<ExamScore> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    ExamScore findByStudentIdAndCourseModuleId(Long studentId, Long moduleId);

    @Query("SELECT AVG(e.averageScore) FROM ExamScore e WHERE e.student.id = :studentId AND e.course.id = :courseId AND e.courseModule IS NOT NULL")
    Double calculateCourseFinalScore(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    List<ExamScore> findAllByStudentId(Long id);
    List<ExamScore> findByStudentIdOrderByExamDateDesc(Long studentId, Pageable pageable);
}
