package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * Controlador de Spring MVC para las paginas principales de la aplicacion.
 *
 * Esta clase maneja las solicitudes relacionadas con la pagina de inicio (index)
 * y el dashboard del usuario autenticado.
 *
 * @Controller indica que esta clase es un componente de controlador de Spring MVC,
 * que principalmente devuelve nombres de vistas (plantillas HTML).
 */
@Controller
public class HomeController {

    private final UserService userService;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara la instancia de UserService.
     */
    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Maneja las solicitudes GET a la ruta raiz "/".
     * Esta es la pagina de bienvenida o de inicio para usuarios no autenticados.
     *
     * @param model El objeto Model de Spring MVC para pasar datos a la vista.
     * @return El nombre de la vista "index" (src/main/resources/templates/index.html).
     */
    @GetMapping("/")
    public String viewHomePage(Model model) { // Nombre de metodo actualizado
        // Puedes anadir logica aqui si necesitas datos para la pagina de inicio
        // Por ejemplo, si quieres mostrar algo diferente si el usuario esta autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
            // Si el usuario esta autenticado, podrias redirigirlo al dashboard o mostrar un mensaje diferente
            // model.addAttribute("welcomeMessage", "Bienvenido, " + authentication.getName());
        } else {
            // Usuario no autenticado
            // model.addAttribute("welcomeMessage", "Bienvenido a HyprBank. Por favor, inicia sesion.");
        }
        return "index"; // Devuelve el nombre de la plantilla Thymeleaf: src/main/resources/templates/index.html
    }

    /**
     * Maneja las solicitudes GET a la ruta "/dashboard".
     * Esta es la pagina principal para los usuarios autenticados.
     * Obtiene la informacion del usuario autenticado y la pasa a la vista.
     *
     * @param model El objeto Model de Spring MVC para pasar datos a la vista.
     * @return El nombre de la vista "user-dashboard" (src/main/resources/templates/user-dashboard.html).
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // El email es el username en Spring Security

        // Buscar el usuario completo por email
        User user = userService.findByEmail(userEmail);

        if (user != null) {
            model.addAttribute("userName", user.getFirstName());
            model.addAttribute("userLastName", user.getLastName());
            // Aqui podrias anadir el saldo de la cuenta principal del usuario,
            // lista de cuentas, etc., obteniendolo del servicio de cuentas.
            // Ejemplo: model.addAttribute("mainBalance", accountService.getBalance(user.getId()));
        } else {
            // Manejar el caso de usuario no encontrado (deberia ser raro despues de la autenticacion)
            model.addAttribute("error", "No se pudo cargar la informacion del usuario.");
        }

        return "user-dashboard"; // ¡CAMBIADO! Ahora devuelve el nombre correcto del archivo HTML
    }
}