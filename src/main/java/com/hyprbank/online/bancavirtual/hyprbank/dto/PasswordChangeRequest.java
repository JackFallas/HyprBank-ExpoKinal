package com.hyprbank.online.bancavirtual.hyprbank.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * DTO (Data Transfer Object) para la solicitud de cambio de contraseña.
 *
 * Utilizado para recibir la nueva contraseña del usuario desde el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String newPassword;
}
