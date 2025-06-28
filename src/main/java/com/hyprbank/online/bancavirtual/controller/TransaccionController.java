package com.hyprbank.online.bancavirtual.controller;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.model.Movimiento;
import com.hyprbank.online.bancavirtual.model.Usuario;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.repository.UsuarioRepository; // Ajuste de nombre de Repositorio

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.service.TransaccionService;

// Importaciones de DTOs (corregidos según los nuevos nombres)
import com.hyprbank.online.bancavirtual.dto.MovimientoDTO;
import com.hyprbank.online.bancavirtual.dto.MovimientoRequest; // Usar el DTO renombrado
import com.hyprbank.online.bancavirtual.dto.TransferenciaRequest; // Usar el DTO renombrado
import com.hyprbank.online.bancavirtual.dto.TransferenciaExternaRequest; // Usar el DTO renombrado
import com.hyprbank.online.bancavirtual.dto.TransferenciaExternaResponse; // Usar el DTO renombrado

// Importaciones de Spring Framework y validacion
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult; // Para manejar resultados de validacion
import org.springframework.web.bind.annotation.CrossOrigin; // Para CORS
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid; // Para activacion de validacion

// Importaciones de Java Utilities
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Importaciones de Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Controlador REST para la gestion de transacciones bancarias.
 *
 * Proporciona endpoints para que los usuarios autenticados puedan realizar
 * depositos, retiros, transferencias internas y transferencias externas.
 *
 * La anotacion @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los metodos se serializaran directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/transacciones") define la ruta base para todos los endpoints de este controlador.
 * @CrossOrigin permite solicitudes de origen cruzado desde el frontend especificado.
 */
@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "http://localhost:8081") // Ajusta este origen si tu frontend no esta en localhost:8081
public class TransaccionController { // Renombrado a TransaccionController

    private static final Logger logger = LoggerFactory.getLogger(TransaccionController.class);

    private final TransaccionService transaccionService;
    private final UsuarioRepository usuarioRepository; // Ajuste de nombre de Repositorio

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de TransaccionService y UsuarioRepository.
     */
    @Autowired
    public TransaccionController(TransaccionService transaccionService, UsuarioRepository usuarioRepository) {
        this.transaccionService = transaccionService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Metodo auxiliar para transformar una entidad {@link Movimiento} a su correspondiente {@link MovimientoDTO}.
     * Esto es fundamental para evitar la exposicion directa de las entidades de persistencia en la API
     * y prevenir problemas de serializacion (ej., referencias circulares).
     *
     * @param movimiento La entidad Movimiento a ser convertida.
     * @return Un {@link MovimientoDTO} con los datos esenciales del movimiento, o {@code null} si la entidad de entrada es nula.
     */
    private MovimientoDTO mapMovimientoToDTO(Movimiento movimiento) {
        if (movimiento == null) {
            return null;
        }
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getId());
        if (movimiento.getCuenta() != null) {
            dto.setCuentaId(movimiento.getCuenta().getId());
            dto.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        } else {
            // Manejo si la cuenta es nula (no deberia ocurrir con movimientos persistidos validos).
            dto.setCuentaId(null);
            dto.setNumeroCuenta("Cuenta Desconocida");
        }
        dto.setFecha(movimiento.getFecha());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setTipo(movimiento.getTipo());
        dto.setMonto(movimiento.getMonto());
        return dto;
    }

