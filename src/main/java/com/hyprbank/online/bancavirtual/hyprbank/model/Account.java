package com.hyprbank.online.bancavirtual.hyprbank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * Entidad JPA que representa una cuenta bancaria en la base de datos.
 *
 * Mapea la tabla 'cuentas' y define la estructura de los datos
 * relacionados con las cuentas de los usuarios.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 * El constructor sin argumentos es obligatorio para JPA.
 *
 * NOTA SOBRE INICIALIZACION DE CAMPOS:
 * Al inicializar campos directamente en su declaracion (ej. 'private BigDecimal saldo = BigDecimal.ZERO;'),
 * estos valores por defecto se aplican cada vez que una instancia de 'Cuenta' es creada,
 * ya sea manualmente con 'new Cuenta()' o por JPA al cargarla desde la base de datos.
 * La desventaja es que, si un objeto 'Cuenta' es cargado de la DB, estos valores por defecto
 * pueden sobrescribir accidentalmente los valores existentes si no se maneja cuidadosamente,
 * aunque JPA normalmente poblara los campos con los datos de la DB DESPUES de la construccion
 * inicial. Es mas apropiado para valores que quieres que siempre esten presentes al instanciar
 * un objeto nuevo EN MEMORIA antes de que tenga datos de la DB. Para logica mas compleja
 * o garantias de persistencia, se podrian usar metodos @PrePersist o logica en el servicio.
 */

@Entity // Indica que esta clase es una entidad JPA y se mapeara a una tabla
@Table(name = "accounts") // Nombre de la tabla en la base de datos
@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos (OBLIGATORIO PARA JPA)
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO; // <-- Inicializado directamente

    @Column(nullable = false, length = 50)
    private String accountType = "SAVINGS"; // <-- Inicializado directamente

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // <-- Inicializado directamente

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate = LocalDateTime.now(); // <-- Inicializado directamente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Columna de clave foranea en la tabla 'accounts'
    private User user; // La entidad User a la que pertenece esta cuenta
}
