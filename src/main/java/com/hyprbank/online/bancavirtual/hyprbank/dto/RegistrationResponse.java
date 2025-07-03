package com.hyprbank.online.bancavirtual.hyprbank.dto;

// Importaciones de Lombok
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la respuesta de una operacion de registro de usuario/cliente.
 *
 * Utilizado para devolver informacion relevante al frontend despues de un registro exitoso,
 * como el email del usuario y la contraseña generada (en texto plano antes de encriptacion).
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters,
 * constructores sin argumentos y con todos los argumentos.
 */
@Data // Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Genera un constructor sin argumentos
@AllArgsConstructor // Genera un constructor con todos los argumentos
public class RegistrationResponse {
    private String email;
    private String generatedPassword; // Contraseña generada en texto plano
}