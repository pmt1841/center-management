package com.codegym.controller.course;

import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.course.Course;
import com.codegym.model.course.CourseModule;
import com.codegym.model.course.Lesson;
import com.codegym.model.course.LessonType;
import com.codegym.model.system.ActionType;
import com.codegym.repository.course.CourseModuleRepository;
import com.codegym.security.CustomUserDetails;
import com.codegym.service.LessonService;
import com.codegym.service.course.CourseService;
import com.codegym.service.system.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
@PreAuthorize("hasRole('ADMIN')")
public class CourseController {
    private final CourseService courseService;
    private final SystemLogService systemLogService;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonService lessonService;

    @GetMapping({"", "/"})
    public String listCourses(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Course> coursePage = courseService.searchCourses(keyword, pageable);

        model.addAttribute("courses", coursePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        return "course/list";
    }

    @GetMapping("/create")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "course/create";
    }

    @PostMapping("/create")
    public String addCourse(@ModelAttribute Course course,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        String courseCode = course.getCourseCode();
        boolean courseExists = courseService.findByCourseCode(courseCode).isPresent();
        if (courseExists) {
            redirectAttributes.addFlashAttribute("message", "Khóa học đã tồn tại");
            return "redirect:/courses/create";
        }
        courseService.save(course);
        redirectAttributes.addFlashAttribute("message", "Thêm khóa học thành công");
        systemLogService.saveLog(ActionType.CREATE, currentUser.getUsername(), "thêm khóa học mới" + course.getCourseName());
        return "redirect:/courses/create";
    }

    @GetMapping("/{courseCode}/edit")
    public String showCreateCourseForm(@PathVariable("courseCode") String courseCode, Model model) {
        Course course = courseService.getCourseDetailForEdit(courseCode);

        if (course.getModules() == null || course.getModules().isEmpty()) {
            course.setModules(new ArrayList<>());
        }

        model.addAttribute("course", course);
        return "course/edit";
    }

    @PostMapping("/{courseCode}/edit")
    public String editCourse(@PathVariable("courseCode") String courseCode,
                             @ModelAttribute Course course,
                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        Course courseOptional = courseService.findByCourseCode(courseCode).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        courseService.save(courseOptional);
        systemLogService.saveLog(ActionType.UPDATE, currentUser.getUsername(), "cập nhật khóa học " + course.getCourseName());
        return "redirect:/courses/edit";
    }

    @PostMapping("/{courseCode}/delete")
    public String deleteCourse(@PathVariable("courseCode") String courseCode,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        Course course = courseService.findByCourseCode(courseCode).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy khóa học"));
        courseService.deleteByCourseCode(courseCode);
        systemLogService.saveLog(ActionType.DELETE, currentUser.getUsername(), "xóa khóa học " + course.getCourseName());
        return "redirect:/courses";
    }

    @PostMapping("/{courseCode}/restore")
    public String restoreCourse(@PathVariable("courseCode") String courseCode,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        Course course = courseService.findByCourseCode(courseCode).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy khóa học"));
        courseService.restoreByCourseCode(courseCode);
        systemLogService.saveLog(ActionType.RESTORE, currentUser.getUsername(), "khôi phục khóa học " + course.getCourseName());
        return "redirect:/courses";
    }

    @PostMapping("/{courseCode}/modules/save")
    public String saveModule(@PathVariable("courseCode") String courseCode,
                             @RequestParam(value = "moduleId", required = false) Long moduleId,
                             @RequestParam("moduleName") String moduleName,
                             @RequestParam("description") String description,
                             @AuthenticationPrincipal CustomUserDetails currentUser,
                             RedirectAttributes redirectAttributes) {

        Course course = courseService.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        Long checkId = (moduleId != null) ? moduleId : -1L;
        boolean isDuplicate = courseModuleRepository.existsByCourseIdAndModuleNameAndIdNot(course.getId(), moduleName, checkId);

        if (isDuplicate) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Tên bài học '" + moduleName + "' đã tồn tại trong khóa học này!");
            return "redirect:/courses/" + courseCode + "/edit";
        }

        CourseModule module;
        if (moduleId != null) {
            // TRƯỜNG HỢP CẬP NHẬT: Lấy từ DB ra, không đụng chạm đến thuộc tính orderNumber cũ
            module = courseService.findModuleById(moduleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học phần"));
        } else {
            // TRƯỜNG HỢP THÊM MỚI: Tự động tính số thứ tự tiếp theo
            module = new CourseModule();
            module.setCourse(course);

            Integer maxOrder = courseModuleRepository.findMaxOrderNumberByCourseId(course.getId());
            module.setOrderNumber((maxOrder == null ? 0 : maxOrder) + 1);
        }

        module.setModuleName(moduleName);
        module.setDescription(description);

        courseModuleRepository.save(module);
        systemLogService.saveLog(ActionType.CREATE, currentUser.getUsername(), "thêm/cập nhật module " + moduleName + " cho khóa học " + course.getCourseName());
        return "redirect:/courses/" + courseCode + "/edit";
    }

    @PostMapping("/{courseCode}/modules/{moduleId}/delete")
    public String deleteModule(@PathVariable("courseCode") String courseCode,
                               @PathVariable("moduleId") Long moduleId,
                               @AuthenticationPrincipal CustomUserDetails currentUser) {
        Course course = courseService.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy module"));

        courseModuleRepository.deleteById(moduleId);
        systemLogService.saveLog(ActionType.DELETE, currentUser.getUsername(), "xóa module " + module.getModuleName() + " trong khóa học " + course.getCourseName());
        return "redirect:/courses/" + courseCode + "/edit";
    }

    @PostMapping("/{courseCode}/lessons/quick-save")
    public String quickSaveLesson(
            @PathVariable("courseCode") String courseCode,
            @RequestParam("moduleId") Long moduleId,
            @RequestParam("lessonTitle") String lessonTitle,
            @RequestParam("lessonType") LessonType lessonType,
            RedirectAttributes ra) {

        try {
            // Gọi LessonService để lưu bài học vào DB
            lessonService.createQuickLesson(moduleId, lessonTitle, lessonType);
            ra.addFlashAttribute("successMessage", "Đã thêm bài học mới thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi thêm bài học: " + e.getMessage());
        }

        // Lưu xong thì quay lại trang chỉnh sửa khóa học
        return "redirect:/courses/" + courseCode + "/edit";
    }

    @GetMapping("/{code}/modules/{mId}/lessons/{lId}/edit")
    public String showEditLessonPage(
            @PathVariable("code") String courseCode,
            @PathVariable("mId") Long moduleId,
            @PathVariable("lId") Long lessonId,
            Model model) {

        Lesson lesson = lessonService.getLessonById(lessonId);

        model.addAttribute("lesson", lesson);
        model.addAttribute("courseCode", courseCode); // Truyền mã khóa học để làm nút "Quay lại"

        return "lesson/edit"; // Trỏ đến file HTML mới
    }

    @PostMapping("/{code}/modules/{mId}/lessons/{lId}/edit")
    public String updateLesson(
            @PathVariable("code") String courseCode,
            @PathVariable("mId") Long moduleId,
            @PathVariable("lId") Long lessonId,
            @ModelAttribute Lesson lesson,
            RedirectAttributes ra) {

        try {
            lessonService.updateLessonDetail(lessonId, lesson);
            ra.addFlashAttribute("successMessage", "Cập nhật nội dung bài học thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        // Lưu xong, redirect về lại trang Curriculum (trang chỉnh sửa khóa học tổng)
        return "redirect:/courses/" + courseCode + "/edit";
    }
}
