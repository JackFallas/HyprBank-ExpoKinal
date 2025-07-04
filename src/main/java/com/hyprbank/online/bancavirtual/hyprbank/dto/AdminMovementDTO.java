package com.hyprbank.online.bancavirtual.hyprbank.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate; // O LocalDateTime, dependiendo de cómo guardes la fecha en tu entidad Movement

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMovementDTO {
    private Long id;
    private LocalDate date; // Formato YYYY-MM-DD para compatibilidad con HTML input type="date"
    private String description;
    private String type; // Ej. "INCOME", "EXPENSE"
    private BigDecimal amount;
    private String accountNumber; // Número de cuenta involucrada
    private String userName; // Nombre completo del usuario asociado a la cuenta
}