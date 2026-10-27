package com.yourname.digishop.controller;

import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService service;

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("items", service.list(principal.getName()));
        return "wishlist";
    }

    @PostMapping("/toggle")
    public String toggle(@RequestParam Long productId,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         Principal principal, RedirectAttributes ra) {
        try {
            boolean added = service.toggle(principal.getName(), productId);
            ra.addFlashAttribute("message", added ? "Đã thêm vào yêu thích" : "Đã bỏ khỏi yêu thích");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/wishlist");
    }
}