package com.hyprbank.online.bancavirtual.hyprbank.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientReportDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String dpi;
    private String nit;
    private String phoneNumber;
    private boolean enabled;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}