package com.yourname.digishop.controller.admin;

import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService service;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", service.search(keyword, page));
        model.addAttribute("keyword", keyword);
        return "admin/products";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("product", service.get(id));
            return "admin/product-edit";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam BigDecimal price,
                         @RequestParam int stock,
                         @RequestParam(defaultValue = "false") boolean active,
                         @RequestParam(required = false) String name,
                         RedirectAttributes ra) {
        try {
            service.update(id, price, stock, active, name);
            ra.addFlashAttribute("message", "Đã cập nhật sản phẩm #" + id);
            return "redirect:/admin/products";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (OptimisticLockingFailureException e) {
            ra.addFlashAttribute("error", "Sản phẩm vừa được thay đổi (có thể do đơn hàng mới), hãy tải lại rồi sửa");
        }
        return "redirect:/admin/products/" + id + "/edit";
    }

    @PostMapping("/{id}/image")
    public String uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                              RedirectAttributes ra) {
        try {
            service.saveImage(id, file);
            ra.addFlashAttribute("message", "Đã cập nhật ảnh");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Không lưu được ảnh, thử lại");
        }
        return "redirect:/admin/products/" + id + "/edit";
    }
}