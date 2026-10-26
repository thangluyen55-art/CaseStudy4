package com.yourname.digishop.controller;

import com.yourname.digishop.dto.CheckoutForm;
import com.yourname.digishop.entity.Cart;
import com.yourname.digishop.entity.Order;
import com.yourname.digishop.entity.User;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.UserRepository;
import com.yourname.digishop.service.CartService;
import com.yourname.digishop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository users;

    @GetMapping
    public String form(Principal principal, Model model, RedirectAttributes ra) {
        Cart cart = cartService.getOrCreateCart(principal.getName());
        if (cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng đang trống");
            return "redirect:/cart";
        }
        if (!model.containsAttribute("form")) {            // điền sẵn thông tin tài khoản
            User u = users.findByEmail(principal.getName()).orElseThrow();
            CheckoutForm f = new CheckoutForm();
            f.setReceiverName(u.getFullName());
            f.setPhone(u.getPhone());
            model.addAttribute("form", f);
        }
        fillModel(model, cart);
        return "checkout";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("form") CheckoutForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            fillModel(model, cartService.getOrCreateCart(principal.getName()));
            return "checkout";
        }
        try {
            Order order = orderService.placeOrder(principal.getName(), form);
            ra.addFlashAttribute("message", "Đặt hàng thành công! Mã đơn #" + order.getId());
            return "redirect:/orders/" + order.getId();
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        } catch (OptimisticLockingFailureException e) {
            ra.addFlashAttribute("error", "Có người vừa mua cùng sản phẩm, vui lòng thử lại");
            return "redirect:/cart";
        }
    }

    private void fillModel(Model model, Cart cart) {
        model.addAttribute("cart", cart);
        model.addAttribute("shippingFee", OrderService.SHIPPING_FEE);
    }
}