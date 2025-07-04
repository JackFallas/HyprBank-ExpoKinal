package com.hyprbank.online.bancavirtual.hyprbank.repository;

import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement.MovementType; // Importa el enum anidado

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Importar Pageable para paginación y limitación de resultados
import org.springframework.data.domain.Pageable;


/*
 * Interfaz de Repositorio para la entidad Movement.
 *
 * Proporciona métodos para interactuar con la base de datos utilizando CRUD.
 *
 * Extiende JpaRepository de Spring Data JPA.
 * esto nos da acceso a métodos predefinidos del CRUD.
 *
 * @param <Movement> El tipo de la entidad con la que trabaja este repositorio.
 * @param <Long> El tipo de PK de la entidad Movement (El ID).
 */

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    /*
     * Busca todos los movimientos para una cuenta específica, ordenados por fecha descendente.
     * Ahora acepta un parámetro Pageable para limitar los resultados.
     * @param accountId El ID de la cuenta.
     * @param pageable Objeto Pageable para paginación y limitación de resultados.
     * @return Lista de movimientos.
     */
    List<Movement> findByAccountIdOrderByDateDesc(Long accountId, Pageable pageable);

    /*
     * Busca movimientos para una cuenta dentro de un rango de fechas, ordenados por fecha descendente.
     * Ahora acepta un parámetro Pageable para limitar los resultados.
     * @param accountId El ID de la cuenta.
     * @param startDate La fecha de inicio del rango (inclusive).
     * @param endDate La fecha de fin del rango (inclusive).
     * @param pageable Objeto Pageable para paginación y limitación de resultados.
     * @return Lista de movimientos.
     */
    List<Movement> findByAccountIdAndDateBetweenOrderByDateDesc(Long accountId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /*
     * Busca movimientos para una cuenta, un tipo específico y dentro de un rango de fechas, ordenados por fecha descendente.
     * Ahora acepta un parámetro Pageable para limitar los resultados.
     * @param accountId El ID de la cuenta.
     * @param type El tipo de movimiento (INCOME, EXPENSE).
     * @param startDate La fecha de inicio del rango (inclusive).
     * @param endDate La fecha de fin del rango (inclusive).
     * @param pageable Objeto Pageable para paginación y limitación de resultados.
     * @return Lista de movimientos.
     */
    List<Movement> findByAccountIdAndTypeAndDateBetweenOrderByDateDesc(Long accountId, MovementType type, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /*
     * Busca movimientos para una cuenta y un tipo específico, ordenados por fecha descendente.
     * Ahora acepta un parámetro Pageable para limitar los resultados.
     * @param accountId El ID de la cuenta.
     * @param type El tipo de movimiento (INCOME, EXPENSE).
     * @param pageable Objeto Pageable para paginación y limitación de resultados.
     * @return Lista de movimientos.
     */
    List<Movement> findByAccountIdAndTypeOrderByDateDesc(Long accountId, MovementType type, Pageable pageable);
}
