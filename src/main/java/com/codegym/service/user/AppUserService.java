package com.codegym.service.user;

import com.codegym.dto.user.ProfileUpdateDTO;
import com.codegym.dto.user.UserCreateDTO;
import com.codegym.dto.user.UserEditDTO;
import com.codegym.model.user.AppUser;
import com.codegym.model.user.lecturer.Lecturer;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AppUserService {
    Optional<AppUser> findByEmail(String email);

    void saveUser(UserCreateDTO dto);

    List<AppUser> findAll();

    Optional<AppUser> findById(long id);

    Page<AppUser> searchUsers(String keyword, Long roleId, Pageable pageable);

    void updateUser(long id, UserEditDTO dto);

    long countLecturer();

    long countMinistry();

    List<Lecturer> findAllLecturer();

    void updateUserPassword(String email);

    void changePassword(Long id, String oldPassword, String newPassword, String confirmPassword);

    void updateProfile(Long userId, ProfileUpdateDTO dto);

}
