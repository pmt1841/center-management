package com.codegym.controller.user;

import com.codegym.dto.user.ProfileUpdateDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.student.Student;
import com.codegym.security.CustomUserDetails;
import com.codegym.service.cloud.UploadImageService;
import com.codegym.service.user.AppUserService;
import com.codegym.service.user.student.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final AppUserService appUserService;
    private final StudentService studentService;

    @GetMapping("/profile")
    public String showProfile(Model model, @ModelAttribute("currentUser") AppUser currentUser) {
        AppUser user = appUserService.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        model.addAttribute("user", user);
        switch (user.getRole().getName()) {
            case "ROLE_STUDENT":
                model.addAttribute("examScores", studentService.getExamScoreByStudent((Student) user));
                break;
        }
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("user") ProfileUpdateDTO dto,
                                BindingResult bindingResult,
                                @ModelAttribute("currentUser") AppUser currentUser,
                                Model model,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            AppUser user = appUserService.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            model.addAttribute("user", user);
            if ("ROLE_STUDENT".equals(currentUser.getRole().getName())) {
                model.addAttribute("examScores", studentService.getExamScoreByStudent((Student) currentUser));
            }
            return "user/profile";
        }

        try {
            String avatarUrl = UploadImageService.uploadFile(avatarFile, "user_avatar");
            dto.setAvatarUrl(avatarUrl);
            appUserService.updateProfile(currentUser.getId(), dto);

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (currentAuth != null && currentAuth.getPrincipal() instanceof CustomUserDetails userDetails) {

                userDetails.setAvatarUrl(avatarUrl);

                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        currentAuth.getCredentials(),
                        currentAuth.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @ModelAttribute("currentUser") AppUser currentUser,
                                 RedirectAttributes redirectAttributes) {

        try {
            appUserService.changePassword(currentUser.getId(), oldPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Vui lòng sử dụng mật khẩu mới cho lần đăng nhập sau.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi hệ thống xảy ra khi đổi mật khẩu.");
        }

        return "redirect:/profile";
    }
}
