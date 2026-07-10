package com.codegym.scheduler;

import com.codegym.model.system.AppSetting;
import com.codegym.model.tuition.TuitionPayment;
import com.codegym.model.tuition.TuitionStatus;
import com.codegym.repository.system.SettingRepository;
import com.codegym.repository.tuition.TuitionPaymentRepository;
import com.codegym.service.cloud.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {
    private final TuitionPaymentRepository tuitionPaymentRepository;
    private final SettingRepository settingRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *")
    public void autoSendTuitionEmails() {
        AppSetting setting = settingRepository.findBySettingKey("TUITION_NOTIFICATION_DAY").orElse(null);
        if (setting == null) return;

        int targetDay = Integer.parseInt(setting.getSettingValue());
        int today = LocalDate.now().getDayOfMonth();

        if (today == targetDay) {
            log.info("Bắt đầu tiến trình gửi email thông báo học phí...");

            List<TuitionPayment> unpaidTuitions = tuitionPaymentRepository.findByTuitionStatus(TuitionStatus.DEBT);

            for (TuitionPayment t : unpaidTuitions) {
                try {
                    emailService.sendTuitionReminder(
                            t.getStudent().getEmail(),
                            t.getStudent().getFullName(),
                            String.valueOf(LocalDate.now().getMonth()));
                } catch (Exception e) {
                    log.error("Lỗi gửi mail cho học viên: " + t.getStudent().getFullName());
                }
            }
        }
    }
}
