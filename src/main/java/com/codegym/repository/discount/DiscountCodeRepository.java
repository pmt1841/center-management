package com.codegym.repository.discount;

import com.codegym.model.discount.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    @Query("SELECT d FROM DiscountCode d WHERE d.active = true " +
            "AND (d.maxUsage IS NULL OR d.currentUsage < d.maxUsage) " +
            "AND (d.validFrom IS NULL OR d.validFrom <= CURRENT_TIMESTAMP) " +
            "AND (d.validUntil IS NULL OR d.validUntil >= CURRENT_TIMESTAMP)")
    List<DiscountCode> findAllValidCode();

    List<DiscountCode> findByCodeContainingIgnoreCaseAndActive(String code, boolean active);

    // Lọc chỉ theo từ khóa
    List<DiscountCode> findByCodeContainingIgnoreCase(String code);

    // Lọc chỉ theo trạng thái
    List<DiscountCode> findByActive(boolean active);
}
