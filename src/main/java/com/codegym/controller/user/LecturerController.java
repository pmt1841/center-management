package com.codegym.controller.user;

import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentStatus;
import com.codegym.repository.user.lecturer.LecturerRepository;
import com.codegym.service.classroom.ClassroomService;
import com.codegym.service.user.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/lecturer")
@RequiredArgsConstructor
public class LecturerController {
    private final StudentService studentService;
    private final LecturerRepository lecturerRepository;
    private final ClassroomService classroomService;

    @GetMapping("/students")
    public String showStudents(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                               @RequestParam(value = "status", required = false) StudentStatus status,
                               @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                               @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                               Model model, @ModelAttribute("currentUser") AppUser currentUser) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Long id = currentUser.getId();
        Lecturer lecturer = lecturerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        Page<Student> studentPage = studentService.searchStudentsByLecturer(keyword, status, pageable, lecturer);

        model.addAttribute("students", studentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        model.addAttribute("status", status);

        List<Classroom> classrooms = classroomService.findAllByLecturer(lecturer);
        model.addAttribute("classrooms", classrooms);

        return "student/list";
    }

    @GetMapping("/classrooms")
    public String showClassroomList(@RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(required = false, value = "page", defaultValue = "0") int page,
                                    @RequestParam(required = false, value = "size", defaultValue = "10") int size,
                                    Model model, @ModelAttribute("currentUser") AppUser currentUser) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("classCode").ascending());

        Long id = currentUser.getId();
        Lecturer lecturer = lecturerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        Page<Classroom> classroomPage = classroomService.searchClassroomsByLecturer(keyword, pageable, lecturer);

        model.addAttribute("classrooms", classroomPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        return "classroom/list";
    }
}
