package com.codegym.controller.user;

import com.codegym.dto.user.UserCreateDTO;
import com.codegym.dto.user.UserEditDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.mapper.UserMapper;
import com.codegym.model.classroom.Classroom;
import com.codegym.model.course.Course;
import com.codegym.model.system.ActionType;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.Role;
import com.codegym.model.user.student.Student;
import com.codegym.security.CustomUserDetails;
import com.codegym.service.system.SystemLogService;
import com.codegym.service.user.role.RoleService;
import com.codegym.service.classroom.ClassroomService;
import com.codegym.service.course.CourseService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final AppUserService appUserService;
    private final ClassroomService classroomService;
    private final RoleService roleService;
    private final CourseService courseService;
    private final UserMapper userMapper;
    private final SystemLogService systemLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping({"", "/"})
    public String showUserList(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                               @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                               @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                               @RequestParam(name = "roleId", required = false) Long roleId,
                               @RequestParam(value = "sort", defaultValue = "latest") String sortType,
                               Model model) {

        Sort sort = switch (sortType) {
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "fullName"); // Tên A-Z
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "fullName"); // Tên Z-A
            case "role_asc" -> Sort.by(Sort.Direction.ASC, "role");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AppUser> userPage = appUserService.searchUsers(keyword, roleId, pageable);
        List<Role> roles = roleService.findAll();

        model.addAttribute("users", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        model.addAttribute("roles", roles);
        model.addAttribute("roleId", roleId);
        model.addAttribute("currentSort", sortType);

        return "user/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        AppUser user = appUserService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng id: " + id));

        if (user.getRole().getName().equals("ROLE_STUDENT")) {
            Student student = (Student) user;
            return "redirect:/students/" + student.getStudentCode() + "/edit";
        }

        UserEditDTO dto = userMapper.toDto(user);

        model.addAttribute("user", dto);

        return "user/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateUserInfo(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("user") UserEditDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            return "user/edit";
        }

        appUserService.updateUser(id, dto);
        redirectAttributes.addFlashAttribute("message", "Cập nhật thành công");

        systemLogService.saveLog(ActionType.UPDATE, currentUser.getUsername(), "cập nhật thông tin người dùng " + dto.getEmail());

        return "redirect:/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new UserCreateDTO());
        List<Role> roles = roleService.findAll();
        List<Classroom> classrooms = classroomService.findAll();
        List<Course> courses = courseService.findAll();
        model.addAttribute("roleList", roles);
        model.addAttribute("classroomList", classrooms);
        model.addAttribute("courseList", courses);

        return "user/create";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("user") UserCreateDTO dto,
                             BindingResult bindingResult, Model model,
                             RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roleList", roleService.findAll());
            model.addAttribute("classroomList", classroomService.findAll());
            model.addAttribute("subjectList", courseService.findAll());

            return "user/create";
        }

        boolean accountExists = appUserService.findByEmail(dto.getEmail()).isPresent();
        if (accountExists) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản người dùng đã tồn tại");
            return "redirect:/users/create";
        }

        appUserService.saveUser(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản người dùng thành công");

        systemLogService.saveLog(ActionType.CREATE, currentUser.getUsername(), "tạo người dùng mới " + dto.getEmail() + " với quyền " + roleService.findById(dto.getRoleId()).get().getDescription());

        return "redirect:/users/create";
    }
}
