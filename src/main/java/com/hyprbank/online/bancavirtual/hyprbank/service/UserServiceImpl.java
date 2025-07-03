package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.model.Role;
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.RoleRepository; // ¡IMPORTANTE: Nuevo import!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;
import java.util.Collection;
import java.util.stream.Collectors;

/*
 * Implementacion de la interfaz {@link UserService}.
 *
 * Esta clase provee la lógica de negocio para la gestión de usuarios,
 * incluyendo el registro, la consulta, la integración con Spring Security para la autenticación,
 * la creación automática de cuentas bancarias y la inicialización de usuarios clave.
 *
 * Esta versión utiliza un RoleRepository para una gestión más robusta y estándar de las entidades Role.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository; // ¡NUEVO: Inyección del RoleRepository!

    /*
     * Constructor para inyección de dependencias.
     * Spring inyectará las instancias necesarias, incluyendo RoleRepository.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                           AccountRepository accountRepository, RoleRepository roleRepository) { // ¡NUEVO: Añadido RoleRepository!
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository; // ¡NUEVO: Asignación de RoleRepository!
    }

    /**
     * Guarda un nuevo usuario en la base de datos, encripta la contraseña,
     * asigna un rol por defecto y crea automáticamente una cuenta bancaria asociada.
     *
     * @param registrationDTO El DTO con la información del usuario a registrar.
     * @return La entidad {@link User} guardada.
     * @throws RuntimeException Si el email ya está registrado.
     */
    @Override
    @Transactional
    public User save(RegistrationRequest registrationDTO) {
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("El email '" + registrationDTO.getEmail() + "' ya está registrado.");
        }

        // --- 1. Obtener o crear el rol por defecto (ROLE_USER) ---
        // Esta es la forma recomendada: buscar el rol. Si no existe, crearlo y guardarlo.
        Role userRole = roleRepository.findByName("ROLE_USER")
                                    .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));


        // --- 2. Crear y guardar el usuario ---
        User user = User.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .accounts(new ArrayList<>()) // Inicializa la lista de cuentas vacía
                .roles(Collections.singletonList(userRole)) // Usa el rol obtenido/creado por el RoleRepository
                .build();

        User savedUser = userRepository.save(user);

        // --- 3. Lógica para crear la cuenta bancaria asociada al usuario ---
        Account newAccount = new Account();
        newAccount.setUser(savedUser);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setAccountType("AHORROS");
        newAccount.setStatus("ACTIVA");

        String generatedAccountNumber;
        Random random = new Random();
        int accountNumberLength = 12;

        do {
            StringBuilder sb = new StringBuilder(accountNumberLength);
            for (int i = 0; i < accountNumberLength; i++) {
                sb.append(random.nextInt(10));
            }
            generatedAccountNumber = sb.toString();
        } while (accountRepository.findByAccountNumber(generatedAccountNumber).isPresent());

        newAccount.setAccountNumber(generatedAccountNumber);
        savedUser.addAccount(newAccount);
        accountRepository.save(newAccount); // Mantenemos esta línea explícita por claridad

        return savedUser;
    }

    /**
     * Carga los detalles de un usuario por su nombre de usuario (email) para Spring Security.
     *
     * @param email La dirección de correo electrónico del usuario.
     * @return Un objeto {@link UserDetails} que representa al usuario autenticado.
     * @throws UsernameNotFoundException Si el usuario no es encontrado con el email proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña inválidos"));
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
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email La dirección de correo electrónico del usuario.
     * @return La entidad {@link User} si se encuentra, o lanza UsernameNotFoundException.
     * @throws UsernameNotFoundException Si no se encuentra un usuario con el email proporcionado.
     */
    @Override
    public User findByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    // Método auxiliar para mapear roles a GrantedAuthority, necesario para Spring Security
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }

    /**
     * Método que se ejecuta después de que el bean se ha inicializado.
     * Crea un usuario administrador por defecto si no existe.
     */
    @PostConstruct
    @Transactional
    public void initAdmin() {
        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            // --- Obtener o crear el rol de ADMINISTRADOR (ROLE_ADMIN) ---
            // Usamos RoleRepository para buscar el rol. Si no existe, lo creamos y guardamos.
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                        .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Principal")
                    .email("admin@admin.com")
                    .password(passwordEncoder.encode("admin123"))
                    .accounts(new ArrayList<>())
                    .roles(Collections.singletonList(adminRole)) // Usa el rol obtenido/creado por el RoleRepository
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuario administrador 'admin@admin.com' creado con contraseña 'admin123'.");
        } else {
            System.out.println("ℹ️ Usuario administrador 'admin@admin.com' ya existe.");
        }
    }
}