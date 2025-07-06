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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet; // Importar HashSet para roles

/*
 * Implementación del servicio de gestión de usuarios.
 * Proporciona la lógica de negocio para operaciones relacionadas con usuarios,
 * incluyendo registro, actualización, eliminación y carga de detalles de usuario
 * para Spring Security.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository; // Para la creación de cuenta por defecto

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           BCryptPasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }

    /**
     * Guarda un nuevo usuario en el sistema.
     * Asigna un rol por defecto (ROLE_USER) y crea una cuenta bancaria inicial.
     *
     * @param registrationDTO DTO con los datos del usuario a registrar.
     * @return El usuario guardado.
     * @throws RuntimeException Si el email ya está registrado o hay un problema al guardar.
     */
    @Override
    @Transactional
    public User save(RegistrationRequest registrationDTO) {
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new RuntimeException("El email " + registrationDTO.getEmail() + " ya está registrado.");
        }

        // Buscar el rol ROLE_USER, si no existe, crearlo
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        User user = User.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .dpi(registrationDTO.getDpi())
                .nit(registrationDTO.getNit())
                .phoneNumber(registrationDTO.getPhoneNumber())
                .enabled(true) // Habilitar usuario por defecto
                .roles(Collections.singletonList(userRole)) // Asignar el rol de usuario
                .build();

        User savedUser = userRepository.save(user);

        // Crear una cuenta bancaria por defecto para el nuevo usuario
        Account defaultAccount = new Account();
        defaultAccount.setAccountNumber("ACC" + savedUser.getId() + (int)(Math.random() * 10000)); // Generar un número de cuenta simple
        defaultAccount.setBalance(BigDecimal.ZERO);
        defaultAccount.setAccountType("SAVINGS");
        defaultAccount.setStatus("ACTIVE");
        defaultAccount.setUser(savedUser); // Asociar la cuenta al usuario
        accountRepository.save(defaultAccount);

        return savedUser;
    }

    /**
     * Carga los detalles de un usuario por su nombre de usuario (email).
     * Este método es utilizado por Spring Security para la autenticación.
     *
     * @param username El email del usuario.
     * @return Un objeto UserDetails que representa al usuario autenticado.
     * @throws UsernameNotFoundException Si el usuario no es encontrado.
     */
    @Override
    @Transactional(readOnly = true) // Asegura que las relaciones LAZY se puedan cargar dentro de la transacción
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));

        // Asegúrate de que los roles se carguen si son LAZY
        user.getRoles().size(); // Fuerza la inicialización de la colección de roles

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(), // Asegúrate de que el estado enabled se use
                true, true, true,
                mapRolesToAuthorities(user.getRoles()));
    }

    /**
     * Mapea una colección de objetos Role a una colección de GrantedAuthority.
     *
     * @param roles La colección de roles del usuario.
     * @return Una colección de GrantedAuthority.
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return Una lista de objetos {@link User}.
     */
    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    /**
     * Busca un usuario por su dirección de correo electrónico (email).
     *
     * @param email La dirección de correo electrónico del usuario a buscar.
     * @return La entidad {@link User} si se encuentra.
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
     * Actualiza la información de un usuario existente.
     *
     * @param updateRequest DTO con la información actualizada del usuario.
     * @return La entidad {@link User} actualizada.
     * @throws RuntimeException Si el usuario no se encuentra o hay un problema al actualizar.
     */
    @Override
    @Transactional
    public User updateUser(UserUpdateRequest updateRequest) {
        User existingUser = userRepository.findById(updateRequest.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + updateRequest.getId()));

        existingUser.setFirstName(updateRequest.getFirstName());
        existingUser.setLastName(updateRequest.getLastName());
        existingUser.setEmail(updateRequest.getEmail());
        existingUser.setDpi(updateRequest.getDpi());
        existingUser.setNit(updateRequest.getNit());
        existingUser.setPhoneNumber(updateRequest.getPhoneNumber());

        // Manejar el estado del usuario
        if (updateRequest.getEstado() != null) {
            switch (updateRequest.getEstado().toLowerCase()) {
                case "activo":
                    existingUser.setEnabled(true);
                    existingUser.setAccountNonLocked(true); // Asumo que "activo" implica no bloqueado
                    break;
                case "inactivo":
                    existingUser.setEnabled(false);
                    existingUser.setAccountNonLocked(true); // Inactivo no es lo mismo que bloqueado
                    break;
                case "bloqueado":
                    existingUser.setAccountNonLocked(false);
                    existingUser.setEnabled(true); // Un usuario bloqueado puede seguir "habilitado" pero no puede iniciar sesión
                    break;
                default:
                    // Si el estado no es reconocido, no hacer nada o lanzar una excepción
                    break;
            }
        }

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

    /**
     * Método que se ejecuta después de la construcción del bean para inicializar
     * el usuario administrador si no existe.
     */
    @PostConstruct
    @Transactional
    public void createAdminUserIfNotFound() {
        String adminEmail = "adminhyprbank@gmail.com";
        String defaultAdminPassword = "admin123"; // Contraseña en texto plano para el hash

        Optional<User> adminOptional = userRepository.findByEmail(adminEmail);

        if (adminOptional.isEmpty()) {
            // Si el rol ROLE_ADMIN no existe, crearlo
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("HyprBank")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(defaultAdminPassword))
                    .dpi("9999999999999")
                    .nit("GENERICO123456")
                    .phoneNumber("1234-5678")
                    .enabled(true) // Asegurarse de que el admin esté habilitado
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(new HashSet<>(Collections.singletonList(adminRole))) // Asignar el rol ROLE_ADMIN
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuario administrador '" + adminEmail + "' creado con contraseña '" + defaultAdminPassword + "'.");
        } else {
            // Si el admin ya existe, lo actualizamos para asegurar que esté habilitado y con la contraseña correcta
            User admin = adminOptional.get();
            boolean needsUpdate = false;

            // Asegurarse de que esté habilitado
            if (!admin.isEnabled()) {
                admin.setEnabled(true);
                needsUpdate = true;
                System.out.println("✅ Usuario administrador '" + adminEmail + "' habilitado.");
            }

            // Opcional: Si quieres forzar la actualización de la contraseña cada vez que inicia la app
            // Esto es útil en desarrollo para evitar problemas de contraseña, pero no para producción.
            // if (!passwordEncoder.matches(defaultAdminPassword, admin.getPassword())) {
            //     admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            //     needsUpdate = true;
            //     System.out.println("✅ Contraseña del administrador '" + adminEmail + "' reseteada a '" + defaultAdminPassword + "'.");
            // }

            // Asegurarse de que el rol ROLE_ADMIN esté asignado
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));
            if (!admin.getRoles().contains(adminRole)) {
                admin.getRoles().add(adminRole);
                needsUpdate = true;
                System.out.println("✅ Rol 'ROLE_ADMIN' asignado al usuario '" + adminEmail + "'.");
            }


            if (needsUpdate) {
                userRepository.save(admin);
            }
            System.out.println("ℹ️ Usuario administrador '" + adminEmail + "' ya existe y está verificado.");
        }
    }
}