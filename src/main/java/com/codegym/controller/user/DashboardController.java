package com.codegym.controller.user;

import com.codegym.dto.CourseStatsDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.classroom.ClassDiary;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.course.ExamScore;
import com.codegym.model.system.SystemLog;
import com.codegym.model.tuition.TuitionPayment;
import com.codegym.model.tuition.TuitionStatus;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import com.codegym.repository.user.lecturer.LecturerRepository;
import com.codegym.security.CustomUserDetails;
import com.codegym.service.classroom.ClassroomService;
import com.codegym.service.examScore.ExamScoreService;
import com.codegym.service.course.CourseService;
import com.codegym.service.system.SystemLogService;
import com.codegym.service.tuition.TuitionService;
import com.codegym.service.user.AppUserService;
import com.codegym.service.user.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final AppUserService appUserService;
    private final ClassroomService classroomService;
    private final CourseService courseService;
    private final StudentService studentService;
    private final SystemLogService systemLogService;
    private final ObjectMapper objectMapper;
    private final ExamScoreService examScoreService;
    private final TuitionService tuitionService;
    private final LecturerRepository lecturerRepository;

    @GetMapping("/dashboard")
    public String dashboardRouter(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");

        return switch (role) {
            case "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            case "ROLE_MINISTRY" -> "redirect:/ministry/dashboard";
            case "ROLE_LECTURER" -> String.format("redirect:/lecturer/%s/dashboard", currentUser.getUserCode());
            case "ROLE_STUDENT" -> String.format("redirect:/student/%s/dashboard", currentUser.getUserCode());
            default -> "redirect:/";
        };
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {

        long totalStudents = studentService.count();
        long totalClasses = classroomService.count();
        long totalCourses = courseService.count();
        long totalLecturers = appUserService.countLecturer();
        long totalMinistries = appUserService.countMinistry();

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalLecturers", totalLecturers);
        model.addAttribute("totalMinistries", totalMinistries);
        model.addAttribute("totalClasses", totalClasses);
        model.addAttribute("totalCourses", totalCourses);

        try {
            int currentYear = Year.now().getValue();
            List<Integer> monthlyEnrollments = studentService.getMonthlyEnrollmentsByYear(currentYear);
            String enrollmentDataJson = objectMapper.writeValueAsString(monthlyEnrollments);
            model.addAttribute("enrollmentData", enrollmentDataJson);
        } catch (Exception e) {
            model.addAttribute("enrollmentData", "[]");
        }

        List<CourseStatsDTO> topCourses = courseService.getTopPopularCourses(5);
        model.addAttribute("topCourses", topCourses);

        List<SystemLog> recentLogs = systemLogService.getRecentLogs(10);
        model.addAttribute("systemLogs", recentLogs);

        return "dashboard/admin-dashboard";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MINISTRY')")
    @GetMapping("/ministry/dashboard")
    public String showMinistryDashboard(Model model) {
        long activeStudents = studentService.count();
        long activeClasses = classroomService.count();
        long diariesToday = classroomService.countDiariesByDate(LocalDate.now());
        long warningStudents = studentService.countStudentsUnderAverageScore(5.0);

        model.addAttribute("activeStudents", activeStudents);
        model.addAttribute("activeClasses", activeClasses);
        model.addAttribute("diariesToday", diariesToday);
        model.addAttribute("warningStudents", warningStudents);

        try {
            int currentYear = Year.now().getValue();
            List<Double> monthlyAvgScores = examScoreService.getMonthlyAveragesByYear(currentYear);
            String chartDataJson = objectMapper.writeValueAsString(monthlyAvgScores);
            model.addAttribute("chartData", chartDataJson);
        } catch (JacksonException e) {
            model.addAttribute("chartData", "[]");
        }

        Map<String, Double> averageScores = examScoreService.getAverageScorePerClassThisMonth();
        model.addAttribute("averageScores", averageScores);

        List<ClassDiary> recentDiaries = classroomService.findAllDiaries();
        model.addAttribute("classDiaries", recentDiaries);

        return "dashboard/ministry-dashboard";
    }

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    @GetMapping("/lecturer/{lecturerCode}/dashboard")
    public String showLecturerDashboard(Model model, @PathVariable("lecturerCode") String lecturerCode) {
        Lecturer lecturer = lecturerRepository.findByLecturerCode(lecturerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên " + lecturerCode));

        Long totalStudents = studentService.countByLecturer(lecturer);
        Long totalClassrooms = classroomService.countByLecturer(lecturer);
        List<Classroom> activeClassrooms = classroomService.findAllByLecturer(lecturer);

        Map<String, Double> avgScoresMap = classroomService.getClassroomAverageScores(activeClassrooms);

        List<String> chartClassNames = new ArrayList<>(avgScoresMap.keySet());
        List<Double> chartAvgScores = new ArrayList<>(avgScoresMap.values());

        model.addAttribute("activeClassrooms", activeClassrooms);
        model.addAttribute("totalClassrooms", totalClassrooms);
        model.addAttribute("totalStudents", totalStudents);

        model.addAttribute("chartClassNames", chartClassNames);
        model.addAttribute("chartAvgScores", chartAvgScores);

        return "dashboard/lecturer-dashboard";
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping("/student/{studentCode}/dashboard")
    public String showStudentDashboard(Model model, @PathVariable("studentCode") String studentCode) {
        Student student = studentService.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên " + studentCode));

        model.addAttribute("currentUser", student);

        Double gpa = examScoreService.calculateOverallGPA(student.getId());
        if (gpa != null) {
            gpa = Math.round(gpa * 100.0) / 100.0;
        }
        model.addAttribute("gpa", gpa);

        int completedModules = examScoreService.countPassedModules(student.getId(), 8.0);

        int totalModules = 0;
        if (student.getClassroom().getCourse() != null && student.getClassroom().getCourse().getModules() != null) {
            totalModules = student.getClassroom().getCourse().getModules().size();
        }

        model.addAttribute("completedModules", completedModules);
        model.addAttribute("totalModules", totalModules);


        // 4. Tình trạng học phí
        // Lấy thông tin học phí mới nhất hoặc tổng hợp của học viên
        TuitionStatus tuitionStatus = null;
        TuitionPayment tuition = tuitionService.findLatestByStudentId(student.getId());
        if (tuition != null) {
            tuitionStatus = tuition.getTuitionStatus();
        }
        model.addAttribute("tuitionStatus", tuitionStatus);

        // 5. Kết quả thi mới nhất (Recent Scores)
        // Lấy danh sách 5 kết quả thi gần nhất (Dùng Pageable trong Repository để limit)
        List<ExamScore> recentScores = examScoreService.findRecentScoresByStudentId(student.getId(), 5);
        model.addAttribute("recentScores", recentScores);

        model.addAttribute("currentCourse", student.getClassroom().getCourse().getCourseName());

        return "dashboard/student-dashboard";
    }
}
