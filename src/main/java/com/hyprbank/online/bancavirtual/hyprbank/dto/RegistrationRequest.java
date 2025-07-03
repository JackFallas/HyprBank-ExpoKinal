package com.hyprbank.online.bancavirtual.hyprbank.dto;

// Importaciones de Lombok
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Importaciones para validacion de Jakarta
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past; // Para validar que la fecha sea en el pasado
import jakarta.validation.constraints.Pattern; // Para expresiones regulares
import java.time.LocalDate; // Para el tipo de dato de fecha

/*
 * DTO (Data Transfer Object) para la solicitud de registro de un nuevo usuario/cliente.
 *
 * Esta clase se utiliza para encapsular los datos que se reciben del formulario
 * de registro en el frontend (administrador), antes de ser procesados por la capa de servicio.
 * Incluye validaciones basicas para asegurar la integridad de los datos.
 *
 * La contraseña sera generada automaticamente por el sistema.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters,
 * constructores sin argumentos y con todos los argumentos.
 */
@Data // Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Genera un constructor sin argumentos
@AllArgsConstructor // Genera un constructor con todos los argumentos
public class RegistrationRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres.")
    private String lastName;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email debe ser una direccion de correo valida.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    // --- Nuevos campos y validaciones ---

    @NotBlank(message = "El DPI es obligatorio.")
    @Size(min = 13, max = 13, message = "El DPI debe tener 13 digitos.")
    private String dpi;

    @NotBlank(message = "El NIT es obligatorio.")
    @Size(min = 5, max = 20, message = "El NIT debe tener entre 5 y 20 caracteres.")
    // Regex para NIT: Permite numeros, letras (mayusculas/minusculas) y el guion.
    // No impone mayusculas/minusculas/numeros especificos, solo el formato general.
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "El NIT solo puede contener numeros, letras y guiones.")
    private String nit;

    @NotBlank(message = "El numero de telefono es obligatorio.")
    // Regex para formato XXXX-XXXX (8 digitos).
    // La logica de formateo automatico (agregar el guion) se implementara en el frontend (JavaScript).
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "El numero de telefono debe tener el formato XXXX-XXXX (8 digitos).")
    private String phoneNumber;

    @NotBlank(message = "La direccion es obligatoria.")
    @Size(min = 10, max = 255, message = "La direccion debe tener entre 10 y 255 caracteres.")
    private String address;

    @Past(message = "La fecha de nacimiento debe ser en el pasado.") // Valida que la fecha sea anterior a la actual
    private LocalDate birthDate; // Tipo de dato para la fecha de nacimiento
}
