package com.hyprbank.online.bancavirtual.hyprbank.controller;

import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminMovementDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.AccountDTO; // Si necesitas un DTO para Account
import com.hyprbank.online.bancavirtual.hyprbank.service.AdminMovementService;
import com.hyprbank.online.bancavirtual.hyprbank.service.AccountService; // Si tienes un servicio para buscar cuentas por número
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize; // Para seguridad a nivel de método

import java.util.List;

@RestController
@RequestMapping("/api/admin") // Prefijo para todas las APIs de admin
public class AdminRestController {

    private final AdminMovementService adminMovementService;
    private final AccountService accountService; // Asume que tienes un AccountService para buscar cuentas

    public AdminRestController(AdminMovementService adminMovementService, AccountService accountService) {
        this.adminMovementService = adminMovementService;
        this.accountService = accountService;
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
        // Asume que tu AccountService tiene un método para buscar por número de cuenta
        // y que devuelve un AccountDTO o una entidad Account que puedes mapear a DTO.
        AccountDTO account = accountService.findByAccountNumber(accountNumber);
        if (account != null) {
            return ResponseEntity.ok(account);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Puedes añadir más endpoints REST para el admin aquí (ej. para estadísticas del dashboard)
    // @GetMapping("/dashboard-stats")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<AdminDashboardStatsDTO> getAdminDashboardStats() {
    //     // Lógica para obtener y devolver las estadísticas
    // }
}