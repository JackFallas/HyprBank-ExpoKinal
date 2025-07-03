package com.hyprbank.online.bancavirtual.hyprbank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Controller // ¡IMPORTANTE! Cambiado a @Controller
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // Este método es para la vista (HTML)
    @GetMapping("/dashboard/admin")
    public String showAdminDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User adminUser = userService.findByEmail(userEmail);

        if (adminUser != null) {
            model.addAttribute("userName", adminUser.getFirstName());
            model.addAttribute("userLastName", adminUser.getLastName());
        }

        List<User> allUsers = userService.listUsers();
        model.addAttribute("users", allUsers);

        return "Admin"; // Devuelve el nombre de la plantilla Thymeleaf "Admin.html"
    }
}