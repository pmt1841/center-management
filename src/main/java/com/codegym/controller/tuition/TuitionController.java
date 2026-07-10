package com.codegym.controller.tuition;

import com.codegym.dto.user.StudentDebtDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.discount.DiscountCode;
import com.codegym.model.system.AppSetting;
import com.codegym.model.tuition.TuitionPayment;
import com.codegym.model.tuition.TuitionStatus;
import com.codegym.model.user.student.Student;
import com.codegym.repository.system.SettingRepository;
import com.codegym.service.discount.DiscountService;
import com.codegym.service.tuition.TuitionService;
import com.codegym.service.user.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/tuitions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TuitionController {
    private final TuitionService tuitionService;
    private final SettingRepository settingRepository;
    private final DiscountService discountService;
    private final StudentService studentService;

    @GetMapping("")
    public String viewTuitions(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) TuitionStatus status,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<StudentDebtDTO> studentDebtPage = tuitionService.getStudentDebtSummary(keyword, pageable, status);

        model.addAttribute("studentDebtPage", studentDebtPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        model.addAttribute("status", status);

        AppSetting daySetting = settingRepository.findBySettingKey("TUITION_NOTIFICATION_DAY")
                .orElse(new AppSetting("TUITION_NOTIFICATION_DAY", "25"));
        model.addAttribute("notificationDay", daySetting.getSettingValue());

        return "tuition/list";
    }

    @GetMapping("/students/{studentCode}/pay")
    public String tuitionPayment(@PathVariable("studentCode") String studentCode, Model model) {
        Student student = studentService.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên " + studentCode));

        List<TuitionPayment> debtList = tuitionService.findByStudentIdAndStatus(student.getId(), TuitionStatus.DEBT);

        long totalDebt = 0;
        for (TuitionPayment debt : debtList) {
            totalDebt += debt.getAmount();
        }
        int unpaidMonths = debtList.size();

        model.addAttribute("student", student);
        model.addAttribute("debtList", debtList);
        model.addAttribute("totalDebt", totalDebt);
        model.addAttribute("unpaidMonths", unpaidMonths);

        List<DiscountCode> validDiscounts = discountService.findAllValidCode();

        model.addAttribute("validDiscounts", validDiscounts);

        return "tuition/payment";
    }

    @PostMapping("/process-payment")
    public String processPayment(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "tuitionIds", required = false) List<Long> tuitionIds,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "discountId", required = false) Long discountId,
            RedirectAttributes redirectAttributes) {

        // Validate cơ bản từ server (Dù Front-end đã chặn nút bấm)
        if (tuitionIds == null || tuitionIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 tháng nợ để thanh toán!");
            return "redirect:/admin/tuitions/students/" + studentId; // Trở lại trang chi tiết
        }

        try {
            // Gọi Service xử lý logic thanh toán
            tuitionService.processTuitionPayment(studentId, tuitionIds, paymentMethod, discountId);

            redirectAttributes.addFlashAttribute("successMessage", "Thu tiền học phí thành công!");
            return "redirect:/admin/tuitions"; // Thu xong đá về danh sách

        } catch (Exception e) {
            // Bắt lỗi (ví dụ: mã giảm giá hết hạn, hết lượt...)
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thanh toán: " + e.getMessage());
            return "redirect:/admin/tuitions/students/" + studentId;
        }
    }

    @PostMapping("/setting-day")
    public String saveTuitionNotificationDay(
            @RequestParam("notificationDay") String day,
            RedirectAttributes redirectAttributes) {

        if (settingRepository.findBySettingKey("TUITION_NOTIFICATION_DAY").isEmpty()) {
            AppSetting setting = new AppSetting("TUITION_NOTIFICATION_DAY", day);
            settingRepository.save(setting);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ngày gửi email tự động thành công!");
        return "redirect:/admin/tuitions";
    }

    @GetMapping("/students/{code}/detail")
    public String showTuitionDetail(@PathVariable("code") String studentCode, Model model) {
        Student student = studentService.findByStudentCode(studentCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học viên có mã: " + studentCode));

        // 2. Lấy danh sách các bản ghi học phí của học viên này (Sắp xếp theo hạn đóng tăng dần)
        List<TuitionPayment> tuitionList = tuitionService.findByStudentIdOrderByDueDateAsc(student.getId());

        // 3. Tính toán tổng nợ hiện tại để hiển thị trên Card thống kê
        long totalDebtAmount = tuitionList.stream()
                .filter(payment -> payment.getTuitionStatus() == TuitionStatus.DEBT)
                .mapToLong(TuitionPayment::getAmount)
                .sum();

        // 4. Đưa dữ liệu xuống Model
        model.addAttribute("student", student);
        model.addAttribute("tuitionList", tuitionList);
        model.addAttribute("totalDebtAmount", totalDebtAmount);

        return "tuition/detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        TuitionPayment payment = tuitionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi học phí ID: " + id));

        model.addAttribute("payment", payment);

        return "tuition/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable("id") Long id,
                                  @ModelAttribute("payment") TuitionPayment formPayment,
                                  RedirectAttributes redirectAttributes) {

        TuitionPayment existingPayment = tuitionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi học phí ID: " + id));

        existingPayment.setAmount(formPayment.getAmount());
        existingPayment.setDueDate(formPayment.getDueDate());
        existingPayment.setTuitionStatus(formPayment.getTuitionStatus());
        existingPayment.setNote(formPayment.getNote());

        if (formPayment.getTuitionStatus() == TuitionStatus.PAID) {
            existingPayment.setPaymentDate(formPayment.getPaymentDate());
            existingPayment.setPaymentMethod(formPayment.getPaymentMethod());
        } else {
            existingPayment.setPaymentDate(null);
            existingPayment.setPaymentMethod(null);
        }

        tuitionService.save(existingPayment);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bản ghi học phí thành công!");

        return "redirect:/admin/tuitions/edit/" + id;
    }
}
