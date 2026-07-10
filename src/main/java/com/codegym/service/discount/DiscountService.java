package com.codegym.service.discount;

import com.codegym.model.discount.DiscountCode;
import com.codegym.repository.discount.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountCodeRepository discountCodeRepository;

    public List<DiscountCode> findAllValidCode() {
        return discountCodeRepository.findAllValidCode();
    }

    public List<DiscountCode> findAll() {
        return discountCodeRepository.findAll();
    }

    public List<DiscountCode> findByActive(Boolean status) {
        return discountCodeRepository.findByActive(status);
    }

    public List<DiscountCode> findByCodeContainingIgnoreCase(String keyword) {
        return discountCodeRepository.findByCodeContainingIgnoreCase(keyword);
    }

    public List<DiscountCode> findByCodeContainingIgnoreCaseAndActive(String keyword, Boolean status) {
        return discountCodeRepository.findByCodeContainingIgnoreCaseAndActive(keyword, status);
    }

    public void save(DiscountCode discountCode) {
        // Có thể thêm logic kiểm tra mã trùng tại đây
        discountCodeRepository.save(discountCode);
    }

    public DiscountCode findById(Long id) {
        return discountCodeRepository.findById(id).orElse(null);
    }

    public void update(Long id, DiscountCode updatedData) {
        DiscountCode existing = findById(id);
        if (existing != null) {
            existing.setCode(updatedData.getCode());
            existing.setType(updatedData.getType());
            existing.setDiscountValue(updatedData.getDiscountValue());
            existing.setMaxUsage(updatedData.getMaxUsage());
            existing.setValidFrom(updatedData.getValidFrom());
            existing.setValidUntil(updatedData.getValidUntil());
            existing.setActive(updatedData.isActive());
            discountCodeRepository.save(existing);
        }
    }

    public void toggleStatus(Long id) {
        DiscountCode dc = findById(id);
        if (dc != null) {
            dc.setActive(!dc.isActive());
            discountCodeRepository.save(dc);
        }
    }
}
