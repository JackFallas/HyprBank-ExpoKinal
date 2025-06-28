package com.hyprbank.online.bancavirtual.repository;

import com.hyprbank.online.bancavirtual.model.Movimiento;
import com.hyprbank.online.bancavirtual.model.Movimiento.TipoMovimiento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/*
 * Interfaz de Repositorio para la entidad Movimiento.
 *
 * Proporciona métodos para interactuar con la base de datos utilizando CRUD.
 *
 * Extiende JpaRepository de Spring Data JPA.
 * esto nos da acceso a métodos predefinidos del CRUD.
 *
 * @param <Movimiento> El tipo de la entidad con la que trabaja este repositorio.
 * @param <Long> El tipo de PK de la entidad Movimiento (El ID).
 */

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    /*
     * Busca todos los movimientos para una cuenta específica, ordenados por fecha descendente.
     * @param cuentaId El ID de la cuenta.
     * @return Lista de movimientos.
     */
    List<Movimiento> findByCuentaIdOrderByFechaDesc(Long cuentaId);

    /*
     * Busca movimientos para una cuenta dentro de un rango de fechas, ordenados por fecha descendente.
     * @param cuentaId El ID de la cuenta.
     * @param fechaInicio La fecha de inicio del rango (inclusive).
     * @param fechaFin La fecha de fin del rango (inclusive).
     * @return Lista de movimientos.
     */
    List<Movimiento> findByCuentaIdAndFechaBetweenOrderByFechaDesc(Long cuentaId, LocalDate fechaInicio, LocalDate fechaFin);

    /*
     * Busca movimientos para una cuenta, un tipo específico y dentro de un rango de fechas, ordenados por fecha descendente.
     * @param cuentaId El ID de la cuenta.
     * @param tipo El tipo de movimiento (DEPOSITO, RETIRO, TRANSFERENCIA).
     * @param fechaInicio La fecha de inicio del rango (inclusive).
     * @param fechaFin La fecha de fin del rango (inclusive).
     * @return Lista de movimientos.
     */
    List<Movimiento> findByCuentaIdAndTipoAndFechaBetweenOrderByFechaDesc(Long cuentaId, TipoMovimiento tipo, LocalDate fechaInicio, LocalDate fechaFin);

    /*
     * Busca movimientos para una cuenta y un tipo específico, ordenados por fecha descendente.
     * @param cuentaId El ID de la cuenta.
     * @param tipo El tipo de movimiento (DEPOSITO, RETIRO, TRANSFERENCIA).
     * @return Lista de movimientos.
     */
    List<Movimiento> findByCuentaIdAndTipoOrderByFechaDesc(Long cuentaId, TipoMovimiento tipo);    
}
