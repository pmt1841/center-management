package com.codegym.repository.user.student;

import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.classroom.lecturer = :lecturer")
    long countByLecturer(@Param("lecturer") Lecturer lecturer);

    @Query(
            value = "SELECT s FROM Student s " +
                    "LEFT JOIN s.classroom c " +
                    "WHERE (:status IS NULL OR s.studentStatus = :status) " +
                    "AND (:classroomId IS NULL OR c.id = :classroomId) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR (" +
                    "   LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "   OR LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "   OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                    "))",
            // Ép Hibernate join bảng cha (AppUser) bằng ID, và lấy thuộc tính từ AppUser (alias là 'a')
            countQuery = "SELECT COUNT(s.id) FROM Student s " +
                    "JOIN AppUser a ON s.id = a.id " +
                    "LEFT JOIN s.classroom c " +
                    "WHERE (:status IS NULL OR s.studentStatus = :status) " +
                    "AND (:classroomId IS NULL OR c.id = :classroomId) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR (" +
                    "   LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "   OR LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "   OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                    "))"
    )
    Page<Student> searchStudents(@Param("keyword") String keyword,
                                 @Param("status") StudentStatus status,
                                 @Param("classroomId") Long classroomId,
                                 Pageable pageable);
    long countByStudentStatusIn(List<StudentStatus> statuses);

    @Query("SELECT MONTH(s.createdAt), COUNT(s.id) " +
            "FROM Student s " +
            "WHERE YEAR(s.createdAt) = :year " +
            "AND s.deletedAt IS NULL " +
            "GROUP BY MONTH(s.createdAt) " +
            "ORDER BY MONTH(s.createdAt)")
    List<Object[]> countEnrollmentsByMonthAndYear(@Param("year") int year);

    @Query("SELECT COUNT (s) FROM ExamScore s ")
    long countStudentUnderAverageScore(@Param("averageScore") double averageScore);

    @Query("SELECT s FROM Student s " +
            "JOIN s.classroom c " +
            "WHERE c.lecturer = :lecturer " +
            "AND (:status IS NULL OR s.studentStatus = :status) " +
            "AND (" +
            "   LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Student> searchStudentsByLecturer(
            @Param("keyword") String keyword,
            @Param("status") StudentStatus status,
            Pageable pageable,
            @Param("lecturer") Lecturer lecturer
    );
}
