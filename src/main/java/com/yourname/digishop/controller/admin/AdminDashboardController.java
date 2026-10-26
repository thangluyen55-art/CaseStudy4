package com.yourname.digishop.controller.admin;

import com.yourname.digishop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/admin")
    public String home() {
        return "redirect:/admin/orders";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("d", dashboardService.build());
        return "admin/dashboard";
    }
}