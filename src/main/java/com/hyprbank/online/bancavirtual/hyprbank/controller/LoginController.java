package com.hyprbank.online.bancavirtual.hyprbank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /**
     * Maneja las solicitudes GET a la ruta "/login".
     * Muestra el formulario de inicio de sesión.
     * La lógica de autenticación (procesamiento del POST) es manejada
     * directamente por Spring Security y no necesita un método @PostMapping aquí.
     *
     * @return El nombre de la vista "login" (que corresponde a login.html).
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Esto buscará src/main/resources/templates/login.html
    }

    /**
     * Opcional: Si tienes una página de dashboard para usuarios logueados,
     * este controlador podría manejarla también.
     *
     * @return El nombre de la vista "dashboard" (que corresponde a dashboard.html o user-dashboard.html).
     */

    // Si tu página principal "/" también debe mostrarse desde un controlador
    // (aunque ya tienes welcome page mapping), podrías añadirla aquí.
    // @GetMapping("/")
    // public String showIndex() {
    //     return "index";
    // }
}
