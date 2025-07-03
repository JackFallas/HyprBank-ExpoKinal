package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.RegistrationRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.UserUpdateRequest;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.model.Role;
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.RoleRepository;

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
 * la creación automática de cuentas bancarias y la inicialización de usuarios clave,
 * así como la actualización y eliminación de usuarios.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    /*
     * Constructor para inyección de dependencias.
     * Spring inyectará las instancias necesarias.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                           AccountRepository accountRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
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
        Role userRole = roleRepository.findByName("ROLE_USER")
                                     .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        // --- 2. Crear y guardar el usuario ---
        User user = User.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .dpi(registrationDTO.getDpi())
                .nit(registrationDTO.getNit())
                .phoneNumber(registrationDTO.getPhoneNumber())
                .accounts(new ArrayList<>())
                .roles(Collections.singletonList(userRole))
                .enabled(true) // Los nuevos usuarios se crean activos por defecto
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
        accountRepository.save(newAccount);

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

    /**
     * Busca un usuario por su ID.
     *
     * @param id El ID del usuario a buscar.
     * @return Un {@link Optional} que contiene la entidad {@link User} si se encuentra, o vacío si no.
     */
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Actualiza la informacion de un usuario existente a partir de un DTO de actualizacion.
     *
     * @param updateRequest El DTO con la informacion actualizada del usuario.
     * @return La entidad {@link User} actualizada.
     * @throws RuntimeException Si el usuario no se encuentra o hay un problema al actualizar.
     */
    @Override
    @Transactional
    public User updateUser(UserUpdateRequest updateRequest) {
        // Buscar el usuario existente por ID
        User existingUser = userRepository.findById(updateRequest.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + updateRequest.getId()));

        // Actualizar los campos desde el DTO
        existingUser.setFirstName(updateRequest.getFirstName());
        existingUser.setLastName(updateRequest.getLastName());
        existingUser.setEmail(updateRequest.getEmail());
        existingUser.setDpi(updateRequest.getDpi());
        existingUser.setNit(updateRequest.getNit());
        existingUser.setPhoneNumber(updateRequest.getPhoneNumber());

        // Mapear el String 'estado' del DTO al boolean 'enabled' de la entidad User
        // "Activo" -> true, cualquier otro valor -> false
        existingUser.setEnabled("Activo".equalsIgnoreCase(updateRequest.getEstado()));

        // No actualizamos la contraseña aquí por seguridad; debería ser un proceso separado.

        return userRepository.save(existingUser);
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id El ID del usuario a eliminar.
     * @throws RuntimeException Si el usuario no se encuentra.
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }


    // Método auxiliar para mapear roles a GrantedAuthority, necesario para Spring Security
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }

    /**
     * Método que se ejecuta después de que el bean se ha inicializado.
     * Crea o actualiza un usuario administrador por defecto si no existe o si necesita ser reseteado.
     */
    @PostConstruct
    @Transactional
    public void initAdmin() {
        Optional<User> adminOptional = userRepository.findByEmail("admin@admin.com");
        User admin;
        String defaultAdminPassword = "admin123"; // La contraseña por defecto sin codificar

        if (adminOptional.isEmpty()) {
            // Si el admin no existe, lo creamos
            System.out.println("ℹ️ Usuario administrador 'admin@admin.com' no encontrado. Creándolo...");
            // --- Obtener o crear el rol de ADMINISTRADOR (ROLE_ADMIN) ---
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                         .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

            admin = User.builder()
                    .firstName("Admin")
                    .lastName("Principal")
                    .email("admin@admin.com")
                    .password(passwordEncoder.encode(defaultAdminPassword)) // Codificar la contraseña
                    .dpi("1234567890123")
                    .nit("1234567-8")
                    .phoneNumber("50212345678")
                    .accounts(new ArrayList<>())
                    .roles(Collections.singletonList(adminRole))
                    .enabled(true) // Asegurar que el admin esté habilitado
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuario administrador 'admin@admin.com' creado con contraseña 'admin123'.");
        } else {
            // Si el admin ya existe, lo actualizamos para asegurar que esté habilitado y con la contraseña correcta
            admin = adminOptional.get();
            boolean needsUpdate = false;

            // Asegurarse de que esté habilitado
            if (!admin.isEnabled()) {
                admin.setEnabled(true);
                needsUpdate = true;
                System.out.println("✅ Usuario administrador 'admin@admin.com' habilitado.");
            }

            // Opcional: Si quieres forzar la actualización de la contraseña cada vez que inicia la app
            // Esto es útil en desarrollo para evitar problemas de contraseña, pero no para producción.
            // if (!passwordEncoder.matches(defaultAdminPassword, admin.getPassword())) {
            //     admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            //     needsUpdate = true;
            //     System.out.println("✅ Contraseña del administrador 'admin@admin.com' reseteada a 'admin123'.");
            // }

            if (needsUpdate) {
                userRepository.save(admin);
            }
            System.out.println("ℹ️ Usuario administrador 'admin@admin.com' ya existe y está verificado.");
        }
    }
}
