package com.codegym.service.tuition;

import com.codegym.dto.user.StudentDebtDTO;
import com.codegym.model.tuition.TuitionPayment;
import com.codegym.model.tuition.TuitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TuitionService {
    void enrollStudentAndGenerateTuition(Long studentId, Long courseId, LocalDate registrationDate);

    List<TuitionPayment> findByStudentId(Long id);

    TuitionPayment findLatestByStudentId(Long id);

    Page<StudentDebtDTO> getStudentDebtSummary(String keyword, Pageable pageable, TuitionStatus tuitionStatus);

    List<TuitionPayment> findByStudentIdAndStatus(Long id, TuitionStatus status);

    void processTuitionPayment(Long studentId, List<Long> tuitionIds, String payMode, Long discountId);

    List<TuitionPayment> findAllById(List<Long> tuitionIds);

    List<TuitionPayment> findByStudentIdOrderByDueDateAsc(Long id);

    Optional<TuitionPayment> findById(Long id);

    void save(TuitionPayment existingPayment);
}