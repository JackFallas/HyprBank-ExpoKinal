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

    // --- NUEVO CAMPO AÑADIDO: 'enabled' ---
    @Column(nullable = false) // Generalmente un usuario está habilitado o deshabilitado
    private boolean enabled;
    // --- FIN NUEVO CAMPO ---

    // Relación OneToMany con Account: Un usuario puede tener muchas cuentas
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Este método es parte de UserDetails y ahora devuelve el valor de la propiedad 'enabled'
        return this.enabled;
    }
}

