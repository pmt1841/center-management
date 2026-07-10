package com.codegym.security;

import com.codegym.model.user.AppUser;
import com.codegym.model.user.lecturer.Lecturer;
import com.codegym.model.user.student.Student;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final AppUser appUser;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = appUser.getRole().getName();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    public String getFullName() {
        return appUser.getFullName();
    }

    public Long getId() {
        return appUser.getId();
    }

    public String getUserCode() {
        String roleName = appUser.getRole().getName();
        return switch (roleName) {
            case "ROLE_STUDENT" -> {
                Student student = (Student) appUser;
                yield student.getStudentCode();
            }
            case "ROLE_LECTURER" -> {
                Lecturer lecturer = (Lecturer) appUser;
                yield lecturer.getLecturerCode();
            }
            default -> "";
        };
    }

    public void setAvatarUrl(String avatarUrl) {
        this.appUser.setAvatarUrl(avatarUrl);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
