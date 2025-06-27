package com.hyprbank.online.bancavirtual.dto;

import com.hyprbank.online.bancavirtual.model.Movimiento.TipoMovimiento; 
import java.math.BigDecimal;
import java.time.LocalDate;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la entidad Movimiento.
 *
 * Utilizado para transferir la informacion de un movimiento bancario
 * entre las capas de la aplicacion de forma segura y controlada,
 * evitando exponer directamente la entidad del modelo.
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class MovimientoDTO {
    private Long id;
    private Long cuentaId; // ID de la cuenta asociada
    private String numeroCuenta; // Número de cuenta para mostrar en el frontend
    private LocalDate fecha;
    private String descripcion;
    private TipoMovimiento tipo;
    private BigDecimal monto;
}