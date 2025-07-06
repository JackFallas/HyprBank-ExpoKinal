package com.hyprbank.online.bancavirtual.hyprbank.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set; // Importar Set para la colección de roles

/*
 * Entidad JPA que representa un usuario en la base de datos.
 *
 * Mapea la tabla 'users' y define la estructura de los datos
 * relacionados con los usuarios de la aplicación.
 *
 * Implementa UserDetails de Spring Security para ser reconocida
 * como un principal de seguridad.
 *
 * Utilizaremos Lombok para generar automáticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 * El constructor sin argumentos es obligatorio para JPA.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "accounts"})
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

    @Column(nullable = true)
    private String dpi;

    @Column(nullable = true)
    private String nit;

    @Column(nullable = true)
    private String phoneNumber;

    // --- Campos de estado para Spring Security ---
    @Column(nullable = false)
    private boolean enabled = true; // Si la cuenta está habilitada (puede iniciar sesión)

    @Column(nullable = false)
    private boolean accountNonExpired = true; // Si la cuenta no ha expirado

    @Column(nullable = false)
    private boolean accountNonLocked = true; // Si la cuenta no está bloqueada

    @Column(nullable = false)
    private boolean credentialsNonExpired = true; // Si las credenciales no han expirado
    // --- Fin Campos de estado ---

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts;

    @ManyToMany(fetch = FetchType.EAGER) // Cargar roles inmediatamente con el usuario
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles;

    // --- Métodos de utilidad para la relación bidireccional ---
    public void addAccount(Account account) {
        if (this.accounts == null) {
            this.accounts = new ArrayList<>();
        }
        this.accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(Account account) {
        if (this.accounts != null) {
            this.accounts.remove(account);
            account.setUser(null);
        }
    }
    // --- FIN Métodos de utilidad ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles != null && !this.roles.isEmpty()) {
            return this.roles;
        }
        // Si no tiene roles asignados, por defecto le damos el rol de usuario básico.
        // Esto es un fallback, lo ideal es que siempre tenga al menos ROLE_USER.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Este método es añadido para resolver el error en tiempo de ejecución
     * "The method getRequiresPasswordChange() is undefined for the type User".
     * Si no tienes una lógica específica para forzar el cambio de contraseña,
     * simplemente devuelve `false`.
     *
     * @return `true` si el usuario debe cambiar su contraseña, `false` en caso contrario.
     */
    public boolean getRequiresPasswordChange() {
        // Por ahora, siempre devuelve false. Si en el futuro necesitas una lógica
        // para forzar el cambio de contraseña (ej. después del primer login, o si la contraseña expira),
        // la implementarías aquí, quizás con un nuevo campo en la base de datos para el usuario.
        return false;
    }
}