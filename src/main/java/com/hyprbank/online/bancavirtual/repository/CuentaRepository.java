package com.hyprbank.online.bancavirtual.repository;

import com.hyprbank.online.bancavirtual.model.Cuenta;
import com.hyprbank.online.bancavirtual.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/*
 * Interfaz de Repositorio para la entidad Cuenta.
 *
 * Proporciona métodos para interactuar con la base de datos utilizando CRUD.
 *
 * Extiende JpaRepository de Spring Data JPA.
 * esto nos da acceso a métodos predefinidos del CRUD.
 *
 * @param <Cuenta> El tipo de la entidad con la que trabaja este repositorio.
 * @param <Long> El tipo de PK de la entidad Cuenta (El ID).
 */

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    /*
     * Método personalizado para buscar una cuenta por su número de cuenta.
     * Spring Data JPA lo implementará automáticamente siguiendo las convenciones de nombres.
     *
     * @param numeroCuenta El número único de la cuenta a buscar.
     * @return Un Optional que contiene la Cuenta si se encuentra, o un Optional vacío si no.
     */
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

     /*
     * Método personalizado para buscar todas las cuentas asociadas a un usuario específico.
     *
     * @param usuario El objeto Usuario cuyas cuentas se desean encontrar.
     * @return Una lista de Cuentas asociadas al usuario. Puede estar vacía si el usuario no tiene cuentas.
     */
    List<Cuenta> findByUsuario(Usuario usuario);

    /*
     * Método personalizado para buscar una cuenta específica por su número de cuenta
     * y que pertenezca a un usuario en particular. Esto añade una capa extra de seguridad
     * para asegurar que un usuario solo pueda acceder a sus propias cuentas.
     *
     * @param numeroCuenta El número de la cuenta a buscar.
     * @param usuario El objeto Usuario al que debe pertenecer la cuenta.
     * @return Un Optional que contiene la Cuenta si coincide con el número y el usuario,
     * o un Optional vacío si no se encuentra.
     */
    Optional<Cuenta> findByNumeroCuentaAndUsuario(String numeroCuenta, Usuario usuario);
}
