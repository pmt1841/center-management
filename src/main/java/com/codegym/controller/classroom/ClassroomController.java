package com.codegym.controller.classroom;

import com.codegym.dto.ClassroomDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.classroom.ClassDiary;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.course.Course;
import com.codegym.model.system.ActionType;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentStatus;
import com.codegym.security.CustomUserDetails;
import com.codegym.service.classroom.ClassroomService;
import com.codegym.service.course.CourseService;
import com.codegym.service.system.SystemLogService;
import com.codegym.service.user.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/classrooms")
@PreAuthorize("hasAnyRole('ADMIN','MINISTRY')")
public class ClassroomController {
    private final ClassroomService classroomService;
    private final AppUserService appUserService;
    private final CourseService courseService;
    private final SystemLogService systemLogService;

    @GetMapping({"", "/"})
    public String showClassroomList(@RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(required = false, value = "page", defaultValue = "0") int page,
                                    @RequestParam(required = false, value = "size", defaultValue = "10") int size,
                                    Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("classCode").ascending());

        Page<Classroom> classroomPage = classroomService.searchClassrooms(keyword, pageable);

        model.addAttribute("classrooms", classroomPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        return "classroom/list";
    }

    @GetMapping("/create")
    public String showCreateClassroomForm(Model model) {
        model.addAttribute("classroom", new Classroom());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("lecturers", appUserService.findAllLecturer());

        return "classroom/create";
    }

    @PostMapping("/create")
    public String saveClassroom(@ModelAttribute("classroom") Classroom classroom,
                                RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        boolean classroomExist = classroomService.findByClassCode(classroom.getClassCode()).isPresent();
        if (classroomExist) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp học đã tồn tại");
        } else {
            classroomService.save(classroom);
            redirectAttributes.addAttribute("successMessage", "Tạo lớp học thành công");
        }

        systemLogService.saveLog(ActionType.CREATE, currentUser.getUsername(), "thêm lớp học mới " + classroom.getClassName());

        return "redirect:/classrooms/create";
    }

    @GetMapping("/{classCode}/edit")
    public String showClassroomDetail(
            @PathVariable("classCode") String classCode,
            Model model) {

        Classroom classroom = classroomService.findByClassCode(classCode).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy lớp học: "));

        List<Student> students = classroom.getStudents().stream()
                .filter(student -> student.getStudentStatus() == StudentStatus.STUDYING
                        || student.getStudentStatus() == StudentStatus.SUSPENDED)
                .collect(Collectors.toList());

        List<ClassDiary> diaries = classroomService.findAllDiariesByClassroom(classroom);
        List<Lecturer> lecturers = appUserService.findAllLecturer();
        List<Course> courses = courseService.findAll();
        Lecturer classroomLecturer = classroom.getLecturer();
        Course classroomCourse = classroom.getCourse();

        model.addAttribute("lecturer", classroomLecturer);
        model.addAttribute("course", classroomCourse);

        model.addAttribute("courses", courses);
        model.addAttribute("lecturers", lecturers);
        model.addAttribute("classroom", classroom);
        model.addAttribute("students", students);
        model.addAttribute("classDiaries", diaries);

        ClassroomDTO dto = new ClassroomDTO();
        dto.setId(classroom.getId());
        dto.setClassName(classroom.getClassName());

        if (classroom.getCourse() != null) {
            dto.setCourseId(classroom.getCourse().getId());
        }
        if (classroom.getLecturer() != null) {
            dto.setLecturerId(classroom.getLecturer().getId());
        }

        model.addAttribute("classroomDTO", dto);

        return "classroom/edit";
    }

    @PostMapping("/{classCode}/edit")
    public String updateClassroom(@PathVariable("classCode") String classCode,
                                  @Valid @ModelAttribute("classroomDTO") ClassroomDTO classroomDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model,
                                  @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("subjects", courseService.findAll());
            model.addAttribute("lecturers", appUserService.findAllLecturer());
            return "classroom/edit";
        }

        classroomService.updateClassroom(classCode, classroomDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin lớp học thành công!");

        systemLogService.saveLog(ActionType.UPDATE, currentUser.getUsername(), "cập nhật lớp họp " + classroomDTO.getClassName());

        return "redirect:/classrooms/" + classCode + "/edit";
    }

    @PostMapping("/{classCode}/diaries")
    public String addClassDiary(
            @PathVariable("classCode") String classCode,
            @RequestParam("content") String content,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Classroom classroom = classroomService.findByClassCode(classCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
        AppUser author = appUserService.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        ClassDiary diary = new ClassDiary();
        diary.setContent(content);
        diary.setClassroom(classroom);
        diary.setAuthor(author);

        classroomService.saveClassDiary(diary);

        systemLogService.saveLog(ActionType.OTHER, currentUser.getUsername(), "viết nhật kí lớp học " + classroom.getClassName());

        return "redirect:/classrooms/" + classCode + "/edit?diarySuccess=true";
    }

    @PostMapping("/{classCode}/delete")
    public String deleteClassroom(@PathVariable("classCode") String classCode, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Classroom classroom = classroomService.findByClassCode(classCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
        classroomService.deleteByClassCode(classCode);
        systemLogService.saveLog(ActionType.DELETE, currentUser.getUsername(), "xóa lớp học " + classroom.getClassName());
        return "redirect:/classrooms";
    }

    @PostMapping("/{classCode}/restore")
    public String restoreClassroom(@PathVariable("classCode") String classCode, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Classroom classroom = classroomService.findByClassCode(classCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
        classroomService.restoreByClassCode(classCode);
        systemLogService.saveLog(ActionType.RESTORE, currentUser.getUsername(), "khôi phục lớp học " + classroom.getClassName());
        return "redirect:/classrooms";
    }

    @PostMapping("/{classCode}/students/{studentCode}/remove")
    public String removeStudentFromClassoom(@PathVariable("classCode") String classCode,
                                            @PathVariable("studentCode") String studentCode,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        classroomService.removeStudent(classCode, studentCode);
        systemLogService.saveLog(ActionType.OTHER, currentUser.getUsername(), "xóa học viên " + studentCode + " khỏi lớp học " + classCode);
        return "redirect:/classrooms/" + classCode + "/edit";
    }
}
