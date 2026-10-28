package com.yourname.digishop.controller;

import com.yourname.digishop.dto.RegisterForm;
import com.yourname.digishop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }


    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult result,
            RedirectAttributes ra) {

        if (!Objects.equals(
                form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue(
                    "confirmPassword",
                    "mismatch",
                    "Mật khẩu nhập lại không khớp");
        }

        if (userService.emailExists(form.getEmail())) {
            result.rejectValue(
                    "email",
                    "exists",
                    "Email này đã được sử dụng");
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerCustomer(form);
        } catch (DataIntegrityViolationException ex) {
            // Database unique constraint vẫn là lớp bảo vệ cuối cùng.
            result.rejectValue(
                    "email",
                    "exists",
                    "Email này đã được sử dụng");

            return "register";
        }

        ra.addFlashAttribute(
                "message", "Đăng ký thành công, hãy đăng nhập");

        return "redirect:/login";
    }

}