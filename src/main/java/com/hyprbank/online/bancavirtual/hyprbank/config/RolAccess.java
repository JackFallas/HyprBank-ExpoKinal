package com.hyprbank.online.bancavirtual.hyprbank.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails; // Importar UserDetails
import com.hyprbank.online.bancavirtual.hyprbank.model.User; // Importar tu entidad User

import java.io.IOException;

@Component
public class RolAccess implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Obtener el principal autenticado
        Object principal = authentication.getPrincipal();

        // Verificar si el principal es una instancia de tu clase User
        // Esto es importante porque Spring Security puede devolver un UserDetails genérico
        if (principal instanceof User) {
            User user = (User) principal;
            // Línea 37: Verificar si el usuario requiere cambio de contraseña
            if (user.getRequiresPasswordChange()) {
                response.sendRedirect("/change-password"); // Redirige a una página para cambiar la contraseña
                return; // Importante: salir del método después de la redirección
            }
        }

        // Si no requiere cambio de contraseña, procede con la redirección por rol
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String role = auth.getAuthority(); // Obtiene el nombre del rol (ej. "ROLE_ADMIN", "ROLE_USER")
            if (role.equals("ROLE_ADMIN")) {
                response.sendRedirect("/dashboard/admin"); // Redirige a una URL específica para admin
                return;
            } else if (role.equals("ROLE_USER")) {
                response.sendRedirect("/dashboard/user"); // Redirige a una URL específica para usuario
                return;
            }
        }

        // Si por alguna razón el usuario autenticado no tiene ninguno de los roles esperados,
        // redirige a la página de usuario por defecto.
        response.sendRedirect("/dashboard/user"); // Redirección por defecto a dashboard de usuario
    }
}