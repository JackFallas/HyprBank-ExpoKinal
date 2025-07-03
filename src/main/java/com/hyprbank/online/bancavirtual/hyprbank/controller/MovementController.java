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

// Importaciones de Java Utilities
import java.time.LocalDate;
import java.util.List;

/*
 * Controlador REST para la gestion de movimientos bancarios.
 *
 * Proporciona endpoints para que los usuarios autenticados puedan consultar su historial de movimientos,
 * con opciones de filtrado por rango de fechas y tipo de movimiento.
 *
 * La anotacion @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los metodos se serializaran directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/movements") define la ruta base para todos los endpoints de este controlador.
 */
@RestController
@RequestMapping("/api/movements")
public class MovementController {

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
     * Permite filtrar los movimientos por un rango de fechas y/o por tipo de movimiento.
     *
     * @param userDetails Objeto UserDetails inyectado por Spring Security, que representa al usuario autenticado.
     * @param startDate Fecha de inicio para el filtro del historial (opcional, formato ISO_DATE 'YYYY-MM-DD').
     * @param endDate Fecha de fin para el filtro del historial (opcional, formato ISO_DATE 'YYYY-MM-DD').
     * @param type Tipo de movimiento (INCOME o EXPENSE, opcional).
     * @return ResponseEntity con una lista de MovementDTOs que cumplen con los criterios de filtro.
     * Retorna HTTP 200 OK si la solicitud es exitosa.
     * @throws RuntimeException Si el usuario autenticado no es encontrado en la base de datos (lo cual es un estado inesperado).
     */
    @GetMapping("/history")
    public ResponseEntity<List<MovementDTO>> getMovementHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) MovementType type
    ) {
        // El email del usuario autenticado se obtiene del objeto UserDetails proporcionado por Spring Security.
        String userEmail = userDetails.getUsername();

        // Buscar el ID del usuario en la base de datos usando su email.
        // Se lanza una RuntimeException si el usuario no es encontrado, indicando un problema de consistencia de datos.
        Long userId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el email: " + userEmail)).getId();

        // Delegar la logica de obtencion de movimientos al servicio.
        List<MovementDTO> movements = movementService.getMovementsByUserId(userId, startDate, endDate, type);

        // Devolver la lista de movimientos con un estado HTTP 200 OK.
        return new ResponseEntity<>(movements, HttpStatus.OK);
    }
}