package com.hyprbank.online.bancavirtual.hyprbank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList; // Asegurate de que esta importacion este presente para ArrayList

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

    // Relación OneToMany con Account: Un usuario puede tener muchas cuentas
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>(); // Inicializar para evitar NullPointerException

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles;

    // --- Métodos de utilidad para la relación bidireccional (tomado de la copia, adaptado a nombres) ---
    public void addAccount(Account account) {
        if (this.accounts == null) {
            this.accounts = new ArrayList<>();
        }
        this.accounts.add(account);
        account.setUser(this); // Asegura que la cuenta también sepa a qué usuario pertenece
    }

    public void removeAccount(Account account) {
        if (this.accounts != null) {
            this.accounts.remove(account);
            account.setUser(null); // Desvincula la cuenta del usuario
        }
    }
    // --- FIN Métodos de utilidad ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles != null && !this.roles.isEmpty()) {
            return this.roles;
        }
        // Si no hay roles específicos, asignar un rol por defecto
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // El email es el nombre de usuario para Spring Security
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Lógica por defecto, se puede cambiar para gestionar la expiración de la cuenta
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Lógica por defecto, se puede cambiar para gestionar el bloqueo de la cuenta
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Lógica por defecto, se puede cambiar para gestionar la expiración de credenciales
    }

    @Override
    public boolean isEnabled() {
        return true; // Lógica por defecto, se puede cambiar para habilitar/deshabilitar usuarios
    }
}