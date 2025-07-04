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
 * Controlador de Spring MVC para la gestion del registro de usuarios.
 *
 * Esta clase maneja las solicitudes relacionadas con la visualizacion del formulario
 * de registro y el procesamiento de las operaciones de registro.
 * La logica de login se movera a un controlador separado.
 *
 * @Controller indica que esta clase es un componente de controlador de Spring MVC.
 * @RequestMapping("/register") indica que todos los metodos dentro de esta clase
 * manejaran rutas que comiencen con "/register".
 */
@Controller
@RequestMapping("/register") // Prefijo base para todas las rutas de este controlador
public class AuthController { // Se podria renombrar a 'RegistrationController' para mayor claridad

    @Autowired
    private UserService userService;

    /**
     * Prepara un nuevo objeto {@link RegistrationRequest} para el formulario de registro.
     * Este objeto se vinculara al atributo "user" en la vista.
     *
     * @return Una nueva instancia de {@link RegistrationRequest}.
     */
    @ModelAttribute("user")
    public RegistrationRequest returnNewUserRegistrationDTO() {
        return new RegistrationRequest();
    }

    /**
     * Maneja las solicitudes GET a la ruta "/register" (que ahora es solo "/").
     * Muestra el formulario de registro de usuario.
     *
     * @param model El objeto Model de Spring MVC para pasar datos a la vista.
     * @return El nombre de la vista "register" (register.html).
     */
    @GetMapping // Mapea a /register (debido al RequestMapping de la clase)
    public String showRegistrationForm(Model model) {
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
    @PostMapping // Mapea a /register (debido al RequestMapping de la clase)
    public String registerUser(@ModelAttribute("user") RegistrationRequest registrationDTO, RedirectAttributes redirectAttributes) {
        try {
            userService.save(registrationDTO);
            // Anade un atributo flash para mostrar un mensaje de exito despues de la redireccion
            redirectAttributes.addFlashAttribute("registrationSuccess", "¡Te has registrado exitosamente! Ahora puedes iniciar sesión.");
            return "redirect:/login"; // Redirige a la pagina de login (asumiendo que ahora tendras un LoginController)
        } catch (RuntimeException e) { // Mantuvimos RuntimeException para compatibilidad con tu UserService actual
            // Anade un atributo flash para mostrar un mensaje de error y el DTO para repoblar el formulario
            redirectAttributes.addFlashAttribute("registrationError", e.getMessage());
            redirectAttributes.addFlashAttribute("user", registrationDTO); // Mantiene los datos del formulario
            return "redirect:/register"; // Redirige de vuelta al formulario de registro
        }
    }
}