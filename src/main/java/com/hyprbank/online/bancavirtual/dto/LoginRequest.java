package com.hyprbank.online.bancavirtual.dto;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la solicitud de inicio de sesion.
 *
 * Utilizado para encapsular y transferir las credenciales (username/email y password)
 * desde el cliente (frontend) hacia la capa de autenticacion del backend.
 *
 * Contiene campos relevantes para la logica de inicio de sesion.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class LoginRequest {
    private String username; // Generalmente el email o nombre de usuario
    private String password; // Contraseña del usuario
}