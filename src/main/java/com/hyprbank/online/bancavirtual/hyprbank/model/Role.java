package com.hyprbank.online.bancavirtual.hyprbank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.security.core.GrantedAuthority; // Importante para la integracion con Spring Security

/*
 * Entidad JPA que representa un rol de usuario en la base de datos.
 *
 * Mapea la tabla 'roles' y define la estructura de los roles (ej. ADMIN, USER)
 * que seran utilizados para la gestion de permisos y autorizacion en la aplicacion.
 *
 * Implementa GrantedAuthority de Spring Security para ser reconocida
 * como una autoridad de seguridad.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 * El constructor sin argumentos es obligatorio para JPA.
 *
 * NOTA SOBRE INICIALIZACION DE CAMPOS:
 * Al inicializar campos directamente en su declaracion (ej. 'private String nombre = "USUARIO";'),
 * estos valores por defecto se aplican cada vez que una instancia de 'Rol' es creada,
 * ya sea manualmente con 'new Rol()' o por JPA al cargarla desde la base de datos.
 * La desventaja es que, si un objeto 'Rol' es cargado de la DB, estos valores por defecto
 * pueden sobrescribir accidentalmente los valores existentes si no se maneja cuidadosamente,
 * aunque JPA normalmente poblara los campos con los datos de la DB DESPUES de la construccion
 * inicial. Es mas apropiado para valores que quieres que siempre esten presentes al instanciar
 * un objeto nuevo EN MEMORIA antes de que tenga datos de la DB. Para logica mas compleja
 * o garantias de persistencia, se podrian usar metodos @PrePersist o logica en el servicio.
 */

@Entity // Indica que esta clase es una entidad JPA y se mapeara a una tabla
@Table(name = "roles") // Especifica el nombre de la tabla en la base de datos (usando 'roles' como en tu segundo codigo)
@Data // Anotacion lombok para getters, setters, toString, equals, hashCode
@NoArgsConstructor // Anotacion lombok para el constructor vacio (OBLIGATORIO PARA JPA)
@AllArgsConstructor // Anotacion lombok para el constructor con todos los argumentos
public class Role implements GrantedAuthority { // Nombre de clase actualizado

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // El nombre del rol no puede ser nulo y debe ser unico
    private String name; // Ej. "ROLE_USER", "ROLE_ADMIN" (convencion de Spring Security) - Nombre de campo actualizado

    @Override
    public String getAuthority() {
        // Este metodo es requerido por GrantedAuthority y devuelve el nombre del rol.
        // Spring Security lo usa para determinar los permisos.
        return name; // Nombre de campo actualizado
    }
}