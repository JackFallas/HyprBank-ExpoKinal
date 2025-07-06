package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementDTO;

// Importaciones de Entidades y Enums
// import com.hyprbank.online.bancavirtual.hyprbank.model.User; // ELIMINADO: No se usa directamente aquí
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement.MovementType; // Importa el enum anidado

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.MovementService;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin; // Importar CrossOrigin para CORS

// Importaciones de Java Utilities
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Controlador REST para la gestión de movimientos bancarios.
 *
 * Proporciona un endpoint para que los usuarios autenticados puedan consultar
 * su historial de movimientos, aplicando filtros por rango de fechas y tipo de movimiento.
 *
 * La anotación @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los métodos se serializarán directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/movements") define la ruta base para todos los endpoints de este controlador.
 * @CrossOrigin permite solicitudes de origen cruzado, útil para desarrollo frontend separado.
 */
@RestController
@RequestMapping("/api/movements")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:8080", "http://localhost:8081"})
public class MovementController {

    private static final Logger logger = LoggerFactory.getLogger(MovementController.class);

    private final MovementService movementService;
    private final UserRepository userRepository; // Necesario para obtener el ID del usuario

    @Autowired
    public MovementController(MovementService movementService, UserRepository userRepository) {
        this.movementService = movementService;
        this.userRepository = userRepository;
    }

    /**
     * Obtiene el historial de movimientos de una cuenta para el usuario autenticado.
     * Permite filtrar por rango de fechas, tipo de movimiento y limitar la cantidad de resultados.
     *
     * @param userDetails Objeto UserDetails proporcionado por Spring Security, que contiene los detalles del usuario autenticado.
     * @param startDate   Fecha de inicio para filtrar movimientos (opcional).
     * @param endDate     Fecha de fin para filtrar movimientos (opcional).
     * @param type        Tipo de movimiento a filtrar (INCOME, EXPENSE) (opcional).
     * @param limit       Número máximo de movimientos a devolver (opcional).
     * @return ResponseEntity con una lista de {@link MovementDTO} que representan el historial de movimientos.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getMovementHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) Integer limit // AGREGADO: Parámetro para limitar resultados
    ) {
        try {
            // El email del usuario autenticado se obtiene del objeto UserDetails proporcionado por Spring Security.
            String userEmail = userDetails.getUsername();

            // Buscar el ID del usuario en la base de datos usando su email.
            Long userId = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail)).getId();

            // Delegar la logica de obtencion de movimientos al servicio, pasando el nuevo parámetro 'limit'.
            List<MovementDTO> movements = movementService.getMovementHistory(userId, startDate, endDate, type, limit);

            // Devolver la lista de movimientos con un estado HTTP 200 OK.
            return new ResponseEntity<>(movements, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Error al obtener historial de movimientos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error interno del servidor al obtener historial de movimientos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno del servidor al obtener historial de movimientos: " + e.getMessage()));
        }
    }
}
