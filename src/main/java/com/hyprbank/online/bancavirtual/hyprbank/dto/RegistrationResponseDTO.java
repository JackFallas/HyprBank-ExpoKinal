package com.hyprbank.online.bancavirtual.hyprbank.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO (Data Transfer Object) para la respuesta de una operación de registro.
 *
 * Utilizado para encapsular y transferir la información de las credenciales
 * generadas (email y contraseña) de vuelta al cliente (frontend)
 * después de un registro exitoso.
 *
 * Utiliza Lombok para generar automáticamente getters, setters, toString(), equals() y hashCode().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDTO {
    private String email;
    private String generatedPassword; // Contraseña generada para el nuevo usuario
}
