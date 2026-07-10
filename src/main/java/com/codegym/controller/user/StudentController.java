package com.codegym.controller.user;

import com.codegym.dto.ScoreUpdateForm;
import com.codegym.dto.user.StudentUpdateDTO;
import com.codegym.dto.user.UserCreateDTO;
import com.codegym.dto.user.UserEditDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.mapper.UserMapper;
import com.codegym.model.course.CourseModule;
import com.codegym.model.course.ExamScore;
import com.codegym.model.course.Course;
import com.codegym.model.system.ActionType;
import com.codegym.model.tuition.TuitionPayment;
import com.codegym.model.tuition.TuitionStatus;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentDiary;
import com.codegym.model.user.student.StudentStatus;
import com.codegym.security.CustomUserDetails;
import com.codegym.service.classroom.ClassroomService;
import com.codegym.service.examScore.ExamScoreService;
import com.codegym.service.course.CourseService;
import com.codegym.service.system.SystemLogService;
import com.codegym.service.tuition.TuitionService;
import com.codegym.service.user.AppUserService;
import com.codegym.service.user.student.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {
    private final AppUserService appUserService;
    private final ClassroomService classroomService;
    private final StudentService studentService;
    private final TuitionService tuitionService;
    private final CourseService courseService;
    private final ExamScoreService examScoreService;
    private final SystemLogService systemLogService;
    private final UserMapper userMapper;

    @GetMapping({"", "/"})
    public String listStudents(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) StudentStatus status,
            @RequestParam(value = "classroomId", required = false) Long classroomId,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @RequestParam(value = "sort", defaultValue = "latest", required = false) String sortType,
            Model model) {

        Sort sort = switch (sortType) {
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "fullName"); // A - Z
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "fullName"); // Z - A
            default -> Sort.by(Sort.Direction.DESC, "updatedAt")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Student> studentPage = studentService.searchStudents(keyword, status, classroomId, pageable);

        model.addAttribute("students", studentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        model.addAttribute("status", status);
        model.addAttribute("classrooms", classroomService.findAll());
        model.addAttribute("classroomId", classroomId);
        model.addAttribute("currentSort", sortType);

        return "student/list";
    }

    @GetMapping("/{studentCode}/edit")
    public String showEditStudent(@PathVariable("studentCode") String studentCode, Model model) {
        Student student = studentService.findByStudentCode(studentCode).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên"));

        UserEditDTO studentDto = userMapper.toDto(student);

        if (student.getClassroom() != null) {
            studentDto.setClassroomId(student.getClassroom().getId());
        }

        model.addAttribute("student", studentDto);
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("classrooms", classroomService.findAll());
        model.addAttribute("statuses", StudentStatus.values());

        List<ExamScore> scores = studentService.getExamScoreByStudent(student);
        ScoreUpdateForm scoreForm = new ScoreUpdateForm();
        scoreForm.setScoreItems(scores.stream().map(s -> {
            ScoreUpdateForm.ScoreItem item = new ScoreUpdateForm.ScoreItem();
            item.setScoreId(s.getId());
            item.setTheoryScore(s.getTheoryScore());
            item.setPracticalScore(s.getPracticalScore());
            return item;
        }).collect(Collectors.toList()));

        model.addAttribute("scoreForm", scoreForm);
        model.addAttribute("rawScores", scores);
        model.addAttribute("diaries", studentService.getStudentDiaries(student));
        model.addAttribute("newDiary", new StudentDiary());
        model.addAttribute("tuitions", tuitionService.findByStudentId(student.getId()));

        List<TuitionPayment> tuitions = tuitionService.findByStudentId(student.getId());
        model.addAttribute("tuitions", tuitions);

        Course course = null;

        if (student.getClassroom() != null) {
            course = student.getClassroom().getCourse();
        }

        if (course != null) {
            int totalMonths = course.getDurationMonths();
            double totalFee = totalMonths * course.getMonthlyTuitionFee();

            long paidMonths = tuitions.stream()
                    .filter(t -> t.getTuitionStatus() == TuitionStatus.PAID)
                    .count();

            double paidAmount = tuitions.stream()
                    .filter(t -> t.getTuitionStatus() == TuitionStatus.PAID)
                    .mapToDouble(TuitionPayment::getAmount)
                    .sum();

            int progressPercent = totalMonths > 0 ? (int) Math.round((double) paidMonths / totalMonths * 100) : 0;

            model.addAttribute("course", course);
            model.addAttribute("totalMonths", totalMonths);
            model.addAttribute("paidMonths", paidMonths);
            model.addAttribute("progressPercent", progressPercent);
            model.addAttribute("totalFee", totalFee);
            model.addAttribute("paidAmount", paidAmount);

            List<ExamScore> allScores = studentService.findScoreByStudentIdAndCourseId(student.getId(), course.getId());

            Map<Long, ExamScore> latestScoresPerModule = new HashMap<>();
            for (ExamScore score : allScores) {
                if (score.getCourseModule() != null) {
                    Long modId = score.getCourseModule().getId();

                    if (!latestScoresPerModule.containsKey(modId) || score.getId() > latestScoresPerModule.get(modId).getId()) {
                        latestScoresPerModule.put(modId, score);
                    }
                }
            }

            List<Long> passedModuleIds = new ArrayList<>();
            for (Map.Entry<Long, ExamScore> entry : latestScoresPerModule.entrySet()) {
                if (entry.getValue().getAverageScore() >= 8.0) {
                    passedModuleIds.add(entry.getKey());
                }
            }

            boolean isCourseCompleted = false;
            if (course.getModules() != null && !course.getModules().isEmpty()) {
                isCourseCompleted = passedModuleIds.size() == course.getModules().size();
            }

            model.addAttribute("passedModuleIds", passedModuleIds);
            model.addAttribute("isCourseCompleted", isCourseCompleted);

            List<CourseModule> sortedModules = course.getModules().stream()
                    .sorted(Comparator.comparing(CourseModule::getOrderNumber))
                    .toList();

            CourseModule currentModule = null;
            boolean isRetake = false;

            for (CourseModule module : sortedModules) {
                ExamScore latestScore = latestScoresPerModule.get(module.getId());

                if (latestScore == null) {
                    currentModule = module;
                    break;
                } else if (latestScore.getAverageScore() != null && latestScore.getAverageScore() < 8.0) {
                    currentModule = module;
                    isRetake = true;
                    break;
                }
            }

            model.addAttribute("currentModule", currentModule);
            model.addAttribute("isRetake", isRetake);

            int totalModules = (course.getModules() != null) ? course.getModules().size() : 0;
            int passedModules = passedModuleIds.size();
            int progressPercentage = (totalModules == 0) ? 0 : (passedModules * 100) / totalModules;

            model.addAttribute("totalModules", totalModules);
            model.addAttribute("passedModules", passedModules);
            model.addAttribute("progressPercentage", progressPercentage);
        }
        return "student/edit";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MINISTRY')")
    @PostMapping("/{studentCode}/update-info")
    public String updateStudentInfo(
            @PathVariable("studentCode") String studentCode,
            @Valid @ModelAttribute("student") StudentUpdateDTO updatedStudent,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes, @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.student", bindingResult);
            redirectAttributes.addFlashAttribute("student", updatedStudent);

            return "redirect:/students/" + studentCode + "/edit";
        }

        studentService.updateStudentInfo(studentCode, updatedStudent);

        systemLogService.saveLog(ActionType.UPDATE, currentUser.getUsername(), "cập nhật thông tin học viên " + studentCode);

        return "redirect:/students/" + studentCode + "/edit?success=info";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MINISTRY')")
    @PostMapping("/{studentCode}/update-scores")
    public String updateStudentScores(@PathVariable("studentCode") String studentCode,
                                      @ModelAttribute("scoreForm") ScoreUpdateForm scoreForm,
                                      @AuthenticationPrincipal CustomUserDetails currentUser) {
        Student student = studentService.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên " + studentCode));
        examScoreService.updateScores(scoreForm.getScoreItems());
        systemLogService.saveLog(ActionType.UPDATE, currentUser.getUsername(), "cập nhật điểm số học viên " + studentCode);

        return "redirect:/students/" + studentCode + "/edit?success=scores";
    }

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    @PostMapping("/{studentCode}/add-diary")
    public String addStudentDiary(@PathVariable("studentCode") String studentCode,
                                  @ModelAttribute("newDiary") StudentDiary newDiary,
                                  @RequestParam("authorId") Long authorId,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        AppUser author = appUserService.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        newDiary.setAuthor(author);
        Student student = studentService.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên " + studentCode));
        newDiary.setStudent(student);

        studentService.saveStudentDiary(newDiary);

        systemLogService.saveLog(ActionType.OTHER, currentUser.getUsername(), "viết nhật kí học viên " + studentCode);

        return "redirect:/students/" + studentCode + "/edit?success=diary";
    }

    @PostMapping("/{studentCode}/add-score")
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public String addStudentScore(@PathVariable("studentCode") String studentCode,
                                  @RequestParam("courseCode") String courseCode,
                                  @RequestParam("moduleId") Long moduleId,
                                  @RequestParam("theoryScore") Double theoryScore,
                                  @RequestParam("practicalScore") Double practicalScore,
                                  @RequestParam("examDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
                                  @AuthenticationPrincipal CustomUserDetails currentUser,
                                  RedirectAttributes redirectAttributes) {

        Student student = studentService.findByStudentCode(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học viên " + studentCode));

        Course course = courseService.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học " + courseCode));

        List<ExamScore> existingScores = studentService.findScoreByStudentIdAndCourseId(student.getId(), moduleId);

        ExamScore latestScore = existingScores.stream()
                .max(Comparator.comparing(ExamScore::getId))
                .orElse(null);

        CourseModule module = courseService.findModuleById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Module id: " + moduleId));

        ExamScore newScore = new ExamScore();
        newScore.setStudent(student);
        newScore.setCourse(module.getCourse());
        newScore.setClassroom(student.getClassroom());
        newScore.setCourseModule(module);
        newScore.setTheoryScore(theoryScore);
        newScore.setPracticalScore(practicalScore);
        newScore.setExamDate(examDate);

        newScore.setAverageScore((theoryScore + practicalScore) / 2.0);

        examScoreService.save(newScore);
        systemLogService.saveLog(ActionType.CREATE, currentUser.getUsername(), "thêm điểm số cho học viên " + studentCode);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm điểm thành công!");
        return "redirect:/students/" + studentCode + "/edit?tab=scores";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MINISTRY')")
    @PostMapping("/{studentCode}/delete")
    public String deleteStudent(@PathVariable("studentCode") String studentCode,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        studentService.deleteByStudentCode(studentCode);
        systemLogService.saveLog(ActionType.DELETE, currentUser.getUsername(), "xóa học viên " + studentCode);
        return "redirect:/students";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MINISTRY')")
    @PostMapping("/{studentCode}/restore")
    public String restoreStudent(@PathVariable("studentCode") String studentCode,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        studentService.restoreByStudentCode(studentCode);
        systemLogService.saveLog(ActionType.RESTORE, currentUser.getUsername(), "khôi phục học viên " + studentCode);
        return "redirect:/students";
    }
}
