package com.hyprbank.online.bancavirtual.controller;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.dto.MovimientoDTO;

// Importaciones de Entidades y Enums
import com.hyprbank.online.bancavirtual.model.Usuario;
import com.hyprbank.online.bancavirtual.model.Movimiento.TipoMovimiento; // Importa el enum anidado

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.repository.UsuarioRepository;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.service.MovimientoService;

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
 * @RequestMapping("/api/movimientos") define la ruta base para todos los endpoints de este controlador.
 */
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController { // Renombrado a MovimientoController

    private final MovimientoService movimientoService;
    private final UsuarioRepository usuarioRepository;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de MovimientoService y UsuarioRepository.
     */
    @Autowired
    public MovimientoController(MovimientoService movimientoService, UsuarioRepository usuarioRepository) {
        this.movimientoService = movimientoService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Endpoint para obtener el historial de movimientos del usuario autenticado.
     * Permite filtrar los movimientos por un rango de fechas y/o por tipo de movimiento.
     *
     * @param userDetails Objeto UserDetails inyectado por Spring Security, que representa al usuario autenticado.
     * @param fechaInicio Fecha de inicio para el filtro del historial (opcional, formato ISO_DATE 'YYYY-MM-DD').
     * @param fechaFin Fecha de fin para el filtro del historial (opcional, formato ISO_DATE 'YYYY-MM-DD').
     * @param tipo Tipo de movimiento (INGRESO o EGRESO, opcional).
     * @return ResponseEntity con una lista de MovimientoDTOs que cumplen con los criterios de filtro.
     * Retorna HTTP 200 OK si la solicitud es exitosa.
     * @throws RuntimeException Si el usuario autenticado no es encontrado en la base de datos (lo cual es un estado inesperado).
     */
    @GetMapping("/historial")
    public ResponseEntity<List<MovimientoDTO>> getHistorialMovimientos(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) TipoMovimiento tipo
    ) {
        // El email del usuario autenticado se obtiene del objeto UserDetails proporcionado por Spring Security.
        String userEmail = userDetails.getUsername();

        // Buscar el ID del usuario en la base de datos usando su email.
        // Se lanza una RuntimeException si el usuario no es encontrado, indicando un problema de consistencia de datos.
        Long usuarioId = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el email: " + userEmail)).getId();

        // Delegar la logica de obtencion de movimientos al servicio.
        List<MovimientoDTO> movimientos = movimientoService.getMovimientosByUsuarioId(usuarioId, fechaInicio, fechaFin, tipo);

        // Devolver la lista de movimientos con un estado HTTP 200 OK.
        return new ResponseEntity<>(movimientos, HttpStatus.OK);
    }
}