package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/*
 * Implementacion de la interfaz {@link UserService}.
 *
 * Esta clase provee la logica de negocio para la gestion de usuarios,
 * incluyendo el registro, la consulta y la integracion con Spring Security para la autenticacion.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /*
     * Constructor para inyeccion de dependencias.
     * Spring inyectara las instancias de UserRepository y BCryptPasswordEncoder.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * La contrasena se encripta antes de ser guardada.
     *
     * @param registrationDTO El DTO con la informacion del usuario a registrar.
     * @return La entidad {@link User} guardada.
     */
    @Override
    public User save(RegistrationRequest registrationDTO) {
        // Crea una nueva instancia de User a partir del DTO.
        User user = new User(
                registrationDTO.getId(),
                registrationDTO.getFirstName(),
                registrationDTO.getLastName(),
                registrationDTO.getEmail(),
                passwordEncoder.encode(registrationDTO.getPassword()), // Encripta la contrasena
                null // La lista de cuentas se inicializa en el constructor de User
        );
        // Guarda el usuario en el repositorio.
        return userRepository.save(user);
    }

    /**
     * Carga los detalles de un usuario por su nombre de usuario (email).
     * Este metodo es parte de la interfaz UserDetailsService de Spring Security.
     *
     * @param email La direccion de correo electronico del usuario.
     * @return Un objeto {@link UserDetails} que representa al usuario autenticado.
     * @throws UsernameNotFoundException Si el usuario no es encontrado con el email proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    /**
     * Lista todos los usuarios existentes en la base de datos.
     *
     * @return Una {@link List} de entidades {@link User}.
     */
    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    /**
     * Busca un usuario por su direccion de correo electronico.
     * Proporciona la entidad {@link User} directamente o lanza una excepcion si no se encuentra.
     *
     * @param email La direccion de correo electronico del usuario.
     * @return La entidad {@link User} si se encuentra.
     * @throws UsernameNotFoundException Si no se encuentra un usuario con el email proporcionado.
     */
    @Override
    public User findByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }
}