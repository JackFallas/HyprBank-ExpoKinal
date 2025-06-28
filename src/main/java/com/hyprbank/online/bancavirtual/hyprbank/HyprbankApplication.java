package com.hyprbank.online.bancavirtual.hyprbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {
    UserDetailsServiceAutoConfiguration.class // Excluye la autoconfiguracion del UserDetailsService en memoria de Spring Security
})

public class HyprbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(HyprbankApplication.class, args);
	}

}
