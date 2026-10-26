package com.yourname.digishop.controller;

import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("orders", orderService.getOrdersOfUser(principal.getName()));
        return "orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal,
                         Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("order", orderService.getOrderOfUser(id, principal.getName()));
            return "order-detail";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            orderService.cancelOrder(id, principal.getName());
            ra.addFlashAttribute("message", "Đã hủy đơn hàng #" + id);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}