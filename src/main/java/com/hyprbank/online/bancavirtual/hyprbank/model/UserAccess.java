package com.hyprbank.online.bancavirtual.hyprbank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


// Importaciones de Hibernate para OnDelete
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
/*
 * Entidad JPA que representa un registro de acceso de usuario en la base de datos.
 *
 * Mapea la tabla 'accesos_usuarios' y define la estructura de los datos
 * relacionados con los intentos y tipos de acceso de los usuarios a la aplicacion.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 * El constructor sin argumentos es obligatorio para JPA.
 */

@Entity // Indica que esta clase es una entidad JPA y se mapeara a una tabla
@Table(name = "user_accesses") // Especifica el nombre de la tabla en la base de datos
@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos (OBLIGATORIO PARA JPA)
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos (Util para conveniencia, no para JPA directamente)
@Builder
public class UserAccess {

    @Id // Marca este campo como la clave primaria de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Le dice a la base de datos que genere automaticamente el valor del ID (autoincremento)
    private Long id; // El ID unico para cada registro de acceso

    @ManyToOne(fetch = FetchType.LAZY) // Define una relacion de muchos-a-uno: muchos accesos pueden ser de un solo usuario
    @JoinColumn(name = "user_id", nullable = true) // <--- CAMBIADO A nullable = true
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user; // El objeto User al que pertenece este registro de acceso (puede ser null para intentos fallidos)

    @Column(nullable = false) // Indica que esta columna no puede ser nula
    private LocalDateTime accessDateTime; // Almacena la fecha y hora exacta del acceso

    @Column(length = 50) // Define la longitud maxima de la columna en la base de datos
    private String accessType; // Un string para describir el tipo de acceso (ej. "LOGIN_SUCCESS", "LOGIN_FAILED", "LOGOUT")

    @Column(length = 45) // Define la longitud para la direccion IP
    private String ipAddress; // Opcional: para registrar la direccion IP desde donde se hizo el acceso

    @Column(name = "attempted_username", length = 255) // <--- NUEVA COLUMNA para el nombre de usuario intentado
    private String attemptedUsername; // Almacena el nombre de usuario que se intentó usar (útil para fallos)
}
