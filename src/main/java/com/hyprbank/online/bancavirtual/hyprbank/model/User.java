package com.hyprbank.online.bancavirtual.hyprbank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate; // Importar para la fecha de nacimiento
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/*
 * Clase de entidad para representar un Usuario en el sistema bancario.
 *
 * Esta clase mapea a la tabla 'users' en la base de datos y
 * implementa la interfaz UserDetails de Spring Security para integrarse
 * con el sistema de autenticacion.
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * @Data de Lombok genera getters, setters, toString, equals y hashCode.
 * @NoArgsConstructor y @AllArgsConstructor de Lombok generan constructores.
 * @Builder de Lombok permite construir objetos de forma mas flexible.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // --- Nuevos campos de informacion personal ---

    @Column(nullable = false, unique = true) // DPI suele ser unico
    private String dpi;

    @Column(nullable = false)
    private String nit; // NIT no necesariamente unico, pero requerido

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalDate birthDate; // Fecha de nacimiento

    // Relacion One-to-Many con Account: Un usuario puede tener multiples cuentas
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    // Relacion Many-to-Many con Role: Un usuario puede tener multiples roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles;

    // --- Metodos de la interfaz UserDetails ---

    /**
     * Retorna las autoridades (roles) concedidas al usuario.
     * Si el usuario tiene roles asignados, los retorna. De lo contrario,
     * asigna un rol por defecto "ROLE_USER".
     *
     * @return Una coleccion de GrantedAuthority.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
        return true;
    }

    /**
     * Indica si el usuario esta bloqueado o desbloqueado.
     *
     * @return true si el usuario no esta bloqueado, false de lo contrario.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica si las credenciales (contrasena) del usuario han expirado.
     *
     * @return true si las credenciales son validas (no han expirado), false de lo contrario.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario esta habilitado o deshabilitado.
     *
     * @return true si el usuario esta habilitado, false de lo contrario.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}