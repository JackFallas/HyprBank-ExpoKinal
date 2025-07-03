package com.hyprbank.online.bancavirtual.hyprbank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la entidad Cuenta.
 *
 * Utilizado para transferir informacion de la cuenta entre las capas de la aplicacion
 * de forma segura y controlada, evita exponer la entidad del modelo
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
*/

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime creationDate;

    // Informacion del usuario asociada a la cuenta
    private Long userId;
    private String userName;
}
