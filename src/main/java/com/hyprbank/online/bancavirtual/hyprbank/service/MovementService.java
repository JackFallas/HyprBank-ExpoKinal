package com.hyprbank.online.bancavirtual.hyprbank.service;

// Importaciones de Entidades y Enums
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement.MovementType; // Importar el enum
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.MovementRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementDTO;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest; // Importar PageRequest
import org.springframework.data.domain.Pageable; // Importar Pageable

// Importaciones de Java Utilities
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator; // Para ordenar la lista final
import java.util.Collections; // Para Collections.emptyList()

/*
 * Clase de Servicio para la gestion de movimientos bancarios.
 *
 * Esta clase encapsula la logica de negocio relacionada con la consulta y manipulacion de movimientos.
 * Interactua con los repositorios de Movement, Account y User para realizar operaciones complejas.
 *
 * La anotacion @Service indica que esta clase es un componente de servicio de Spring.
 */
@Service
public class MovementService { // No es una interfaz, es la implementación directa

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de MovementRepository, AccountRepository y UserRepository.
     */
    @Autowired
    public MovementService(MovementRepository movementRepository, AccountRepository accountRepository, UserRepository userRepository) {
        this.movementRepository = movementRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Obtiene una lista de movimientos para un usuario especifico, con opciones de filtrado y límite.
     * Los movimientos pueden ser filtrados por un rango de fechas y/o por tipo de movimiento.
     * Si no se proporcionan filtros, devuelve todos los movimientos del usuario.
     *
     * @param userId El ID del usuario cuyos movimientos se desean obtener.
     * @param startDate La fecha de inicio del rango para filtrar movimientos (opcional).
     * @param endDate La fecha de fin del rango para filtrar movimientos (opcional).
     * @param type El tipo de movimiento (INCOME o EXPENSE) para filtrar (opcional, se espera el enum MovementType).
     * @param limit El número máximo de movimientos a devolver (opcional).
     * @return Una lista de {@link MovementDTO} que representan los movimientos encontrados.
     * @throws IllegalArgumentException Si el usuario no es encontrado.
     */
    public List<MovementDTO> getMovementHistory(Long userId, LocalDate startDate, LocalDate endDate, MovementType type, Integer limit) {
        // Buscar el usuario por ID para asegurar que existe.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        // Obtener todas las cuentas asociadas a este usuario.
        List<Account> userAccounts = accountRepository.findByUser(user);

        if (userAccounts.isEmpty()) {
            return Collections.emptyList(); // Si no hay cuentas, no hay movimientos
        }

        // Para simplificar, asumiremos que solo hay una cuenta por usuario o que queremos movimientos de todas.
        // Si solo hay una cuenta, obtenemos su ID. Si hay varias, tendríamos que iterar o decidir.
        // Por ahora, tomaremos la primera cuenta.
        Long accountId = userAccounts.get(0).getId(); // O ajustar si un usuario puede tener varias cuentas y se necesita filtrar por una específica

        // Define Pageable si se proporciona un límite. Si no hay límite, usa Pageable.unpaged()
        // para obtener todos los resultados sin paginación.
        Pageable pageable = (limit != null && limit > 0) ? PageRequest.of(0, limit) : Pageable.unpaged();

        List<Movement> movements;

        if (startDate != null && endDate != null && type != null) {
            // Filtrar por rango de fechas y tipo
            movements = movementRepository.findByAccountIdAndTypeAndDateBetweenOrderByDateDesc(accountId, type, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            // Filtrar solo por rango de fechas
            movements = movementRepository.findByAccountIdAndDateBetweenOrderByDateDesc(accountId, startDate, endDate, pageable);
        } else if (type != null) {
            // Filtrar solo por tipo de movimiento
            movements = movementRepository.findByAccountIdAndTypeOrderByDateDesc(accountId, type, pageable);
        } else {
            // No hay filtros de fecha ni tipo, obtener todos los movimientos de la cuenta con el límite
            movements = movementRepository.findByAccountIdOrderByDateDesc(accountId, pageable);
        }

        // Convertir las entidades Movement a objetos MovementDTO para la capa de presentacion.
        return movements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Metodo auxiliar para convertir una entidad Movement a un MovementDTO.
     * Este metodo se encarga de mapear los campos relevantes de la entidad al DTO.
     *
     * @param movement La entidad Movement a convertir.
     * @return El MovementDTO resultante con los datos mapeados.
     */
    private MovementDTO convertToDto(Movement movement) {
        MovementDTO dto = new MovementDTO();
        dto.setId(movement.getId());
        dto.setAccountId(movement.getAccount().getId());
        dto.setAccountNumber(movement.getAccount().getAccountNumber());
        dto.setDate(movement.getDate());
        dto.setDescription(movement.getDescription());
        dto.setType(movement.getType());
        dto.setAmount(movement.getAmount());
        // Asegúrate de que el DTO también tenga el balance si lo necesitas en el frontend
        dto.setBalance(movement.getAccount().getBalance());
        return dto;
    }
}