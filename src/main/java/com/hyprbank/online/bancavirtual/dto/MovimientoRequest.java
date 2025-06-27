package com.hyprbank.online.bancavirtual.dto;

import com.hyprbank.online.bancavirtual.model.Movimiento.TipoMovimiento;
import java.math.BigDecimal;

// Importaciones de validación (Necesarias)
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para solicitudes de movimientos bancarios.
 *
 * Utilizado para recibir y validar los datos de entrada cuando un usuario
 * intenta realizar un movimiento (depósito, retiro, transferencia interna).
 * Contiene campos necesarios para procesar la solicitud.
 *
 * Contiene campos relevantes para la vista y la logica.
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class MovimientoRequest {

    @NotBlank(message = "El numero de cuenta es obligatorio")
    private String numeroCuenta;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @Size(max = 255, message = "La descripcion no puede exceder los 255 caracteres")
    private String descripcion;

    // El tipo de movimiento (DEPOSITO, RETIRO, etc.).
    // Su validación (@NotNull) dependerá de si se espera siempre en la solicitud
    // o si puede ser inferido por el contexto (ej., un endpoint específico para "deposito").
    private TipoMovimiento tipo; // INGRESO o EGRESO (usando el enum de Movimiento)
}