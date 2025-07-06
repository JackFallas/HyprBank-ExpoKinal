package com.hyprbank.online.bancavirtual.hyprbank.controller;

import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminMovementDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.AccountDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminDashboardStatsDTO; // Importar el nuevo DTO
import com.hyprbank.online.bancavirtual.hyprbank.service.AdminMovementService;
import com.hyprbank.online.bancavirtual.hyprbank.service.AccountService;
import com.hyprbank.online.bancavirtual.hyprbank.service.AdminDashboardService; // Importar el nuevo servicio
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/admin") // Prefijo para todas las APIs de admin
public class AdminRestController {

    private final AdminMovementService adminMovementService;
    private final AccountService accountService;
    private final AdminDashboardService adminDashboardService; // Inyectar el nuevo servicio

    public AdminRestController(AdminMovementService adminMovementService, AccountService accountService, AdminDashboardService adminDashboardService) {
        this.adminMovementService = adminMovementService;
        this.accountService = accountService;
        this.adminDashboardService = adminDashboardService; // Inicializar el nuevo servicio
    }

    @GetMapping("/movements/all")
    @PreAuthorize("hasRole('ADMIN')") // Asegura que solo los ADMIN puedan acceder
    public ResponseEntity<List<AdminMovementDTO>> getAllMovementsForAdmin() {
        List<AdminMovementDTO> movements = adminMovementService.getAllBankMovements();
        return ResponseEntity.ok(movements);
    }

    // Endpoint para buscar una cuenta por número (utilizado en la sección de depósito del admin)
    @GetMapping("/accounts/number/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')") // Asegura que solo los ADMIN puedan acceder
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) {
        AccountDTO account = accountService.findByAccountNumber(accountNumber);
        if (account != null) {
            return ResponseEntity.ok(account);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Nuevo endpoint para obtener las estadísticas del dashboard del administrador.
     * Requiere el rol 'ADMIN'.
     *
     * @return ResponseEntity con un {@link AdminDashboardStatsDTO} que contiene las estadísticas.
     */
    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardStatsDTO> getAdminDashboardStats() {
        AdminDashboardStatsDTO stats = adminDashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }
}