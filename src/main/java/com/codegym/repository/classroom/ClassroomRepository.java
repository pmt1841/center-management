package com.codegym.repository.classroom;

import com.codegym.model.classroom.Classroom;
import com.codegym.model.user.lecturer.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByClassCode(String classCode);

    List<Classroom> findAllByLecturer(Lecturer lecturer);

    @Query("SELECT c FROM Classroom c " +
            "LEFT JOIN c.lecturer l " +
            "LEFT JOIN c.course s " +
            "WHERE LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Classroom> searchClassrooms(@Param("keyword") String keyword, Pageable pageable);

    Long countByLecturer(Lecturer lecturer);

    @Query("SELECT c FROM Classroom c " +
            "LEFT JOIN c.lecturer l " +
            "LEFT JOIN c.course s " +
            "WHERE c.lecturer = :lecturer " +
            "AND (:keyword IS NULL OR :keyword = '' OR (" +
            "   LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(s.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "))")
    Page<Classroom> searchClassroomsByLecturer(@Param("keyword") String keyword,
                                               @Param("lecturer") Lecturer lecturer,
                                               Pageable pageable);
}
