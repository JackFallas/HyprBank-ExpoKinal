package com.hyprbank.online.bancavirtual.hyprbank.controller;

import com.hyprbank.online.bancavirtual.hyprbank.dto.GenericResponse;
import com.hyprbank.online.bancavirtual.hyprbank.dto.PasswordChangeRequest;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
public class PasswordRestController {

    private final UserService userService;

    @Autowired
    public PasswordRestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint para que un usuario autenticado cambie su propia contraseña.
     * Requiere que el usuario esté logueado.
     *
     * @param userDetails Objeto UserDetails de Spring Security que representa al usuario autenticado.
     * @param request     DTO que contiene la nueva contraseña.
     * @return ResponseEntity con un mensaje de éxito o error.
     */
    @PostMapping("/change")
    public ResponseEntity<GenericResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PasswordChangeRequest request) {
        try {
            // Obtener el usuario de la base de datos usando el email del UserDetails
            User user = userService.findByEmail(userDetails.getUsername());

            // Llamar al servicio para cambiar la contraseña
            userService.changeUserPassword(user.getId(), request.getNewPassword());

            return ResponseEntity.ok(new GenericResponse(true, "Contraseña actualizada exitosamente."));
        } catch (IllegalArgumentException e) {
            // Error de validación de contraseña (ej. no cumple con la política)
            return ResponseEntity.badRequest().body(new GenericResponse(false, e.getMessage()));
        } catch (Exception e) {
            // Otros errores inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse(false, "Error interno al cambiar la contraseña: " + e.getMessage()));
        }
    }
}