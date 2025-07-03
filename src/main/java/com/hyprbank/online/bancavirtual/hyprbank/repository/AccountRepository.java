package com.hyprbank.online.bancavirtual.hyprbank.repository;

import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/*
 * Interfaz de Repositorio para la entidad Account.
 *
 * Proporciona métodos para interactuar con la base de datos utilizando CRUD.
 *
 * Extiende JpaRepository de Spring Data JPA.
 * esto nos da acceso a métodos predefinidos del CRUD.
 *
 * @param <Account> El tipo de la entidad con la que trabaja este repositorio.
 * @param <Long> El tipo de PK de la entidad Account (El ID).
 */

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    /*
     * Método personalizado para buscar una cuenta por su número de cuenta.
     * Spring Data JPA lo implementará automáticamente siguiendo las convenciones de nombres.
     *
     * @param accountNumber El número único de la cuenta a buscar.
     * @return Un Optional que contiene la Account si se encuentra, o un Optional vacío si no.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

     /*
     * Método personalizado para buscar todas las cuentas asociadas a un usuario específico.
     *
     * @param user El objeto User cuyas cuentas se desean encontrar.
     * @return Una lista de Accounts asociadas al usuario. Puede estar vacía si el usuario no tiene cuentas.
     */
    List<Account> findByUser(User user);

    /*
     * Método personalizado para buscar una cuenta específica por su número de cuenta
     * y que pertenezca a un usuario en particular. Esto añade una capa extra de seguridad
     * para asegurar que un usuario solo pueda acceder a sus propias cuentas.
     *
     * @param accountNumber El número de la cuenta a buscar.
     * @param user El objeto User al que debe pertenecer la cuenta.
     * @return Un Optional que contiene la Account si coincide con el número y el usuario,
     * o un Optional vacío si no se encuentra.
     */
    Optional<Account> findByAccountNumberAndUser(String accountNumber, User user);
}