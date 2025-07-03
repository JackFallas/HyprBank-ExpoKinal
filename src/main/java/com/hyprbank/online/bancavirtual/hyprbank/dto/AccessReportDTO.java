package com.hyprbank.online.bancavirtual.hyprbank.dto;

import java.time.LocalDateTime;

// Importaciones de Lombok (facilitan proceso, usenlas)
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/*
 * DTO (Data Transfer Object) para el reporte de accesos de usuario.
 *
 * Utilizado para transferir la informacion detallada de los eventos de acceso
 * de los usuarios, preparada para la generacion de reportes.
 *
 * Contiene campos relevantes para la vista y la logica
 *
 * Utilizaremos Lombok para generar automaticamente getters, setters, toString(), equals() y hashCode() para todos los campos
 */

@Data // Anotacion lombok que genera automaticamente lo anterior mencionado
@NoArgsConstructor // Anotacion lombok que genera constructor sin argumentos
@AllArgsConstructor // Anotacion lombok que genera un constructor con todos los argumentos

public class AccessReportDTO {
    private String userName;
    private String userLastName;
    private String userEmail;
    private LocalDateTime accessDateTime;
    private String accessType;
    private String ipAddress;
}
