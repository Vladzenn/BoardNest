package com.boardinghouse.controller;

import com.boardinghouse.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final AccountService accounts;
    public AuthController(AccountService accounts) { this.accounts = accounts; }

    @GetMapping("/") public String home() { return "redirect:/properties"; }
    @GetMapping("/login") public String login() { return "login"; }
    @GetMapping("/register") public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String fullName, @RequestParam String username,
            @RequestParam String password, @RequestParam String confirmPassword, Model model,
            RedirectAttributes redirect) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "The passwords do not match.");
            model.addAttribute("fullName", fullName); model.addAttribute("username", username);
            return "register";
        }
        try {
            accounts.register(fullName, username, password);
            redirect.addFlashAttribute("notice", "Account created. You can now sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("fullName", fullName); model.addAttribute("username", username);
            return "register";
        }
    }
}
