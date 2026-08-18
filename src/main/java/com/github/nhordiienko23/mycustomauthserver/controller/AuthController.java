package com.github.nhordiienko23.mycustomauthserver.controller;

import com.github.nhordiienko23.mycustomauthserver.dto.RegisterRequest;
import com.github.nhordiienko23.mycustomauthserver.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }


    @PostMapping("/register")
    public String registerUser(RegisterRequest registerRequest, Model model) {
        try {
            userService.register(registerRequest);
            model.addAttribute("success", "Account created successfully! You can now log in.");
            return "register";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        model.addAttribute("username", principal.getName());
        return "home";
    }
}