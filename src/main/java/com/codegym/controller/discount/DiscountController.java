package com.codegym.controller.discount;

import com.codegym.model.discount.DiscountCode;
import com.codegym.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/discounts")
@RequiredArgsConstructor
public class DiscountController {
    private final DiscountService discountService;

    @GetMapping
    public String listDiscounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            Model model) {

        List<DiscountCode> discounts;

        // Xử lý logic tìm kiếm & lọc
        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());

        if (hasKeyword && status != null) {
            // Có cả từ khóa và trạng thái
            discounts = discountService.findByCodeContainingIgnoreCaseAndActive(keyword.trim(), status);
        } else if (hasKeyword) {
            // Chỉ có từ khóa
            discounts = discountService.findByCodeContainingIgnoreCase(keyword.trim());
        } else if (status != null) {
            // Chỉ có trạng thái
            discounts = discountService.findByActive(status);
        } else {
            // Không lọc gì cả -> Lấy tất cả
            discounts = discountService.findAll();
        }

        // Đẩy dữ liệu sang View
        model.addAttribute("discounts", discounts);

        // Đẩy lại keyword và status để giữ trạng thái cho form tìm kiếm trên giao diện
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "discount/list"; // Đường dẫn trỏ tới file list.html của bạn
    }

    @PostMapping("/create")
    public String create(@ModelAttribute DiscountCode discountCode, RedirectAttributes redirectAttributes) {
        try {
            discountService.save(discountCode);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Mã này có thể đã tồn tại.");
        }
        return "redirect:/admin/discounts";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        DiscountCode dc = discountService.findById(id);
        if (dc == null) return "redirect:/admin/discounts";
        model.addAttribute("discount", dc);
        return "discount/edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute DiscountCode discountCode, RedirectAttributes ra) {
        discountService.update(id, discountCode);
        ra.addFlashAttribute("successMessage", "Cập nhật mã giảm giá thành công!");
        return "redirect:/admin/discounts";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        discountService.toggleStatus(id);
        ra.addFlashAttribute("successMessage", "Đã thay đổi trạng thái mã giảm giá.");
        return "redirect:/admin/discounts";
    }
}
