package com.hyprbank.online.bancavirtual.controller;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.service.UsuarioService;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Para interactuar con la vista
import org.springframework.web.bind.annotation.GetMapping;

/*
 * Controlador de Spring MVC para la gestion de registro y paginas de inicio.
 *
 * Esta clase maneja las solicitudes relacionadas con el inicio de sesion y la pagina principal
 * del sistema, devolviendo nombres de vistas (templates HTML).
 *
 * La anotacion @Controller indica que esta clase es un componente de controlador de Spring MVC.
 */
@Controller // Indica que esta clase es un controlador de Spring MVC
public class RegistroController { // Renombrado a RegistroController

    private final UsuarioService usuarioService; // Renombrado el campo para consistencia

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara la instancia de UsuarioService.
     */
    @Autowired
    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Maneja las solicitudes GET a la ruta "/login".
     * Redirige al usuario a la pagina de inicio de sesion (login.html).
     *
     * @return El nombre de la vista "login".
     */
    @GetMapping("/login")
    public String iniciarSesion() {
        return "login";
    }

    /**
     * Maneja las solicitudes GET a la ruta raiz "/".
     * Carga una lista de usuarios y la anade al modelo para ser mostrada en la pagina de inicio.
     *
     * @param modelo El objeto Model de Spring MVC para pasar datos a la vista.
     * @return El nombre de la vista "index".
     */
    @GetMapping("/")
    public String verPaginaDeInicio(Model modelo) {
        // Anadir la lista de usuarios al modelo para que sea accesible en la vista "index.html".
        modelo.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "index";
    }
}
    