package com.hyprbank.online.bancavirtual.hyprbank.dto;

import com.hyprbank.online.bancavirtual.hyprbank.model.Movement.MovementType;
import java.math.BigDecimal;
import java.time.LocalDate;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la entidad Movimiento.
 *
 * Utilizado para transferir la informacion de un movimiento bancario
 * entre las capas de la aplicacion de forma segura y controlada,
 * evitando exponer directamente la entidad del modelo.
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class MovementDTO {
    private Long id;
    private Long accountId; // ID de la cuenta asociada
    private String accountNumber; // Número de cuenta para mostrar en el frontend
    private LocalDate date; // Nombre de campo actualizado
    private String description;
    private MovementType type; // Nombre de enum actualizado
    private BigDecimal amount; // Nombre de campo actualizado
    private BigDecimal balance;
}