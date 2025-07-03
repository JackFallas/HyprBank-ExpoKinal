package com.hyprbank.online.bancavirtual.hyprbank.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.dto.UserUpdateRequest; // Asegúrate de que este DTO exista

@RestController // ¡IMPORTANTE! Este es un RestController para APIs
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint para actualizar un usuario existente.
     * Recibe el ID del usuario en la URL y los datos actualizados en el cuerpo de la solicitud.
     *
     * @param id El ID del usuario a actualizar.
     * @param updateRequest DTO con los detalles del usuario con la información actualizada.
     * @return ResponseEntity con el usuario actualizado o un mensaje de error.
     */
    @PutMapping("/api/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest updateRequest) {
        try {
            // Asegurarse de que el ID del path coincida con el ID del objeto DTO
            if (!id.equals(updateRequest.getId())) {
                return new ResponseEntity<>("ID del usuario en la URL no coincide con el ID en el cuerpo de la solicitud.", HttpStatus.BAD_REQUEST);
            }
            User updatedUser = userService.updateUser(updateRequest); // Pasa el DTO al servicio
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // O HttpStatus.BAD_REQUEST
        }
    }

    /**
     * Endpoint para eliminar un usuario por su ID.
     *
     * @param id El ID del usuario a eliminar.
     * @return ResponseEntity con un mensaje de éxito o error.
     */
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>("Usuario eliminado exitosamente.", HttpStatus.NO_CONTENT); // 204 No Content
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}