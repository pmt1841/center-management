package com.codegym.controller.auth;

import com.codegym.service.user.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final AppUserService appUserService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/forgot-password")
    public String showForgetPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgetPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        appUserService.updateUserPassword(email);

        redirectAttributes.addFlashAttribute("message","Thành công! Vui lòng đăng nhập bằng mật khẩu đã nhận.");
        return "redirect:/login";
    }

}
