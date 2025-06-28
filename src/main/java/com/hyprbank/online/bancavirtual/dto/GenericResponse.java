package com.hyprbank.online.bancavirtual.dto;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/* 
 * DTO (Data Transfer Object) para respuestas genericas.
 *
 * Utilizado para comunicar el resultado de una operacion (exito/error)
 * desde el backend al cliente, especialmente cuando no se necesita devolver
 * un objeto complejo o una entidad especifica.
 *
 * Contiene campos relevantes para la vista y la logica.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class GenericResponse {
    private boolean success; // Indica si la operacion fue exitosa (true) o no (false)
    private String message;  // Mensaje descriptivo del resultado de la operacion
}