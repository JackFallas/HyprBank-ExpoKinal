package com.hyprbank.online.bancavirtual.hyprbank.service;

// Importaciones de Entidades y Enums
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement.MovementType;
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

// Importaciones de Java Utilities
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Clase de Servicio para la gestion de movimientos bancarios.
 *
 * Esta clase encapsula la logica de negocio relacionada con la consulta y manipulacion de movimientos.
 * Interactua con los repositorios de Movement, Account y User para realizar operaciones complejas.
 *
 * La anotacion @Service indica que esta clase es un componente de servicio de Spring.
 */
@Service
public class MovementService {

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
     * Obtiene una lista de movimientos para un usuario especifico, con opciones de filtrado.
     * Los movimientos pueden ser filtrados por un rango de fechas y/o por tipo de movimiento.
     * Si no se proporcionan filtros, devuelve todos los movimientos del usuario.
     *
     * @param userId El ID del usuario cuyos movimientos se desean obtener.
     * @param startDate La fecha de inicio del rango para filtrar movimientos (opcional).
     * @param endDate La fecha de fin del rango para filtrar movimientos (opcional).
     * @param type El tipo de movimiento (INCOME o EXPENSE) para filtrar (opcional).
     * @return Una lista de {@link MovementDTO} que representan los movimientos encontrados.
     * @throws IllegalArgumentException Si el usuario no es encontrado.
     */
    public List<MovementDTO> getMovementsByUserId(Long userId, LocalDate startDate, LocalDate endDate, MovementType type) {
        // Buscar el usuario por ID para asegurar que existe.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        // Obtener todas las cuentas asociadas a este usuario.
        List<Account> userAccounts = accountRepository.findByUser(user);

        // Recopilar todos los movimientos de todas las cuentas del usuario, aplicando filtros si existen.
        List<Movement> movements = userAccounts.stream()
                .flatMap(account -> {
                    if (startDate != null && endDate != null && type != null) {
                        // Filtrar por rango de fechas y tipo
                        return movementRepository.findByAccountIdAndTypeAndDateBetweenOrderByDateDesc(account.getId(), type, startDate, endDate).stream();
                    } else if (startDate != null && endDate != null) {
                        // Filtrar solo por rango de fechas
                        return movementRepository.findByAccountIdAndDateBetweenOrderByDateDesc(account.getId(), startDate, endDate).stream();
                    } else if (type != null) {
                        // Filtrar solo por tipo de movimiento
                        return movementRepository.findByAccountIdAndTypeOrderByDateDesc(account.getId(), type).stream();
                    } else {
                        // No hay filtros, obtener todos los movimientos de la cuenta
                        return movementRepository.findByAccountIdOrderByDateDesc(account.getId()).stream();
                    }
                })
                .sorted((m1, m2) -> m2.getDate().compareTo(m1.getDate())) // Ordenar todos los movimientos recopilados por fecha descendente.
                .collect(Collectors.toList());

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
        return dto;
    }

    // Mas adelante, aqui se podrian anadir metodos para crear, actualizar o eliminar movimientos,
    // pero la logica de creacion de movimientos para depositos, retiros y transferencias
    // se manejara en TransactionService para mantener la cohesion.
}