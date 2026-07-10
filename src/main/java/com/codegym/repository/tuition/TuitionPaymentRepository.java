package com.codegym.repository.tuition;

import com.codegym.dto.user.StudentDebtDTO;
import com.codegym.model.tuition.TuitionStatus;
import com.codegym.model.tuition.TuitionPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TuitionPaymentRepository extends JpaRepository<TuitionPayment, Long> {

    List<TuitionPayment> findByTuitionStatus(TuitionStatus tuitionStatus);

    @Query("SELECT t FROM TuitionPayment t WHERE t.deletedAt IS NULL " +
            "AND (:keyword IS NULL OR LOWER(t.student.fullName) " +
            "LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.student.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR t.tuitionStatus = :status) " +
            "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
            "ORDER BY t.createdAt DESC")
    Page<TuitionPayment> filterTuitions(
            @Param("keyword") String keyword,
            @Param("status") TuitionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    List<TuitionPayment> findByStudentIdAndDeletedAtIsNullOrderByDueDateAsc(Long studentId);

    Optional<TuitionPayment> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT new com.codegym.dto.user.StudentDebtDTO(" +
            "t.student.id, " +
            "t.student.studentCode, " +
            "t.student.fullName, " +
            "t.student.classroom.className, " +
            "SUM(CASE WHEN t.tuitionStatus = 'DEBT' THEN 1L ELSE 0L END), " +
            "SUM(CASE WHEN t.tuitionStatus = 'DEBT' THEN t.amount ELSE 0L END)) " +
            "FROM TuitionPayment t " +
            "WHERE (:keyword IS NULL OR :keyword = '' " +
            "   OR LOWER(t.student.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(t.student.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY t.student.id, t.student.studentCode, t.student.fullName, t.student.classroom.className " +
            "HAVING (:status IS NULL " +
            "   OR (:#{#status?.name()} = 'DEBT' AND SUM(CASE WHEN t.tuitionStatus = 'DEBT' THEN 1L ELSE 0L END) > 0) " +
            "   OR (:#{#status?.name()} = 'PAID' AND SUM(CASE WHEN t.tuitionStatus = 'DEBT' THEN 1L ELSE 0L END) = 0))")
    Page<StudentDebtDTO> getStudentDebtSummary(@Param("keyword") String keyword,
                                               @Param("status") TuitionStatus status,
                                               Pageable pageable);

    List<TuitionPayment> findByStudentIdAndTuitionStatus(Long studentId, TuitionStatus tuitionStatus);

    List<TuitionPayment> findByStudentIdOrderByCreatedAtDesc(Long id);
}
