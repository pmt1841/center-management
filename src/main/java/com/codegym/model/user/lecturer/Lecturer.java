package com.codegym.model.user.lecturer;

import com.codegym.model.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lecturers")
@Data
@RequiredArgsConstructor
public class Lecturer extends AppUser {
    @Column(unique = true)
    private String lecturerCode;
}
