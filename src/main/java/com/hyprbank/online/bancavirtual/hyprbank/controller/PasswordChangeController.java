package com.hyprbank.online.bancavirtual.hyprbank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PasswordChangeController {

    /**
     * Maneja las solicitudes GET a la ruta "/change-password".
     * Esta ruta será utilizada cuando un usuario necesite cambiar su contraseña
     * por primera vez o cuando el administrador lo haya forzado.
     *
     * @return El nombre de la vista "change_password" (que corresponde a change_password.html).
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change_password"; // Esto buscará src/main/resources/templates/change_password.html
    }
}
