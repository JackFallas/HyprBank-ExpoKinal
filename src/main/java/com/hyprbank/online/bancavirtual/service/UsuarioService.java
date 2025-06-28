package com.hyprbank.online.bancavirtual.service;

import com.hyprbank.online.bancavirtual.dto.RegistroRequest;
import com.hyprbank.online.bancavirtual.model.Usuario;
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
public interface UsuarioService extends UserDetailsService {

    /**
     * Guarda un nuevo usuario en el sistema a partir de un DTO de registro.
     * Esta operacion puede incluir la encriptacion de la contrasena y la asignacion de roles por defecto.
     *
     * @param registroDTO El DTO que contiene los datos del usuario a registrar.
     * @return La entidad {@link Usuario} guardada con sus datos completos (incluyendo ID generado).
     */
    Usuario guardar(RegistroRequest registroDTO);

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return Una {@link List} de todas las entidades {@link Usuario} existentes.
     */
    List<Usuario> listarUsuarios();

    /**
     * Busca un usuario por su direccion de correo electronico (email).
     * Este metodo es fundamental para la autenticacion de usuarios.
     *
     * @param email La direccion de correo electronico del usuario a buscar.
     * @return La entidad {@link Usuario} si se encuentra.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException Si no se encuentra un usuario con el email proporcionado.
     */
    Usuario buscarPorEmail(String email) throws UsernameNotFoundException;
}