package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationResponseDTO; // Importar el nuevo DTO
// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;
import com.hyprbank.online.bancavirtual.hyprbank.model.User; // Importar User para el tipo de retorno

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Para ResponseEntity
import org.springframework.http.ResponseEntity; // Para devolver respuestas REST
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ModelAttribute; // ELIMINADO: Ya no se usa
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody; // Para recibir JSON
// import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ELIMINADO: Ya no se usa

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
public class AuthController { // Se podria renombrar a 'RegistrationController' para mayor cl...

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Muestra la página de registro de usuarios.
     *
     * @param model El modelo de Spring para pasar datos a la vista.
     * @return El nombre de la vista "register" (que corresponde a register.html).
     */
    @GetMapping
    public String showRegistrationForm(Model model) {
        // Asegúrate de que el objeto "user" esté en el modelo para el formulario Thymeleaf
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new RegistrationRequest());
        }
        return "register"; // Esto buscará src/main/resources/templates/register.html
    }

    /**
     * Procesa la solicitud de registro de un nuevo usuario.
     *
     * @param registrationDTO El {@link RegistrationRequest} que contiene los datos enviados desde el formulario.
     * @return ResponseEntity con el email y la contraseña generada si el registro es exitoso,
     * o un mensaje de error si falla.
     */
    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationDTO) {
        try {
            // El método save de userService ahora devuelve el User guardado.
            // La contraseña generada se establece en el DTO de solicitud por convención.
            User savedUser = userService.save(registrationDTO);

            // Crear el DTO de respuesta con el email y la contraseña generada
            RegistrationResponseDTO responseDTO = new RegistrationResponseDTO(
                savedUser.getEmail(),
                registrationDTO.getPassword() // Usar la contraseña que se estableció en el DTO de solicitud
            );

            return ResponseEntity.ok(responseDTO); // Devolver 200 OK con el DTO de respuesta
        } catch (RuntimeException e) {
            // Devolver un error 400 Bad Request con el mensaje de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}