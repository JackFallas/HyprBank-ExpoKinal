package com.hyprbank.online.bancavirtual.hyprbank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard/user") // Esta es la URL a la que redirige AccesoRol para USER
    public String showUserDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userService.findByEmail(userEmail);

        if (user != null) {
            model.addAttribute("userName", user.getFirstName());
            model.addAttribute("userLastName", user.getLastName());
            // ... más datos específicos de usuario ...
        }
        return "Usuario"; // Devuelve Usuario.html
    }
}
