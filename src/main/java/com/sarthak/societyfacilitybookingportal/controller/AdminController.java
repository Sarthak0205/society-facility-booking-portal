package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.entity.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        Object userId = session.getAttribute("userId");
        Object userRole = session.getAttribute("userRole");

        // Not logged in
        if (userId == null) {
            return "redirect:/users/login";
        }

        // Logged in but not an admin
        if (userRole != UserRole.ADMIN) {
            return "redirect:/facilities";
        }

        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );

        return "admin/dashboard";
    }
}