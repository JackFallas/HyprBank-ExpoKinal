package com.hyprbank.online.bancavirtual.hyprbank.repository;

import com.hyprbank.online.bancavirtual.hyprbank.model.UserAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Interfaz de Repositorio para la entidad UserAccess.
 *
 * Proporciona métodos para interactuar con la base de datos utilizando CRUD.
 *
 * Extiende JpaRepository de Spring Data JPA.
 * esto nos da acceso a métodos predefinidos del CRUD.
 *
 * @param <UserAccess> El tipo de la entidad con la que trabaja este repositorio.
 * @param <Long> El tipo de PK de la entidad UserAccess (El ID).
 */

@Repository
public interface UserAccessRepository extends JpaRepository<UserAccess, Long> {
    /*
     * Nota:
     * De momento no hay metodos personalizados adicionales
     * unicamente estan los proporcionados por JpaRepository
     */
}
