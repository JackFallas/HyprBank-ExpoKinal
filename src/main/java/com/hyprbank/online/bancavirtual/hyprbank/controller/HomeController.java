package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;
import com.hyprbank.online.bancavirtual.hyprbank.model.User; // Asegúrate de que esta es tu entidad User

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * Controlador de Spring MVC para las páginas principales de la aplicación.
 *
 * Esta clase maneja las solicitudes relacionadas con la página de inicio (index)
 * y el dashboard del usuario autenticado.
 *
 * @Controller indica que esta clase es un componente de controlador de Spring MVC,
 * que principalmente devuelve nombres de vistas (plantillas HTML).
 */
@Controller
public class HomeController {

    private final UserService userService;

    /*
     * Constructor para la inyección de dependencias.
     * Spring inyectará la instancia de UserService.
     */
    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Maneja las solicitudes GET a la ruta raíz "/".
     *
     * Con la configuración actual de Spring Security, si un usuario no está autenticado
     * y accede a "/", Spring Security lo interceptará y lo redirigirá a "/login"
     * ANTES de que este método sea ejecutado.
     *
     * Si el usuario está autenticado y accede a "/", este método se ejecutará
     * y mostrará la página "index.html".
     *
     * @param model El objeto Model de Spring MVC para pasar datos a la vista.
     * @return El nombre de la vista "index" (src/main/resources/templates/index.html).
     */
    @GetMapping("/")
    public String viewHomePage(Model model) {
        // En este punto, si el usuario llega aquí, significa que está autenticado
        // o que la ruta "/" fue accedida directamente por un usuario autenticado.
        // La lógica de redirección para no autenticados la maneja Spring Security.

        // Puedes añadir lógica aquí si necesitas datos específicos para la página de inicio
        // para usuarios ya autenticados que decidan volver a la raíz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
            // Si el usuario está autenticado, puedes añadir su nombre al modelo, por ejemplo.
            // model.addAttribute("welcomeMessage", "Bienvenido de nuevo, " + authentication.getName());
        } else {
            // Este bloque es menos probable que se ejecute para usuarios NO autenticados
            // si Spring Security redirige a /login. Pero puede ser útil para depuración
            // o si la lógica de seguridad cambia.
            // model.addAttribute("welcomeMessage", "Bienvenido a HyprBank. Por favor, inicia sesión.");
        }
        return "index"; // Devuelve el nombre de la plantilla Thymeleaf: src/main/resources/templates/index.html
    }

}
