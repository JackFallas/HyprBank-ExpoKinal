package com.hyprbank.online.bancavirtual.dto;

import java.util.List; 

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para el Dashboard del Usuario.
 *
 * Utilizado para consolidar y transferir la informacion esencial que se muestra
 * en el panel principal de un usuario (dashboard) despues de iniciar sesion.
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class DashboardUserDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private List<CuentaDTO> cuentas;
}