package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementDTO;

// Importaciones de Entidades y Enums
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
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
import java.util.Map; // Importar Map para respuestas de error

// Importaciones de Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Controlador REST para la gestion de movimientos bancarios.
 *
 * Proporciona endpoints para que los usuarios autenticados puedan consultar su historial de movimientos,
 * con opciones de filtrado por rango de fechas, tipo de movimiento y un límite de resultados.
 *
 * La anotacion @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los metodos se serializaran directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/movements") define la ruta base para todos los endpoints de este controlador.
 * @CrossOrigin permite solicitudes de origen cruzado desde el frontend especificado.
 */
@RestController
@RequestMapping("/api/movements")
@CrossOrigin(origins = "http://localhost:8081") // Asegúrate de que este origen coincida con tu frontend
public class MovementController {

    private static final Logger logger = LoggerFactory.getLogger(MovementController.class); // Inicializar logger

    private final MovementService movementService;
    private final UserRepository userRepository;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de MovementService y UserRepository.
     */
    @Autowired
    public MovementController(MovementService movementService, UserRepository userRepository) {
        this.movementService = movementService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint para obtener el historial de movimientos del usuario autenticado.
     * Permite filtrar los movimientos por un rango de fechas y/o por tipo de movimiento,
     * y limitar el número de resultados.
     *
     * @param userDetails Objeto UserDetails inyectado por Spring Security, que representa al usuario autenticado.
     * @param startDate Fecha de inicio para el filtro del historial (opcional, formato ISO_DATE 'YYYY-MM-DD').
     * @param endDate Fecha de fin para el filtro del historial (opcional, formato ISO_DATE 'YYYY-MM-DD').
     * @param type Tipo de movimiento (INCOME o EXPENSE, o null/vacío para todos) (opcional).
     * @param limit Número máximo de movimientos a devolver (opcional).
     * @return ResponseEntity con una lista de MovementDTOs que cumplen con los criterios de filtro,
     * o un mensaje de error si ocurre una excepción.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getMovementHistory( // Cambiado a ResponseEntity<?> para permitir Map en caso de error
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type, // Cambiado a String para manejar "todos" y luego convertir en el servicio
            @RequestParam(required = false) Integer limit // <-- AGREGADO: Parámetro para limitar resultados
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
