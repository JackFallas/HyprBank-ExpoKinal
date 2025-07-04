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

import org.springframework.data.domain.PageRequest; // Importar PageRequest
import org.springframework.data.domain.Pageable; // Importar Pageable

// Importaciones de Java Utilities
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator; // Para ordenar la lista final

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
     * Obtiene una lista de movimientos para un usuario especifico, con opciones de filtrado y límite.
     * Los movimientos pueden ser filtrados por un rango de fechas y/o por tipo de movimiento.
     * Si no se proporcionan filtros, devuelve todos los movimientos del usuario.
     *
     * @param userId El ID del usuario cuyos movimientos se desean obtener.
     * @param startDate La fecha de inicio del rango para filtrar movimientos (opcional).
     * @param endDate La fecha de fin del rango para filtrar movimientos (opcional).
     * @param type El tipo de movimiento (INCOME o EXPENSE) para filtrar (opcional, se espera String "INCOME", "EXPENSE", "todos").
     * @param limit El número máximo de movimientos a devolver (opcional).
     * @return Una lista de {@link MovementDTO} que representan los movimientos encontrados.
     * @throws IllegalArgumentException Si el usuario no es encontrado.
     */
    public List<MovementDTO> getMovementHistory(Long userId, LocalDate startDate, LocalDate endDate, String type, Integer limit) {
        // Buscar el usuario por ID para asegurar que existe.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        // Obtener todas las cuentas asociadas a este usuario.
        List<Account> userAccounts = accountRepository.findByUser(user);

        // Define Pageable si se proporciona un límite. Si no hay límite, usa Pageable.unpaged()
        // para obtener todos los resultados sin paginación.
        Pageable pageable = (limit != null && limit > 0) ? PageRequest.of(0, limit) : Pageable.unpaged();

        // Recopilar todos los movimientos de todas las cuentas del usuario, aplicando filtros y límite si existen.
        List<Movement> movements = userAccounts.stream()
                .flatMap(account -> {
                    // Convertir el String 'type' a MovementType enum si no es nulo y no es "todos"
                    MovementType movementType = null;
                    if (type != null && !type.equalsIgnoreCase("todos")) {
                        try {
                            movementType = MovementType.valueOf(type.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            // Si el tipo no es válido, se ignora el filtro de tipo.
                            // Podrías loggear un error aquí si lo deseas.
                        }
                    }

                    if (startDate != null && endDate != null && movementType != null) {
                        // Filtrar por rango de fechas y tipo
                        return movementRepository.findByAccountIdAndTypeAndDateBetweenOrderByDateDesc(account.getId(), movementType, startDate, endDate, pageable).stream();
                    } else if (startDate != null && endDate != null) {
                        // Filtrar solo por rango de fechas
                        return movementRepository.findByAccountIdAndDateBetweenOrderByDateDesc(account.getId(), startDate, endDate, pageable).stream();
                    } else if (movementType != null) {
                        // Filtrar solo por tipo de movimiento
                        return movementRepository.findByAccountIdAndTypeOrderByDateDesc(account.getId(), movementType, pageable).stream();
                    } else {
                        // No hay filtros de fecha ni tipo, obtener todos los movimientos de la cuenta con el límite
                        return movementRepository.findByAccountIdOrderByDateDesc(account.getId(), pageable).stream();
                    }
                })
                .sorted(Comparator.comparing(Movement::getDate).reversed()) // Ordenar todos los movimientos recopilados por fecha descendente.
                .limit(limit != null && limit > 0 ? limit : Long.MAX_VALUE) // Aplicar el límite final después de recopilar y ordenar
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
        // Asegúrate de que el DTO también tenga el balance si lo necesitas en el frontend
        dto.setBalance(movement.getAccount().getBalance());
        return dto;
    }
}
