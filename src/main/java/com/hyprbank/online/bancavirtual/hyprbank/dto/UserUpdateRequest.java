package com.hyprbank.online.bancavirtual.hyprbank.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la actualización de un usuario existente.
 *
 * Utilizado para encapsular y transferir los datos de un usuario
 * desde el cliente (frontend) hacia la capa de servicio/persistencia del backend
 * cuando se realiza una operación de edición por parte de un administrador.
 *
 * Incluye los campos que pueden ser modificados desde el formulario de edición.
 */
@Data // Anotación Lombok para generar getters, setters, toString(), etc.
@NoArgsConstructor // Anotación Lombok para generar un constructor sin argumentos
@AllArgsConstructor // Anotación Lombok para generar un constructor con todos los argumentos
public class UserUpdateRequest {
    private Long id; // El ID del usuario que se va a actualizar
    private String firstName;
    private String lastName;
    private String email;
    private String dpi;
    private String nit;
    private String phoneNumber;
    private String estado; // Campo para el estado (Activo, Inactivo, Bloqueado) como String
    // No incluimos la contraseña aquí por seguridad; la actualización de contraseña
    // debería ser un proceso separado.
}