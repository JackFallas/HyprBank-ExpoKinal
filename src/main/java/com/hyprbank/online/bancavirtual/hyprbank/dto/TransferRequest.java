package com.hyprbank.online.bancavirtual.hyprbank.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para la entidad Transferencia.
 *
 * Utilizado para recibir y validar los datos de entrada cuando un usuario
 * intenta realizar una transferencia entre cuentas del mismo banco.
 * Contiene campos necesarios para procesar la solicitud.
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos.
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class TransferRequest {

    @NotBlank(message = "El numero de cuenta origen es obligatorio")
    private String originAccountNumber; // Nombre de campo actualizado

    @NotBlank(message = "El numero de cuenta destino es obligatorio")
    private String destinationAccountNumber; // Nombre de campo actualizado

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal amount; // Nombre de campo actualizado

    @Size(max = 255, message = "La descripcion no puede exceder los 255 caracteres")
    private String description;
}