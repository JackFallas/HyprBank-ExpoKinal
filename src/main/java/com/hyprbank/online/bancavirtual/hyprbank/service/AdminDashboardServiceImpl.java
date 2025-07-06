package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminDashboardStatsDTO;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para la gestión de estadísticas del Dashboard del Administrador.
 *
 * Proporciona la lógica de negocio para calcular y obtener métricas
 * agregadas sobre los usuarios del sistema.
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;

    @Autowired
    public AdminDashboardServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtiene las estadísticas consolidadas para el dashboard del administrador.
     *
     * Consulta el repositorio de usuarios para obtener el conteo de usuarios
     * por su estado (activo, inactivo, bloqueado) y el total de usuarios.
     *
     * @return Un objeto {@link AdminDashboardStatsDTO} con las estadísticas calculadas.
     */
    @Override
    public AdminDashboardStatsDTO getAdminDashboardStats() {
        long totalUsers = userRepository.count(); // Obtener el total de usuarios

        // Contar usuarios por estado usando métodos personalizados si existen,
        // o filtrando en memoria si la base de datos es pequeña y no hay métodos específicos.
        // Asumiendo que el campo 'status' en la entidad User es un String: "ACTIVO", "INACTIVO", "BLOQUEADO"
        long activeUsers = userRepository.countByStatus("ACTIVO");
        long inactiveUsers = userRepository.countByStatus("INACTIVO");
        long blockedUsers = userRepository.countByStatus("BLOQUEADO");

        // Si no existen los métodos countByStatus, se pueden añadir a UserRepository:
        // long countByStatus(String status);

        return new AdminDashboardStatsDTO(totalUsers, activeUsers, inactiveUsers, blockedUsers);
    }
}
