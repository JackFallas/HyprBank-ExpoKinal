package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/*
 * Interfaz de Servicio para la gestion de usuarios.
 *
 * Extiende UserDetailsService de Spring Security para manejar la carga de detalles del usuario
 * durante el proceso de autenticacion.
 *
 * Define las operaciones de negocio relacionadas con los usuarios, como el registro,
 * la listado y la busqueda por email.
 */
public interface UserService extends UserDetailsService {

    /**
     * Guarda un nuevo usuario en el sistema a partir de un DTO de registro.
     * Esta operacion puede incluir la encriptacion de la contrasena y la asignacion de roles por defecto.
     *
     * @param registrationDTO El DTO que contiene los datos del usuario a registrar.
     * @return La entidad {@link User} guardada con sus datos completos (incluyendo ID generado).
     */
    User save(RegistrationRequest registrationDTO);

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return Una {@link List} de todas las entidades {@link User} existentes.
     */
    List<User> listUsers();

    /**
     * Busca un usuario por su direccion de correo electronico (email).
     * Este metodo es fundamental para la autenticacion de usuarios.
     *
     * @param email La direccion de correo electronico del usuario a buscar.
     * @return La entidad {@link User} si se encuentra.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException Si no se encuentra un usuario con el email proporcionado.
     */
    User findByEmail(String email) throws UsernameNotFoundException;
}