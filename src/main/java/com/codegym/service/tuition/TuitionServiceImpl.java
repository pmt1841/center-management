package com.codegym.service.tuition;

import com.codegym.dto.user.StudentDebtDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.course.Course;
import com.codegym.model.discount.DiscountCode;
import com.codegym.model.discount.DiscountType;
import com.codegym.model.tuition.TuitionPayment;
import com.codegym.model.tuition.TuitionStatus;
import com.codegym.model.user.student.Student;
import com.codegym.repository.course.CourseRepository;
import com.codegym.repository.discount.DiscountCodeRepository;
import com.codegym.repository.tuition.TuitionPaymentRepository;
import com.codegym.repository.user.student.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TuitionServiceImpl implements TuitionService {
    private final TuitionPaymentRepository tuitionPaymentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final DiscountCodeRepository discountCodeRepository;

    @Override
    @Transactional
    public void enrollStudentAndGenerateTuition(Long studentId, Long courseId, LocalDate registrationDate) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên id" + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học id" + courseId));

        LocalDate startDate;
        if (registrationDate.getDayOfMonth() <= 7) {
            startDate = registrationDate.withDayOfMonth(1);
        } else {
            startDate = registrationDate.plusMonths(1).withDayOfMonth(1);
        }

        List<TuitionPayment> payments = new ArrayList<>();
        for (int i = 0; i < course.getDurationMonths(); i++) {
            TuitionPayment payment = new TuitionPayment();
            payment.setStudent(student);
            payment.setAmount(course.getMonthlyTuitionFee());

            LocalDate currentMonth = startDate.plusMonths(i);
            payment.setDueDate(currentMonth.withDayOfMonth(25));
            payment.setStatus(TuitionStatus.DEBT);
            payment.setNote(String.format("Học phí tháng %02d/%d (%s)",
                    currentMonth.getMonthValue(), currentMonth.getYear(), course.getCourseName()));

            payments.add(payment);
        }

        tuitionPaymentRepository.saveAll(payments);
    }

    @Override
    public List<TuitionPayment> findByStudentId(Long id) {
        return tuitionPaymentRepository.findByStudentIdAndDeletedAtIsNullOrderByDueDateAsc(id);
    }

    @Override
    public TuitionPayment findLatestByStudentId(Long id) {
        return tuitionPaymentRepository.findFirstByStudentIdOrderByCreatedAtDesc(id)
                .orElse(null);
    }

    @Override
    public Page<StudentDebtDTO> getStudentDebtSummary(String keyword, Pageable pageable, TuitionStatus tuitionStatus) {
        if (keyword != null && !keyword.isEmpty()) {
            keyword = keyword.trim();
        }
        return tuitionPaymentRepository.getStudentDebtSummary(keyword, tuitionStatus, pageable);
    }

    @Override
    public List<TuitionPayment> findByStudentIdAndStatus(Long id, TuitionStatus status) {
        return tuitionPaymentRepository.findByStudentIdAndTuitionStatus(id, status);
    }

    @Override
    @Transactional
    public void processTuitionPayment(Long studentId, List<Long> tuitionIds, String payMode, Long discountId) {

        // 1. Lấy danh sách các khoản nợ dựa trên ID gửi lên
        List<TuitionPayment> debts = tuitionPaymentRepository.findAllById(tuitionIds);

        if (debts.size() != tuitionIds.size()) {
            throw new IllegalArgumentException("Dữ liệu nợ không hợp lệ hoặc đã thay đổi!");
        }

        // 2. Tính tổng tiền nợ ban đầu (Subtotal)
        double subTotal = 0;
        for (TuitionPayment debt : debts) {
            subTotal += debt.getAmount();
        }

        double discountAmount = 0;

        // 3. Xử lý Mã giảm giá NẾU là hình thức "Đóng toàn khóa" và có chọn mã
        if ("FULL".equals(payMode) && discountId != null) {
            DiscountCode discount = discountCodeRepository.findById(discountId)
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại."));

            // --- Validate điều kiện của mã giảm giá ---
            if (!discount.isActive()) {
                throw new IllegalArgumentException("Mã giảm giá đã bị vô hiệu hóa.");
            }

            LocalDateTime now = LocalDateTime.now();
            if (discount.getValidFrom() != null && now.isBefore(discount.getValidFrom())) {
                throw new IllegalArgumentException("Mã giảm giá chưa đến thời gian áp dụng.");
            }
            if (discount.getValidUntil() != null && now.isAfter(discount.getValidUntil())) {
                throw new IllegalArgumentException("Mã giảm giá đã hết hạn.");
            }
            if (discount.getMaxUsage() != null && discount.getCurrentUsage() >= discount.getMaxUsage()) {
                throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
            }

            // --- Tính tiền giảm ---
            if (discount.getType() == DiscountType.PERCENT) {
                discountAmount = subTotal * (discount.getDiscountValue() / 100.0);
            } else if (discount.getType() == DiscountType.AMOUNT) {
                discountAmount = discount.getDiscountValue();
            }

            // Đảm bảo không giảm lố tổng tiền
            if (discountAmount > subTotal) {
                discountAmount = subTotal;
            }

            // --- Cập nhật lượt sử dụng mã ---
            discount.setCurrentUsage(discount.getCurrentUsage() + 1);
            discountCodeRepository.save(discount);
        }

        // 4. Tính số tiền thực tế khách phải trả (Final Total)
        double finalTotal = subTotal - discountAmount;

        // 5. Cập nhật trạng thái các tháng nợ thành "ĐÃ ĐÓNG"
        for (TuitionPayment debt : debts) {
            debt.setStatus(TuitionStatus.PAID); // Hoặc dùng Enum của bạn
            debt.setPaymentDate(LocalDateTime.now());
            // Có thể lưu thêm phương thức thanh toán, hoặc ID người thu tiền vào đây
        }
        tuitionPaymentRepository.saveAll(debts);
    }

    @Override
    public List<TuitionPayment> findAllById(List<Long> tuitionIds) {
        return tuitionPaymentRepository.findAllById(tuitionIds);
    }

    @Override
    public List<TuitionPayment> findByStudentIdOrderByDueDateAsc(Long id) {
        return tuitionPaymentRepository.findByStudentIdOrderByCreatedAtDesc(id);
    }

    @Override
    public Optional<TuitionPayment> findById(Long id) {
        return tuitionPaymentRepository.findById(id);
    }

    @Override
    @Transactional
    public void save(TuitionPayment existingPayment) {
        tuitionPaymentRepository.save(existingPayment);
    }
}
