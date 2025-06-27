package com.hyprbank.online.bancavirtual.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Importaciones de Lombok
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // Este constructor no es el que usaria JPA, pero es util para tests/creacion manual

// Importaciones de Spring Security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Importante para la integracion con Spring Security

/*
 * Entidad JPA que representa un usuario del sistema en la base de datos.
 *
 * Mapea la tabla 'usuarios' y define la estructura de los datos
 * personales y de autenticacion de los usuarios.
 *
 * Implementa UserDetails de Spring Security, lo cual es fundamental
 * para que Spring Security pueda gestionar la autenticacion y autorizacion
 * del usuario. Esto significa que esta clase sera la representacion
 * principal de un usuario autenticado en el contexto de seguridad.
 *
 * Contiene relaciones con otras entidades como Roles, Cuentas y Movimientos.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 * El constructor sin argumentos es obligatorio para JPA.
 */

@Entity // Indica que esta clase es una entidad JPA y se mapeara a una tabla
@Table(name = "usuarios") // Especifica el nombre de la tabla en la base de datos

@Data // Anotacion lombok para getters, setters, toString, equals, hashCode
@NoArgsConstructor // Anotacion lombok para el constructor vacio (OBLIGATORIO PARA JPA)
@AllArgsConstructor // Anotacion lombok para el constructor con todos los argumentos
public class Usuario implements UserDetails { // <-- Implementa UserDetails

    @Id // Marca este campo como la clave primaria de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 100)
    private String email; // Usado como nombre de usuario (username) para el login

    @Column(nullable = false, length = 255) // La longitud debe ser suficiente para la contraseña encriptada
    private String password;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now(); // <-- Inicializado directamente

    @Column(nullable = false)
    private boolean enabled = true; // Por defecto, el usuario esta habilitado

    @Column(nullable = false)
    private boolean accountNonExpired = true; // Por defecto, la cuenta no ha expirado

    @Column(nullable = false)
    private boolean credentialsNonExpired = true; // Por defecto, las credenciales no han expirado

    @Column(nullable = false)
    private boolean accountNonLocked = true; // Por defecto, la cuenta no esta bloqueada

    // Relacion Many-to-Many con Rol
    // Un usuario puede tener varios roles, y un rol puede ser asignado a varios usuarios.
    @ManyToMany(fetch = FetchType.EAGER) // EAGER para roles es comun ya que son pocos y se necesitan al autenticar
    @JoinTable(
        name = "usuario_roles", // Nombre de la tabla intermedia
        joinColumns = @JoinColumn(name = "usuario_id"), // Columna que referencia a 'usuarios'
        inverseJoinColumns = @JoinColumn(name = "rol_id") // Columna que referencia a 'roles'
    )
    private Set<Rol> roles = new HashSet<>(); // Usamos Set para evitar roles duplicados

    // Relacion One-to-Many con Cuenta
    // Un usuario puede tener varias cuentas bancarias
    // CascadeType.ALL: Si se elimina el usuario, se eliminan sus cuentas.
    // orphanRemoval = true: Si una cuenta es desasociada del usuario, se elimina de la DB.
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Cuenta> cuentas; // Lista de cuentas del usuario

    // Relacion One-to-Many con AccesoUsuario
    // Un usuario puede tener multiples registros de acceso
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AccesoUsuario> accesos; // Historial de accesos del usuario

    // ----------------------------------------------------------------------
    // Metodos requeridos por la interfaz UserDetails (Spring Security)
    // ----------------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Devuelve la coleccion de roles del usuario.
        // Spring Security la usa para autorizacion.
        return roles;
    }

    @Override
    public String getUsername() {
        // Devuelve el nombre de usuario para la autenticacion.
        // En este caso, usamos el email como username.
        return email;
    }

    // Los siguientes metodos reflejan el estado de las propiedades de la cuenta.
    // Los campos 'enabled', 'accountNonExpired', 'credentialsNonExpired', 'accountNonLocked'
    // controlan el estado de la cuenta de usuario para Spring Security.
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
}