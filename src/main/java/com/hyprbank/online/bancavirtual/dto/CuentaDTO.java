package com.hyprbank.online.bancavirtual.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/* 
 * DTO (Data Transfer Object) para la entidad Cuenta.   
 * 
 * Utilizado para transferir informacion de la cuenta entre las capas de la aplicacion
 * de forma segura y controlada, evita exponer la entidad del modelo
 * 
 * Contiene campos relevantes para la vista y la logica
 * 
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
*/

@Data // Anotacion lambok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lambok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lambok que genera un constructor con todos los argumentos

public class CuentaDTO {
    private Long id;
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldo;
    private String estado;
    private LocalDateTime fechaCreacion;

    // Informacion del usuario asociada a la cuenta
    private Long usuarioId;
    private String usuarioNombreCompleto;
}
