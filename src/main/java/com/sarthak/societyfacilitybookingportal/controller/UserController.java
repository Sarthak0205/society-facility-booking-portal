package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        String result = userService.registerUser(
                fullName,
                email,
                password,
                confirmPassword
        );

        if (result.equals("Registration successful!")) {
            model.addAttribute("success", result);
        } else {
            model.addAttribute("error", result);
        }

        return "register";
    }
}