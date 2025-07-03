package com.hyprbank.online.bancavirtual.hyprbank.repository;

import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * Interfaz de Repositorio para la entidad User
 *
 * Proporciona metodos para interactuar con la base de datos utilizando CRUD
 *
 * Extiende JpaRepository de Spring Data JPA
 * esto nos da acceso a metodos predefinidos del crud
 *
 * @param <User> El tipo de la entidad con la que trabaja este repositorio
 * @param <Long> El tipo de PK de la entidad User (El ID)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /*
     * Este metodo busca a un usuario por su email utilizando este
     * mismo como el "username" para iniciar sesion
     *
     * @param email Es la direccion de correo del usuario a buscar
     * @return Un Optional que devuelve el usuario si lo encuentra, si no devuelve un Optional vacio
     * lo utilizamos para manejar posibles ausencias de usuarios de forma segura evitando NullPointerExceptions
     *
     */
    Optional<User> findByEmail(String email);
}
