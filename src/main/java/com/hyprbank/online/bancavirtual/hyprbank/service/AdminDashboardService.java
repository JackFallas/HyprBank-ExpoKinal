package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminDashboardStatsDTO;

/**
 * Interfaz de Servicio para la gestión de estadísticas del Dashboard del Administrador.
 *
 * Define las operaciones de negocio relacionadas con la obtención de métricas
 * agregadas sobre los usuarios del sistema para la vista de administrador.
 */
public interface AdminDashboardService {

    /**
     * Obtiene las estadísticas consolidadas para el dashboard del administrador.
     * Esto incluye el total de usuarios, usuarios activos, inactivos y bloqueados.
     *
     * @return Un objeto {@link AdminDashboardStatsDTO} con las estadísticas.
     */
    AdminDashboardStatsDTO getAdminDashboardStats();
}
