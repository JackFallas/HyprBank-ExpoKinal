package com.hyprbank.online.bancavirtual.hyprbank.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO (Data Transfer Object) para las estadísticas del Dashboard del Administrador.
 *
 * Utilizado para consolidar y transferir la información esencial que se muestra
 * en el panel principal de un administrador, como el conteo de usuarios por estado.
 *
 * Utiliza Lombok para generar automáticamente getters, setters, toString(), equals() y hashCode().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long blockedUsers; // Asumiendo que 'bloqueado' es un estado distinto
}
