package com.hyprbank.online.bancavirtual.repository;

import com.hyprbank.online.bancavirtual.model.AccesoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Interfaz de Repositorio para la entidad AccesoUsuario.
 *
 * Proporciona métodos para interactuar con la base de datos utilizando CRUD.
 *
 * Extiende JpaRepository de Spring Data JPA.
 * esto nos da acceso a métodos predefinidos del CRUD.
 *
 * @param <AccesoUsuario> El tipo de la entidad con la que trabaja este repositorio.
 * @param <Long> El tipo de PK de la entidad AccesoUsuario (El ID).
 */

@Repository
public interface AccesoUsuarioRepository extends JpaRepository<AccesoUsuario, Long> {
    /*
     * Nota:
     * De momento no hay metodos personalizados adicionales
     * unicamente estan los proporcionados por JpaRepository
     */
}
