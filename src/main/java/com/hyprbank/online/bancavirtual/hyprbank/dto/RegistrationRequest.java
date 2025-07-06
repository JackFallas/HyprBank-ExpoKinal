package com.hyprbank.online.bancavirtual.hyprbank.dto;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para el registro de un nuevo usuario.
 *
 * Utilizado para encapsular y transferir los datos de registro de un usuario
 * desde el cliente (frontend) hacia la capa de servicio/persistencia del backend.
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class RegistrationRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password; // Este campo se usará para devolver la contraseña generada
    private String dpi;
    private String nit;
    private String phoneNumber;
    private String address; // Añadido el campo address
    private String birthDate; // Asegurarse de que sea String para coincidir con el HTML y User.java
}