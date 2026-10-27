package com.yourname.digishop.controller.admin;

import com.yourname.digishop.entity.DiscountType;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {
    private final CouponService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coupons", service.findAll());
        model.addAttribute("types", DiscountType.values());
        return "admin/coupons";
    }

    @PostMapping
    public String create(@RequestParam String code,
                         @RequestParam DiscountType discountType,
                         @RequestParam BigDecimal discountValue,
                         @RequestParam(required = false) BigDecimal minOrderValue,
                         @RequestParam(required = false) Integer usageLimit,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
                         RedirectAttributes ra) {
        try {
            service.create(code, discountType, discountValue, minOrderValue, usageLimit, expiresAt);
            ra.addFlashAttribute("message", "Đã tạo mã " + code.trim().toUpperCase());
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.toggle(id);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }
}