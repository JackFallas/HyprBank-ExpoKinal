package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationResponse; // Importar el nuevo DTO de respuesta
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.model.Role;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

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
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL_CHAR = "!@#$%^&*()-_=+[]{}|;:',.<>/?";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;
    private static SecureRandom random = new SecureRandom();

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
     * Genera una contraseña aleatoria segura basada en los criterios especificados.
     * - Mínimo 8 caracteres
     * - Al menos 1 mayúscula
     * - Al menos 1 minúscula
     * - Al menos 1 número
     * - Al menos 1 carácter especial
     *
     * @return La contraseña aleatoria generada.
     */
    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(8); // Minimo 8 caracteres

        // Asegurar al menos 1 de cada tipo
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(SPECIAL_CHAR.charAt(random.nextInt(SPECIAL_CHAR.length())));

        // Rellenar el resto hasta 8 caracteres (o mas si se desea)
        for (int i = 4; i < 8; i++) { // Empezar en 4 porque ya agregamos 4 caracteres
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }

        // Mezclar la contraseña para que los caracteres obligatorios no esten siempre al inicio
        for (int i = 0; i < password.length(); i++) {
            int randomIndex = random.nextInt(password.length());
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(randomIndex));
            password.setCharAt(randomIndex, temp);
        }

        return password.toString();
    }

    /**
     * Guarda un nuevo usuario en la base de datos.
     * La contrasena se encripta antes de ser guardada.
     *
     * @param registrationDTO El DTO con la informacion del usuario a registrar.
     * @return Un {@link RegistrationResponse} que contiene el email del usuario y la contraseña generada (sin encriptar).
     * @throws IllegalArgumentException Si ya existe un usuario con el email o DPI proporcionado.
     */
    @Override
    public RegistrationResponse save(RegistrationRequest registrationDTO) {
        // Validacion para evitar emails duplicados
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este email: " + registrationDTO.getEmail());
        }
        // Validacion para evitar DPI duplicados
        if (userRepository.findByDpi(registrationDTO.getDpi()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este DPI: " + registrationDTO.getDpi());
        }

        String generatedPassword = generateRandomPassword(); // Generar la contraseña aleatoria
        System.out.println("Contraseña generada para " + registrationDTO.getEmail() + ": " + generatedPassword); // Solo para depuracion, NO en produccion real

        // Crear una nueva instancia de User a partir del DTO usando el Builder.
        User user = User.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(generatedPassword)) // Encriptar la contraseña generada
                .dpi(registrationDTO.getDpi())
                .nit(registrationDTO.getNit())
                .phoneNumber(registrationDTO.getPhoneNumber())
                .address(registrationDTO.getAddress())
                .birthDate(registrationDTO.getBirthDate())
                .accounts(new ArrayList<>()) // Inicializa la lista de cuentas vacia
                .roles(Collections.singletonList(new Role(null, "ROLE_USER"))) // Asigna un rol por defecto
                .build();
        
        userRepository.save(user); // Guarda el usuario en el repositorio.

        // Retorna el DTO de respuesta con el email y la contraseña generada (sin encriptar)
        return new RegistrationResponse(registrationDTO.getEmail(), generatedPassword);
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