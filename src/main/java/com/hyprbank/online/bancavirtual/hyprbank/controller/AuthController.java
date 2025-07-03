package com.hyprbank.online.bancavirtual.hyprbank.controller; 

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest; 
// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService; 

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/*
 * Controlador de Spring MVC para la gestion de autenticacion y registro de usuarios.
 *
 * Esta clase maneja las solicitudes relacionadas con la visualizacion de los formularios
 * de login y registro, y el procesamiento de las operaciones de registro.
 *
 * @Controller indica que esta clase es un componente de controlador de Spring MVC,
 * que principalmente devuelve nombres de vistas (plantillas HTML).
 * @RequestMapping sin un prefijo de clase significa que los metodos manejaran rutas absolutas
 * como /login o /register.
 */
@Controller
@RequestMapping // No se especifica un prefijo base para el controlador, los mappings son absolutos
public class AuthController {

    @Autowired
    private UserService userService; // Nombre de campo actualizado

    /**
     * Prepara un nuevo objeto {@link RegistrationRequest} para el formulario de registro.
     * Este objeto se vinculara al atributo "user" en la vista.
     *
     * @return Una nueva instancia de {@link RegistrationRequest}.
     */
    @ModelAttribute("user") // Nombre de atributo actualizado
    public RegistrationRequest returnNewUserRegistrationDTO() { // Nombre de metodo actualizado
        return new RegistrationRequest();
    }

    /**
     * Maneja las solicitudes GET a la ruta "/login".
     * Muestra el formulario de inicio de sesion.
     * Spring Security redirigira aqui si se requiere autenticacion.
     *
     * @return El nombre de la vista "login" (login.html).
     */
    @GetMapping("/login")
    public String login() {
        return "login"; // Devuelve el nombre de la plantilla Thymeleaf: src/main/resources/templates/login.html
    }

    /**
     * Maneja las solicitudes GET a la ruta "/register".
     * Muestra el formulario de registro de usuario.
     *
     * @param model El objeto Model de Spring MVC para pasar datos a la vista.
     * @return El nombre de la vista "register" (register.html).
     */
    @GetMapping("/register") // Nombre de ruta actualizado
    public String showRegistrationForm(Model model) { // Nombre de metodo actualizado
        // El @ModelAttribute("user") ya se encarga de anadir un nuevo RegistrationRequest al modelo
        return "register"; // Devuelve el nombre de la plantilla Thymeleaf: src/main/resources/templates/register.html
    }

    /**
     * Maneja las solicitudes POST enviadas al formulario de registro.
     * Recibe los datos del formulario en un {@link RegistrationRequest} y guarda el nuevo usuario.
     * Despues de un registro exitoso, redirige a la pagina de login con un mensaje de exito.
     * Si hay un error, redirige de nuevo al formulario de registro con un mensaje de error.
     *
     * @param registrationDTO El {@link RegistrationRequest} que contiene los datos enviados desde el formulario.
     * @param redirectAttributes Utilizado para anadir atributos flash para redirecciones.
     * @return Una cadena de redireccion.
     */
    @PostMapping("/register") // Nombre de ruta actualizado
    public String registerUser(@ModelAttribute("user") RegistrationRequest registrationDTO, RedirectAttributes redirectAttributes) { // Nombres de parametros y variables actualizados
        try {
            userService.save(registrationDTO); // Nombre de metodo actualizado
            // Anade un atributo flash para mostrar un mensaje de exito despues de la redireccion
            redirectAttributes.addFlashAttribute("registrationSuccess", "¡Te has registrado exitosamente! Ahora puedes iniciar sesión."); // Nombre de atributo actualizado
            return "redirect:/login"; // Redirige a la pagina de login
        } catch (RuntimeException e) {
            // Anade un atributo flash para mostrar un mensaje de error y el DTO para repoblar el formulario
            redirectAttributes.addFlashAttribute("registrationError", e.getMessage()); // Nombre de atributo actualizado
            redirectAttributes.addFlashAttribute("user", registrationDTO); // Mantiene los datos del formulario - Nombre de atributo actualizado
            return "redirect:/register"; // Redirige de vuelta al formulario de registro - Nombre de ruta actualizado
        }
    }
}