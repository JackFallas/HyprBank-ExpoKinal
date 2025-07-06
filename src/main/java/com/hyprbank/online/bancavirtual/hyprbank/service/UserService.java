package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.UserUpdateRequest; // Importar el nuevo DTO
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

/*
 * Interfaz de Servicio para la gestion de usuarios.
 *
 * Extiende UserDetailsService de Spring Security para manejar la carga de detalles del usuario
 * durante el proceso de autenticacion.
 *
 * Define las operaciones de negocio relacionadas con los usuarios, como el registro,
 * la listado, la busqueda por email, la actualizacion y la eliminacion.
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
     * Lista todos los {@link User} existentes.
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

    /**
     * Busca un usuario por su ID.
     *
     * @param id El ID del usuario a buscar.
     * @return Un {@link Optional} que contiene la entidad {@link User} si se encuentra, o vacío si no.
     */
    Optional<User> findById(Long id);

    /**
     * Actualiza la informacion de un usuario existente a partir de un DTO de actualizacion.
     *
     * @param updateRequest El DTO con la informacion actualizada del usuario.
     * @return La entidad {@link User} actualizada.
     * @throws RuntimeException Si el usuario no se encuentra o hay un problema al actualizar.
     */
    User updateUser(UserUpdateRequest updateRequest);

    /**
     * Elimina un usuario por su ID.
     *
     * @param id El ID del usuario a eliminar.
     * @throws RuntimeException Si el usuario no se encuentra.
     */
    void deleteUser(Long id);

    /**
     * Cambia la contraseña de un usuario específico.
     *
     * @param userId El ID del usuario cuya contraseña se va a cambiar.
     * @param newPassword La nueva contraseña en texto plano.
     * @throws RuntimeException Si el usuario no se encuentra o hay un problema al actualizar la contraseña.
     */
    User changeUserPassword(Long userId, String newPassword);
}