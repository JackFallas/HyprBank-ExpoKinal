package com.hyprbank.online.bancavirtual.hyprbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // Importar esto

@SpringBootApplication(exclude = {
    UserDetailsServiceAutoConfiguration.class, // Excluye la autoconfiguracion del UserDetailsService en memoria de Spring Security
    SecurityAutoConfiguration.class // NUEVO: Excluye la autoconfiguración de seguridad completa
})

public class HyprbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(HyprbankApplication.class, args);
	}

}
