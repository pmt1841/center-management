package com.codegym.service.system;

import com.codegym.model.system.ActionType;
import com.codegym.model.system.SystemLog;
import com.codegym.repository.system.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    public List<SystemLog> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return systemLogRepository.findAllByOrderByCreatedAtDesc(pageable).getContent();
    }

    public Page<SystemLog> getFilteredLogs(String keyword, ActionType actionType, String dateFilter, Pageable pageable) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now(); // Ngày kết thúc luôn là hiện tại

        if (dateFilter != null && !dateFilter.isEmpty()) {
            LocalDate today = LocalDate.now();
            switch (dateFilter) {
                case "today":
                    startDate = today.atStartOfDay(); // Từ 00:00 hôm nay
                    break;
                case "week":
                    startDate = today.with(DayOfWeek.MONDAY).atStartOfDay(); // Từ đầu tuần (Thứ 2)
                    break;
                case "month":
                    startDate = today.withDayOfMonth(1).atStartOfDay(); // Từ mùng 1 đầu tháng
                    break;
                case "year":
                    startDate = today.withDayOfYear(1).atStartOfDay(); // Từ ngày 1/1 đầu năm
                    break;
            }
        }

        String finalKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        return systemLogRepository.filterLogs(finalKeyword, actionType, startDate, endDate, pageable);
    }

    public void saveLog(ActionType actionType, String username, String details) {
        String name = (username != null && !username.trim().isEmpty()) ? username : "Hệ thống";

        String finalDetails = "Tài khoản [" + name + "] vừa " + details;

        SystemLog log = SystemLog.builder()
                .username(name)
                .actionType(actionType)
                .details(finalDetails)
                .build();

        systemLogRepository.save(log);
    }
}