    /**
     * Endpoint para realizar un deposito en una cuenta especifica del usuario autenticado.
     *
     * @param request DTO {@link MovimientoRequest} con los detalles del deposito (numero de cuenta, monto, descripcion).
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un mensaje de exito y el {@link MovimientoDTO} del deposito,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/deposito")
    public ResponseEntity<?> depositar(
            @Valid @RequestBody MovimientoRequest request, // Usando el DTO renombrado
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("message", "Errores de validacion en la solicitud de deposito", "errors", errors));
        }

        try {
            String userEmail = userDetails.getUsername();
            // Buscar el ID del usuario en la base de datos usando su email.
            Usuario usuario = usuarioRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long usuarioId = usuario.getId(); // Obtener el ID del usuario

            // El servicio devuelve la entidad Movimiento, que luego se mapea a DTO.
            Movimiento movimientoRealizado = transaccionService.realizarDeposito(request, usuarioId);

            MovimientoDTO movimientoDTO = mapMovimientoToDTO(movimientoRealizado);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Deposito realizado con exito.", "movimiento", movimientoDTO));
        } catch (IllegalArgumentException e) {
            logger.error("Error al realizar deposito: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error interno del servidor al procesar el deposito: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno del servidor al procesar el deposito: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para realizar un retiro de una cuenta especifica del usuario autenticado.
     *
     * @param request DTO {@link MovimientoRequest} con los detalles del retiro (numero de cuenta, monto, descripcion).
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un mensaje de exito y el {@link MovimientoDTO} del retiro,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/retiro")
    public ResponseEntity<?> retirar(
            @Valid @RequestBody MovimientoRequest request, // Usando el DTO renombrado
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("message", "Errores de validacion en la solicitud de retiro", "errors", errors));
        }

        try {
            String userEmail = userDetails.getUsername();
            Usuario usuario = usuarioRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long usuarioId = usuario.getId();

            Movimiento movimientoRealizado = transaccionService.realizarRetiro(request, usuarioId);

            MovimientoDTO movimientoDTO = mapMovimientoToDTO(movimientoRealizado);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Retiro realizado con exito.", "movimiento", movimientoDTO));
        } catch (IllegalArgumentException e) {
            logger.error("Error al realizar retiro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error interno del servidor al procesar el retiro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno del servidor al procesar el retiro: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para realizar una transferencia de dinero entre dos cuentas del MISMO usuario autenticado (transferencia interna).
     *
     * @param request DTO {@link TransferenciaRequest} con los detalles de la transferencia (numeros de cuenta origen y destino, monto, descripcion).
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un mensaje de exito y una lista de los {@link MovimientoDTO}s generados,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/transferencia")
    public ResponseEntity<?> transferir(
            @Valid @RequestBody TransferenciaRequest request, // Usando el DTO renombrado
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("message", "Errores de validacion en la solicitud de transferencia interna", "errors", errors));
        }

        try {
            String userEmail = userDetails.getUsername();
            Usuario usuario = usuarioRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long usuarioId = usuario.getId();

            List<Movimiento> movimientosGenerados = transaccionService.realizarTransferencia(request, usuarioId);

            List<MovimientoDTO> movimientosDTO = movimientosGenerados.stream()
                    .map(this::mapMovimientoToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Transferencia interna realizada con exito.", "movimientos", movimientosDTO));
        } catch (IllegalArgumentException e) {
            logger.error("Error al realizar transferencia interna: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error interno del servidor al procesar la transferencia interna: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno del servidor al procesar la transferencia interna: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para realizar una transferencia a una cuenta externa (a un tercero) dentro del mismo sistema bancario.
     *
     * @param request DTO {@link TransferenciaExternaRequest} con los detalles de la transferencia externa.
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un {@link TransferenciaExternaResponse} detallado del resultado de la operacion,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/transferenciaExterna")
    public ResponseEntity<?> realizarTransferenciaExterna(
            @Valid @RequestBody TransferenciaExternaRequest request, // Usando el DTO renombrado
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(Map.of("message", "Errores de validacion en la solicitud de transferencia externa", "errors", errors));
        }

        logger.info("Solicitud de Transferencia Externa Recibida: {}", request);

        try {
            String userEmail = userDetails.getUsername();
            Usuario usuario = usuarioRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long usuarioId = usuario.getId();

            // El servicio ahora devuelve directamente el DTO de respuesta.
            TransferenciaExternaResponse responseDTO = transaccionService.procesarTransferenciaExterna(request, usuarioId);

            return ResponseEntity.ok(responseDTO);

        } catch (IllegalArgumentException e) {
            logger.error("Error en realizarTransferenciaExterna: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error interno del servidor al procesar la transferencia externa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno del servidor al procesar la transferencia externa: " + e.getMessage()));
        }
    }
}
