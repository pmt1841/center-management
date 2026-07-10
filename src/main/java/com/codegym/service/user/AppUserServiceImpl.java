package com.codegym.service.user;

import com.codegym.dto.user.ProfileUpdateDTO;
import com.codegym.dto.user.UserCreateDTO;
import com.codegym.dto.user.UserEditDTO;
import com.codegym.exception.ResourceNotFoundException;
import com.codegym.mapper.UserMapper;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.Role;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import com.codegym.model.user.student.StudentStatus;
import com.codegym.repository.classroom.ClassroomRepository;
import com.codegym.repository.user.AppUserRepository;
import com.codegym.repository.user.RoleRepository;
import com.codegym.repository.user.lecturer.LecturerRepository;
import com.codegym.service.cloud.EmailService;
import com.codegym.service.tuition.TuitionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final LecturerRepository lecturerRepository;
    private final UserMapper userMapper;
    private final TuitionService tuitionService;
    private final ClassroomRepository classroomRepository;

    private final String DEFAULT_PASSWORD = "123456";

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    @Override
    public void saveUser(UserCreateDTO dto) {
        Long roleId = dto.getRoleId();
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quyền"));

        AppUser newUser = switch (role.getName()) {
            case "ROLE_ADMIN", "ROLE_MINISTRY" -> new AppUser();
            case "ROLE_STUDENT" -> new Student();
            case "ROLE_LECTURER" -> new Lecturer();
            default -> throw new IllegalArgumentException("Quyền không hợp lệ!");
        };

        userMapper.mapToUser(dto, newUser);

        newUser.setRole(role);
        newUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

        AppUser savedUser = appUserRepository.save(newUser);
        Long generatedId = savedUser.getId();
        boolean needUpdate = true;

        if (savedUser instanceof Student student) {
            student.setStudentCode("HV" + generatedId);
            if (dto.getClassroomId() != null) {
                student.setStudentStatus(StudentStatus.STUDYING);
                student.setClassroom(classroomRepository.findById(dto.getClassroomId()).orElse(null));
                tuitionService.enrollStudentAndGenerateTuition(student.getId(), student.getClassroom().getCourse().getId(), dto.getEnrollmentDate());
            }
        } else if (savedUser instanceof Lecturer lecturer) {
            lecturer.setLecturerCode("GV" + generatedId);
        } else {
            needUpdate = false;
        }

        if (needUpdate) {
            appUserRepository.save(savedUser);
        }

        emailService.sendAccountInformation(savedUser.getEmail(), savedUser.getFullName(), DEFAULT_PASSWORD, role.getDescription());
    }

    @Override
    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    @Override
    public Optional<AppUser> findById(long id) {
        return appUserRepository.findById(id);
    }

    @Override
    public Page<AppUser> searchUsers(String keyword, Long roleId, Pageable pageable) {
        String safeKeyword = (keyword == null) ? "" : keyword.trim();
        return appUserRepository.searchUsers(safeKeyword, roleId, pageable);
    }

    @Override
    @Transactional
    public void updateUser(long id, UserEditDTO dto) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id: " + id));

        userMapper.mapToUser(dto, user);

        appUserRepository.save(user);
    }

    @Override
    public long countLecturer() {
        return lecturerRepository.count();
    }

    @Override
    public long countMinistry() {
        return appUserRepository.countAppUserByRole_Name("ROLE_MINISTRY");
    }

    @Override
    public List<Lecturer> findAllLecturer() {
        return lecturerRepository.findAll();
    }

    @Override
    @Transactional
    public void updateUserPassword(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản email: " + email));

        user.setPassword(passwordEncoder.encode("123456"));
        emailService.sendResetPasswordNotification(user.getEmail(), user.getPassword());
        appUserRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp!");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDTO dto) {
        AppUser existingUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        userMapper.updateProfileFromDto(dto, existingUser);

        appUserRepository.save(existingUser);
    }
}
