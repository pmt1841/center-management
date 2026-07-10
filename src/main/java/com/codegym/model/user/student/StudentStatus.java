package com.codegym.model.user.student;

import jakarta.persistence.Table;

@Table
public enum StudentStatus {
    STUDYING,           // Đang học
    WAITING_TRANSFER,   // Chờ chuyển lớp
    SUSPENDED,          // Đình chỉ
    DROPPED_OUT,        // Thôi học
}