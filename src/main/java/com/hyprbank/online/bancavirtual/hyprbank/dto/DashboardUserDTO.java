package com.hyprbank.online.bancavirtual.hyprbank.dto;

import java.util.List;
import java.math.BigDecimal;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // Asegurate de que esta importacion este presente

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
@Builder // ¡Añade esta anotacion para habilitar el patron Builder!
public class DashboardUserDTO {
    private Long id;
    private String fullName;
    private String email;
    private BigDecimal totalBalance;
    private List<AccountDTO> accounts;
}
