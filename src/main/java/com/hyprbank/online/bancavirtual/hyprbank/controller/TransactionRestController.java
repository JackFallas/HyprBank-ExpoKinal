package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.TransactionService;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.TransferRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.ExternalTransferRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.ExternalTransferResponse;

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
 * @RequestMapping("/api/transactions") define la ruta base para todos los endpoints de este controlador.
 * @CrossOrigin permite solicitudes de origen cruzado desde el frontend especificado.
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:8081") // Ajusta este origen si tu frontend no esta en localhost:8081
public class TransactionRestController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionRestController.class);

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de TransactionService y UserRepository.
     */
    @Autowired
    public TransactionRestController(TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    /**
     * Metodo auxiliar para transformar una entidad {@link Movement} a su correspondiente {@link MovementDTO}.
     * Esto es fundamental para evitar la exposicion directa de las entidades de persistencia en la API
     * y prevenir problemas de serializacion (ej., referencias circulares).
     *
     * @param movement La entidad Movement a ser convertida.
     * @return Un {@link MovementDTO} con los datos esenciales del movimiento, o {@code null} si la entidad de entrada es nula.
     */
    private MovementDTO mapMovementToDTO(Movement movement) {
        if (movement == null) {
            return null;
        }
        MovementDTO dto = new MovementDTO();
        dto.setId(movement.getId());
        if (movement.getAccount() != null) {
            dto.setAccountId(movement.getAccount().getId());
            dto.setAccountNumber(movement.getAccount().getAccountNumber());
        } else {
            // Manejo si la cuenta es nula (no deberia ocurrir con movimientos persistidos validos).
            dto.setAccountId(null);
            dto.setAccountNumber("Cuenta Desconocida");
        }
        dto.setDate(movement.getDate());
        dto.setDescription(movement.getDescription());
        dto.setType(movement.getType());
        dto.setAmount(movement.getAmount());
        return dto;
    }

    /**
     * Endpoint para realizar un deposito en una cuenta especifica del usuario autenticado.
     *
     * @param request DTO {@link MovementRequest} con los detalles del deposito (numero de cuenta, monto, descripcion).
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un mensaje de exito y el {@link MovementDTO} del deposito,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @Valid @RequestBody MovementRequest request,
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
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long userId = user.getId();

            Movement performedMovement = transactionService.performDeposit(request, userId);

            MovementDTO movementDTO = mapMovementToDTO(performedMovement);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Deposito realizado con exito.", "movement", movementDTO));
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
     * @param request DTO {@link MovementRequest} con los detalles del retiro (numero de cuenta, monto, descripcion).
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un mensaje de exito y el {@link MovementDTO} del retiro,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @Valid @RequestBody MovementRequest request,
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
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long userId = user.getId();

            Movement performedMovement = transactionService.performWithdrawal(request, userId);

            MovementDTO movementDTO = mapMovementToDTO(performedMovement);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Retiro realizado con exito.", "movement", movementDTO));
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
     * @param request DTO {@link TransferRequest} con los detalles de la transferencia (numeros de cuenta origen y destino, monto, descripcion).
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un mensaje de exito y una lista de los {@link MovementDTO}s generados,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest request,
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
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long userId = user.getId();

            List<Movement> generatedMovements = transactionService.performTransfer(request, userId);

            List<MovementDTO> movementsDTO = generatedMovements.stream()
                    .map(this::mapMovementToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Transferencia interna realizada con exito.", "movements", movementsDTO));
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
     * @param request DTO {@link ExternalTransferRequest} con los detalles de la transferencia externa.
     * @param result Objeto {@link BindingResult} para capturar errores de validacion.
     * @param userDetails Objeto {@link UserDetails} inyectado por Spring Security, que representa al usuario autenticado.
     * @return ResponseEntity con un {@link ExternalTransferResponse} detallado del resultado de la operacion,
     * o un mensaje de error si la validacion falla o ocurre una excepcion.
     */
    @PostMapping("/external-transfer")
    public ResponseEntity<?> performExternalTransfer(
            @Valid @RequestBody ExternalTransferRequest request,
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
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado para el email: " + userEmail));
            Long userId = user.getId();

            ExternalTransferResponse responseDTO = transactionService.processExternalTransfer(request, userId);

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