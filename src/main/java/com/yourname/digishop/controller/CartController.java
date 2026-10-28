package com.yourname.digishop.controller;

import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public String view(Principal principal, Model model) {
        model.addAttribute("cart", cartService.getOrCreateCart(principal.getName()));
        return "cart";
    }


    @PostMapping("/add")
    public String add(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Principal principal,
            RedirectAttributes ra) {

        try {
            cartService.addItem(
                    principal.getName(), productId, quantity);

            ra.addFlashAttribute(
                    "message", "Đã thêm vào giỏ hàng");

        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cards";
    }


    @PostMapping("/update")
    public String update(@RequestParam Long itemId, @RequestParam int quantity,
                         Principal principal, RedirectAttributes ra) {
        try {
            cartService.updateQuantity(principal.getName(), itemId, quantity);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long itemId, Principal principal, RedirectAttributes ra) {
        try {
            cartService.removeItem(principal.getName(), itemId);
            ra.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }
}