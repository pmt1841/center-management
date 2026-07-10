package com.codegym.model.discount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "discount_codes")
@Data
@NoArgsConstructor
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // VD: FULLPAY2024, TET2024

    // Loại giảm giá: theo phần trăm (PERCENTAGE) hoặc số tiền cố định (AMOUNT)
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType type;

    private Double discountValue; // VD: 10 (10%) hoặc 2000000 (2 triệu)

    // Giới hạn lượt sử dụng
    private Integer maxUsage; // Tổng số lượt được phép dùng (Null nếu không giới hạn)
    private Integer currentUsage = 0; // Số lượt đã sử dụng

    // Giới hạn thời gian
    @Column(name = "valid_from")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime validUntil;

    private boolean active = true; // Trạng thái bật/tắt thủ công của Admin
}
