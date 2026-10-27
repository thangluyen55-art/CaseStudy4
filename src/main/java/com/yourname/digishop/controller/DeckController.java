package com.yourname.digishop.controller;

import com.yourname.digishop.entity.Deck;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/decks")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService service;

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("decks", service.list(principal.getName()));
        return "decks";
    }

    @PostMapping
    public String create(@RequestParam(required = false) String name, Principal principal) {
        Deck d = service.create(principal.getName(), name);
        return "redirect:/decks/" + d.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String q,
                         Principal principal, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("deck", service.get(id, principal.getName()));
            model.addAttribute("results", service.searchCards(q));
            model.addAttribute("q", q);
            return "deck-detail";
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/decks";
        }
    }

    @PostMapping("/{id}/add")
    public String add(@PathVariable Long id, @RequestParam Long cardId,
                      @RequestParam(required = false) String q,
                      Principal principal, RedirectAttributes ra) {
        try {
            service.addCard(id, principal.getName(), cardId);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return back(id, q, ra);
    }

    @PostMapping("/{id}/remove")
    public String remove(@PathVariable Long id, @RequestParam Long cardId,
                         @RequestParam(required = false) String q,
                         Principal principal, RedirectAttributes ra) {
        try {
            service.removeCard(id, principal.getName(), cardId);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return back(id, q, ra);
    }

    @PostMapping("/{id}/to-cart")
    public String toCart(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            var r = service.addToCart(id, principal.getName());
            if (r.added() > 0)
                ra.addFlashAttribute("message", "Đã thêm " + r.added() + " thẻ vào giỏ hàng");
            if (!r.shortages().isEmpty())
                ra.addFlashAttribute("error", "Không đủ hàng: " + String.join("; ", r.shortages()));
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return back(id, null, ra);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            service.delete(id, principal.getName());
            ra.addFlashAttribute("message", "Đã xóa bộ bài");
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/decks";
    }

    // Quay lại trang sửa deck, giữ nguyên từ khóa tìm kiếm
    private String back(Long id, String q, RedirectAttributes ra) {
        ra.addAttribute("id", id);
        if (q != null && !q.isBlank()) ra.addAttribute("q", q);
        return "redirect:/decks/{id}";
    }
}