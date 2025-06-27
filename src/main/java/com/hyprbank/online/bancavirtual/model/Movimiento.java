package com.hyprbank.online.bancavirtual.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // Este constructor no es el que usaria JPA, pero es util para tests/creacion manual

/*
 * Entidad JPA que representa un movimiento bancario en la base de datos.
 *
 * Mapea la tabla 'movimientos' y define la estructura de los datos
 * relacionados con las transacciones (ingresos y egresos) de las cuentas.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 * El constructor sin argumentos es obligatorio para JPA.
 *
 * NOTA SOBRE INICIALIZACION DE CAMPOS:
 * Al inicializar campos directamente en su declaracion (ej. 'private LocalDate fecha = LocalDate.now();'),
 * estos valores por defecto se aplican cada vez que una instancia de 'Movimiento' es creada,
 * ya sea manualmente con 'new Movimiento()' o por JPA al cargarla desde la base de datos.
 * La desventaja es que, si un objeto 'Movimiento' es cargado de la DB, estos valores por defecto
 * pueden sobrescribir accidentalmente los valores existentes si no se maneja cuidadosamente,
 * aunque JPA normalmente poblara los campos con los datos de la DB DESPUES de la construccion
 * inicial. Es mas apropiado para valores que quieres que siempre esten presentes al instanciar
 * un objeto nuevo EN MEMORIA antes de que tenga datos de la DB. Para logica mas compleja
 * o garantias de persistencia, se podrian usar metodos @PrePersist o logica en el servicio.
 */

@Entity // Indica que esta clase es una entidad JPA y se mapeara a una tabla
@Table(name = "movimientos") // Nombre de la tabla en la base de datos

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos (OBLIGATORIO PARA JPA)
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false) // Columna de clave foranea en la tabla 'movimientos'
    private Cuenta cuenta;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now(); // <-- Inicializado directamente con la fecha actual

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING) // Guarda el nombre del enum (INGRESO/EGRESO) como String en la DB
    private TipoMovimiento tipo; // Tipo de movimiento: INGRESO o EGRESO

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    // Enum para el tipo de movimiento, anidado dentro de la clase Movimiento
    public enum TipoMovimiento {
        INGRESO,
        EGRESO
    }
}