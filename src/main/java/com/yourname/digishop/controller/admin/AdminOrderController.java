package com.yourname.digishop.controller.admin;

import com.yourname.digishop.entity.Order;
import com.yourname.digishop.entity.OrderStatus;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final AdminOrderService service;

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", service.search(status, page));
        model.addAttribute("status", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Order order = service.get(id);
            model.addAttribute("order", order);
            model.addAttribute("nextStatuses", service.nextStatuses(order.getStatus()));
            return "admin/order-detail";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam OrderStatus status,
                               RedirectAttributes ra) {
        try {
            service.changeStatus(id, status);
            ra.addFlashAttribute("message", "Đã chuyển đơn #" + id + " sang " + status);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}