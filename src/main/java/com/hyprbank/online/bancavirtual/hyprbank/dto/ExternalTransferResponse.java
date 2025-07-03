package com.hyprbank.online.bancavirtual.hyprbank.dto;

import java.math.BigDecimal;
import java.util.List;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la respuesta de una transferencia externa.
 *
 * Utilizado para comunicar el resultado de una operacion de transferencia externa
 * desde el backend al cliente, incluyendo detalles del estado y los saldos actualizados.
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class ExternalTransferResponse {
    private String message; // Nombre de campo actualizado
    private BigDecimal newOriginAccountBalance; // Nombre de campo actualizado
    private MovementDTO lastOriginMovement; // Nombre de campo y DTO actualizado
    private List<MovementDTO> recentMovements; // Nombre de campo y DTO actualizado
}
