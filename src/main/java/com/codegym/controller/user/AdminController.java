package com.codegym.controller.user;

import com.codegym.model.system.ActionType;
import com.codegym.model.system.AppSetting;
import com.codegym.model.system.SystemLog;
import com.codegym.repository.system.SettingRepository;
import com.codegym.service.system.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final SystemLogService systemLogService;
    private final SettingRepository settingRepository;

    @GetMapping("/system-logs")
    public String viewSystemLogs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "actionType", required = false) ActionType actionType,
            @RequestParam(value = "dateFilter", required = false) String dateFilter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SystemLog> logPage = systemLogService.getFilteredLogs(keyword, actionType, dateFilter, pageable);

        model.addAttribute("logPage", logPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("actionType", actionType);
        model.addAttribute("dateFilter", dateFilter);
        model.addAttribute("actionTypes", ActionType.values());

        return "system/log";
    }

    @GetMapping("/settings")
    public String viewSettings(Model model) {
        AppSetting daySetting = settingRepository.findBySettingKey("TUITION_NOTIFICATION_DAY")
                .orElse(new AppSetting("TUITION_NOTIFICATION_DAY", "25"));
        model.addAttribute("notificationDay", daySetting.getSettingValue());
        return "system/setting";
    }

    @PostMapping("/tuition-day")
    public String saveTuitionDay(@RequestParam("day") String day, RedirectAttributes redirectAttributes) {
        AppSetting setting = new AppSetting("TUITION_NOTIFICATION_DAY", day);
        settingRepository.save(setting);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật ngày gửi thông báo tự động!");
        return "redirect:/admin/settings";
    }
}
