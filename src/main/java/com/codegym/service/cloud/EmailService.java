package com.codegym.service.cloud;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private void sendMail(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        javaMailSender.send(message);
    }

    @Async
    public void sendAccountInformation(String toEmail, String fullName, String rawPassword, String roleName) {
        String role = switch (roleName) {
            case "ROLE_ADMIN" -> "Quản trị viên";
            case "ROLE_MINISTRY" -> "Giáo vụ";
            case "ROLE_LECTURER" -> "Giảng viên";
            case "ROLE_STUDENT" -> "Học viên";
            default -> "Người dùng";
        };

        String content = "Xin chào " + fullName + ",\n\n"
                + "Tài khoản của bạn trên Hệ thống Quản lý đã được tạo thành công với quyền: " + role + ".\n\n"
                + "Dưới đây là thông tin đăng nhập của bạn:\n"
                + "- Email (Tên đăng nhập): " + toEmail + "\n"
                + "- Mật khẩu: " + rawPassword + "\n\n"
                + "Vui lòng đăng nhập và đổi mật khẩu ngay trong lần đầu tiên để bảo mật tài khoản.\n\n"
                + "Trân trọng,\n"
                + "Ban Quản Trị Hệ Thống.";

        sendMail(toEmail, "THÔNG TIN TÀI KHOẢN ĐĂNG NHẬP HỆ THỐNG", content);
    }

    @Async
    public void sendTuitionReminder(String toEmail, String studentName, String month) {
        String content = "Chào " + studentName + ",\n\n"
                + "Phòng Giáo vụ xin thông báo: Học phí tháng " + month + " của bạn sắp đến hạn thanh toán.\n"
                + "Vui lòng hoàn tất nghĩa vụ học phí trước ngày cuối cùng của tháng để không ảnh hưởng đến tiến độ học tập.\n\n"
                + "Trân trọng,\n"
                + "Trung tâm CodeGym.";

        sendMail(toEmail, "THÔNG BÁO: Đóng học phí tháng " + month, content);
    }

    @Async
    public void sendResetPasswordNotification(String toEmail, String fullName) {
        String content = "Xin chào " + fullName + ",\n\n"
                + "Tài khoản của bạn trên Hệ thống Quản lý đã được đặt lại mật khẩu.\n\n"
                + "Dưới đây là mật khẩu đăng nhập của bạn:\n"
                + "- Email (Tên đăng nhập): " + toEmail + "\n"
                + "- Mật khẩu: 123456\n\n"
                + "Vui lòng đăng nhập và đổi mật khẩu ngay để bảo mật tài khoản.\n\n"
                + "Trân trọng,\n"
                + "Ban Quản Trị Hệ Thống.";

        sendMail(toEmail, "ĐẶT LẠI MẬT KHẨU ĐĂNG NHẬP HỆ THỐNG", content);
    }
}
