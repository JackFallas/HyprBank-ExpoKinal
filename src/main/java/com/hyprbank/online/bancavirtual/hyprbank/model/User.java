package com.hyprbank.online.bancavirtual.hyprbank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList; // Para inicializar la lista de cuentas

/*
 * Clase de entidad para representar un Usuario en el sistema bancario.
 *
 * Esta clase mapea a la tabla 'usuarios' en la base de datos y
 * implementa la interfaz UserDetails de Spring Security para integrarse
 * con el sistema de autenticacion.
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * @Data de Lombok genera getters, setters, toString, equals y hashCode.
 * @NoArgsConstructor y @AllArgsConstructor de Lombok generan constructores.
 */
@Entity
@Table(name = "users") // Nombre de tabla actualizado
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // Nombre de clase actualizado

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName; // Nombre de campo actualizado

    @Column(nullable = false)
    private String lastName; // Nombre de campo actualizado

    @Column(nullable = false, unique = true)
    private String email; // Usado como username en Spring Security

    @Column(nullable = false)
    private String password;

    // Relacion One-to-Many con Account: Un usuario puede tener multiples cuentas
    // mappedBy="user" indica que el campo 'user' en la entidad Account es el propietario de la relacion.
    // cascade = CascadeType.ALL significa que las operaciones (persist, remove, merge) se propagaran a las entidades Account asociadas.
    // orphanRemoval = true asegura que si una cuenta se desvincula de un usuario, se elimine de la base de datos.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>(); // Inicializa la lista para evitar NullPointerExceptions

    // Si tienes roles, puedes anadir una relacion ManyToMany aqui
    @ManyToMany(fetch = FetchType.EAGER) // Carga los roles inmediatamente con el usuario
    @JoinTable(
        name = "user_roles", // Nombre de tabla de union actualizado
        joinColumns = @JoinColumn(name = "user_id"), // Columna de union para User
        inverseJoinColumns = @JoinColumn(name = "role_id") // Columna de union para Role
    )
    private Collection<Role> roles; // Nombre de clase actualizado

    // --- Metodos de la interfaz UserDetails ---

    /**
     * Retorna las autoridades (roles) concedidas al usuario.
     * Para este ejemplo, se retorna un rol basico "ROLE_USER".
     * Si tienes una gestion de roles mas compleja (ej. tabla de roles),
     * deberias mapear tus roles a GrantedAuthority.
     *
     * @return Una coleccion de GrantedAuthority.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si el usuario tiene roles asignados, los devuelve. Si no, asigna un rol por defecto.
        if (this.roles != null && !this.roles.isEmpty()) {
            return this.roles;
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Retorna la contrasena utilizada para autenticar al usuario.
     *
     * @return La contrasena del usuario.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Retorna el nombre de usuario utilizado para autenticar al usuario.
     * En nuestro caso, el email es el nombre de usuario.
     *
     * @return El email del usuario.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica si la cuenta del usuario ha expirado.
     *
     * @return true si la cuenta es valida (no ha expirado), false de lo contrario.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Por defecto, la cuenta no expira
    }

    /**
     * Indica si el usuario esta bloqueado o desbloqueado.
     *
     * @return true si el usuario no esta bloqueado, false de lo contrario.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Por defecto, la cuenta no esta bloqueada
    }

    /**
     * Indica si las credenciales (contrasena) del usuario han expirado.
     *
     * @return true si las credenciales son validas (no han expirado), false de lo contrario.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Por defecto, las credenciales no expiran
    }

    /**
     * Indica si el usuario esta habilitado o deshabilitado.
     *
     * @return true si el usuario esta habilitado, false de lo contrario.
     */
    @Override
    public boolean isEnabled() {
        return true; // Por defecto, el usuario esta habilitado
    }
}